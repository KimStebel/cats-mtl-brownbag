package brownbag

import brownbag.Main._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

object MonadTransformers extends App {

  private def readName: IO[Either[Throwable, Name]] = IO {
    val name = "cats"
    if (name.length > 0) Right(Name(name)) else Left(new Exception("name too short!"))
  }

  private def readConfig: IO[String] = IO.pure("Hallo, ")

  private def printHello(name: Name): IO[Either[Throwable, Unit]] = {
    p(s"Hello, ${name.name}!").attempt
  }

  private def p(s: String) = IO { println(s) }

  private def greeting(name: Name): Reader[String, String] = {
    Reader { greeting => greeting + name.name + "!" }
  }

  private type IOE[X] = EitherT[IO, Throwable, X]
  private type RIOE[Y] = ReaderT[IOE, String, Y]

  private val program: IOE[Unit] = for {
    name <- EitherT(readName)
    result <- EitherT(printHello(name))
  } yield result

  private val programWithConfig: RIOE[Unit] = for {
    name <- ReaderT((_: String) => EitherT(readName))
    g <- greeting(name).lift[IOE]
    result <- ReaderT((_: String) => EitherT.right[Throwable](p(g)))
  } yield result

  program.value.unsafeRunSync().left.foreach(error => println(s"ERROR $error"))
  readConfig.flatMap(greeting => programWithConfig.run(greeting).value).unsafeRunSync().left.foreach(error => println(s"ERROR $error"))
}