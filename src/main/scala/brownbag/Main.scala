package brownbag

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

case class Name(name: String)

object Main extends App {

  private def readName: IO[Either[Throwable, Name]] = IO {
    val name = "cats"
    if (name.length > 0) Right(Name(name)) else Left(new Exception("name too short!"))
  }

  private def readConfig: IO[String] = IO.pure("Hallo, ")

  private def printHello(name: Name): IO[Unit] = {
    p(s"Hello, ${name.name}!")
  }

  private def p(s: String) = IO { println(s) }

  private def greeting(name: Name): Reader[String, String] = {
    Reader { greeting => greeting + name.name + "!" }
  }

  private val program: IO[Either[Throwable, Unit]] = {
    readName.flatMap {
      case Right(name) => printHello(name).map(Right(_))
      case Left(error) => IO.pure(Left(error))
    }
  }

  private val programWithConfig: IO[Either[Throwable, Unit]] = readConfig.flatMap(config => {
    readName.flatMap {
      case Right(name) => p(greeting(name).run(config)).map(Right(_))
      case Left(error) => IO.pure(Left(error))
    }
  })

  program.unsafeRunSync().left.foreach(error => println(s"ERROR $error"))
  programWithConfig.unsafeRunSync().left.foreach(error => println(s"ERROR $error"))
}


