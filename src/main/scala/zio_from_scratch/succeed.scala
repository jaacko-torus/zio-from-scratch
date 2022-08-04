package jaackotorus
package zio_from_scratch

import ziofs.ZIO

case class Person(name: String, age: Int)

object Person:
  val peter: Person = Person("Peter", 88)

trait ZIOApp:
  def run: ZIO[Any] = ???
  def main(args: Array[String]): Unit =
    println("default value")
