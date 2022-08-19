package jaackotorus
package zio_from_scratch.ziofs

import scala.concurrent.ExecutionContext

trait Fiber[+A]:
  def start(): Unit
  def join: ZIO[A]

class FiberImpl[A](zio: ZIO[A]) extends Fiber[A]:
  var maybeResult: Option[A]    = None
  var callbacks: List[A => Any] = List.empty
  override def start(): Unit =
    ExecutionContext.global.execute { () =>
      zio.run { a =>
        maybeResult = Some(a)
        callbacks.foreach { callback =>
          callback(a)
        }
      }
    }
  override def join: ZIO[A] = maybeResult match
    case Some(a) => ZIO.succeedNow(a)
    case None =>
      ZIO.async { complete =>
        callbacks = complete :: callbacks
      }

sealed trait ZIO[+A] { self =>
  final def run(callback: A => Unit): Unit = ???

  def flatMap[B](f: A => ZIO[B]): ZIO[B] =
    ZIO.FlatMap(self, f)

  def map[B](f: A => B): ZIO[B] =
    flatMap { a => ZIO.succeedNow(f(a)) }

  def zip[B](that: ZIO[B]): ZIO[(A, B)] =
    (self `zipWith` that)((_, _))

  def zipRight[B](that: ZIO[B]): ZIO[B] =
    (self `zipWith` that)((_, b) => b)

  def *>[B](that: ZIO[B]): ZIO[B] =
    (self `zipRight` that)

  def zipWith[B, C](that: ZIO[B])(f: (A, B) => C): ZIO[C] =
    for
      a <- self
      b <- that
    yield f(a, b)

  def as[B](value: => B): ZIO[B] =
    self.map(_ => value)

  def repeatN(n: Int): ZIO[Any] =
    if n <= 0 then ZIO.succeedNow(())
    else self *> repeatN(n - 1)

  def fork: ZIO[Fiber[A]] = ZIO.Fork(self)

  def zipPar[B](that: ZIO[B]): ZIO[(A, B)] =
    for
      fiberA <- self.fork
      fiberB <- that.fork
      a      <- fiberA.join
      b      <- fiberB.join
    yield (a, b)
}

object ZIO:
  def succeedNow[A](value: A): ZIO[A]                         = ZIO.Succeed(value)
  def succeed[A](value: => A): ZIO[A]                         = ZIO.Effect(() => value)
  def async[A](register: (complete: A => Any) => Any): ZIO[A] = ZIO.Async(register)

  case class Succeed[A](value: A) extends ZIO[A]:
    override def run(callback: A => Unit): Unit = callback(value)

  case class Effect[A](f: () => A) extends ZIO[A]:
    override def run(callback: A => Unit): Unit = callback(f())

  case class FlatMap[A, B](az: ZIO[A], f: A => ZIO[B]) extends ZIO[B]:
    override def run(callback: B => Unit): Unit =
      az.run { a =>
        f(a).run(callback)
      }
  case class Async[A](register: (A => Any) => Any) extends ZIO[A]:
    override def run(callback: A => Unit): Unit =
      register(callback)
  case class Fork[A](zio: ZIO[A]) extends ZIO[Fiber[A]]:
    override def run(callback: Fiber[A] => Unit): Unit =
      val fiber = FiberImpl(zio)
      fiber.start()
      callback(fiber)
