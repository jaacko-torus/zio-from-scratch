package jaackotorus
package zio_from_scratch

import ziofs.ZIO

case class Person(name: String, age: Int)

object Person:
  val peter: Person = Person("Peter", 88)

trait ZIOApp:
  def run: ZIO[Any]
  def main(args: Array[String]): Unit =
    run.run { result =>
      println(s"value: ${result}")
    }

    Thread.sleep(5000)

object succeedNow extends ZIOApp:
  val peterZIO: ZIO[Person] =
    ZIO.succeedNow(Person.peter)
  override def run: ZIO[Person] = peterZIO

object succeedNowUhOh extends ZIOApp:
  val howdyZIO =
    ZIO.succeedNow(println("Howdy 🏇🤠"))
  override def run: ZIO[Any] = ZIO.succeedNow(1)

object succeed extends ZIOApp:
  val howdyZIO =
    ZIO.succeed(println("Howdy 🏇🤠"))
  override def run: ZIO[Any] = ZIO.succeedNow(1)

object succeedAgain extends ZIOApp:
  def printLine(message: String): ZIO[Unit] =
    ZIO.succeed(println(message))
  override def run: ZIO[Any] = printLine("Fancy 🍴")

object zip extends ZIOApp:
  val zippedZIO: ZIO[(Int, String)] =
    ZIO.succeed(8) `zip` ZIO.succeed("LO")
  override def run: ZIO[Any] = zippedZIO

object map extends ZIOApp:
  val zippedZIO: ZIO[(Int, String)] =
    ZIO.succeed(8) `zip` ZIO.succeed("LO")
  val personZIO: ZIO[Person] = zippedZIO.map((i, s) => Person(s, i))
  val mappedZIO: ZIO[String] = zippedZIO.map((i, s) => s * i)
  override def run: ZIO[Any] = mappedZIO

object mapUhOh extends ZIOApp:
  val zippedZIO: ZIO[(Int, String)] =
    ZIO.succeed(8) `zip` ZIO.succeed("LO")

  def printLine(message: String): ZIO[Unit] =
    ZIO.succeed(println(message))

  val mappedZIO = zippedZIO.map { tuple =>
    printLine(s"tuple: $tuple")
  }

  override def run: ZIO[Any] = mappedZIO

object flatMap extends ZIOApp:
  val zippedZIO: ZIO[(Int, String)] =
    ZIO.succeed(8) `zip` ZIO.succeed("LO")

  def printLine(message: String): ZIO[Unit] =
    ZIO.succeed(println(message))

  val flatMappedZIO = zippedZIO.flatMap { tuple =>
    printLine(s"tuple: $tuple")
  }

  override def run: ZIO[Any] = flatMappedZIO

object forComprehension extends ZIOApp:
  val zippedZIO: ZIO[(Int, String)] =
    ZIO.succeed(8) `zip` ZIO.succeed("LO")

  def printLine(message: String): ZIO[Unit] =
    ZIO.succeed(println(message))

  val flatMappedZIO =
    for
      tuple <- zippedZIO
      _     <- printLine(s"tuple: $tuple")
    yield ()

  override def run: ZIO[Any] = flatMappedZIO

object async extends ZIOApp:
  val asyncZIO: ZIO[Int] = ZIO.async[Int] { complete =>
    println("Async start")
    Thread.sleep(1000)
    complete(10)
  }
  override def run = asyncZIO

object fork extends ZIOApp:
  def printLine(message: String): ZIO[Unit] =
    ZIO.succeed(println(message))
  val asyncZIO: ZIO[Int] = ZIO.async[Int] { complete =>
    println("Async start")
    Thread.sleep(1000)
    complete(scala.util.Random.nextInt(999))
  }
  val forkedZIO =
    for
      fiber1 <- asyncZIO.fork
      fiber2 <- asyncZIO.fork
      _      <- printLine("Done forking")
      int1   <- fiber1.join
      int2   <- fiber2.join
    yield s"The ints are $int1 & $int2"
  override def run = forkedZIO

object zipPar extends ZIOApp:
  def printLine(message: String): ZIO[Unit] =
    ZIO.succeed(println(message))
  val asyncZIO: ZIO[Int] = ZIO.async[Int] { complete =>
    println("Async start")
    Thread.sleep(1000)
    complete(scala.util.Random.nextInt(999))
  }
  override def run: ZIO[Any] = asyncZIO `zipPar` asyncZIO

object stackSafety extends ZIOApp:
  val program =
    ZIO.succeed(println("Hey!")).repeatN(10)
  override def run: ZIO[Any] = program
