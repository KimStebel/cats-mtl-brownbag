package brownbag

import brownbag.Main.p
import cats._
import cats.data.{ReaderT, _}
import cats.effect._
import cats.implicits._

case class Name(name: String)

object Main extends App {

  val name = "cats"

  def readName: IO[Either[Throwable, Name]] = IO {
    if (name.length > 0) Right(Name(name)) else Left(new Exception("name too short!"))
  }

  def readConfig: IO[String] = IO.pure("Hallo, ")

  def printHello(name: Name): IO[Either[Throwable, Unit]] = {
    p(s"Hello, ${name.name}!").attempt
  }

  def p(s: String) = IO { println(s) }

  def greeting(name: Name): Reader[String, String] = {
    Reader { greeting => greeting + name.name + "!" }
  }

  val program = {
    readName.flatMap {
      case Right(name) => printHello(name)
      case Left(error) => IO.pure(Left(error))
    }
  }

  val programMtl = for {
    name <- EitherT(readName)
    result <- EitherT(printHello(name))
  } yield result

  val programWithConfig = readConfig.flatMap(config => {
    readName.flatMap {
      case Right(name) => p(greeting(name).run(config)).attempt
      case Left(error) => IO.pure(Left(error))
    }
  })

  type IOE[X] = EitherT[IO, Throwable, X]
  type M[Y] = ReaderT[IOE, String, Y]

  val programWithConfigMtl = for {
    name <- ReaderT((_: String) => EitherT(readName))
    g <- greeting(name).lift[IOE]
    result <- ReaderT((_: String) => EitherT.right[Throwable](p(g)))
  } yield result

  import cats.mtl.ApplicativeAsk
  import cats.mtl.instances._

  def programUsingTypeClasses[F[_]: Monad : Sync : ApplicativeAsk[?[_], String]] = {

    def readName[F[_] : Sync]: F[Either[Throwable, Name]] = Sync[F].delay {
      if (name.length > 0) Right(Name(name)) else Left(new Exception("name too short!"))
    }

    def p[F[_]: Sync](s: String) = Sync[F].delay { println(s) }

    def greetingF[F[_]: ApplicativeAsk[?[_], String] : Functor](name: Name): F[String] = {
      ApplicativeAsk[F, String].ask.map(greeting => greeting + name.name + "!")
    }

    for {
      name <- readName.rethrow
      g <- greetingF(name)
      result <- p(g)
    } yield result

  }

  def readConfigF[F[_] : Sync]: F[String] = Sync[F].delay("Hallo, ")


  program.unsafeRunSync().left.foreach(error => println(s"ERROR $error"))
  programMtl.value.unsafeRunSync().left.foreach(error => println(s"ERROR $error"))
  programWithConfig.unsafeRunSync().left.foreach(error => println(s"ERROR $error"))
  readConfig.flatMap(greeting => programWithConfigMtl.run(greeting).value).unsafeRunSync().left.foreach(error => println(s"ERROR $error"))

  {
    implicit val aam: ApplicativeAsk[IO, String] = new ApplicativeAsk[IO, String] {
      override val applicative: Applicative[IO] = Applicative[IO]

      override def ask: IO[String] = readConfigF[IO]

      override def reader[A](f: String => A): IO[A] = ask.map(f)
    }

    programUsingTypeClasses[IO].unsafeRunSync()

  }
}


