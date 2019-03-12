package brownbag

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import cats.mtl.ApplicativeAsk

object TypeClasses extends App {

  private def readName[F[_] : Sync]: F[Either[Throwable, Name]] = Sync[F].delay {
    val name = "cats"
    if (name.length > 0) Right(Name(name)) else Left(new Exception("name too short!"))
  }

  private def readConfigF[F[_] : Sync]: F[String] = Sync[F].delay("Hallo, ")

  private def p[F[_]: Sync](s: String) = Sync[F].delay { println(s) }

  private def greetingF[F[_]](name: Name)(implicit ask: ApplicativeAsk[F, String]): F[String] = {
    ask.reader(greeting => greeting + name.name + "!")
  }

  private def program[F[_]: Sync]: EitherT[F, Throwable, Unit] = for {
    name <- EitherT(readName)
    result <- EitherT.right[Throwable](p("Hello, " + name.name + "!"))
  } yield result

  private def programWithConfig[F[_]: Sync : ApplicativeAsk[?[_], String]]: EitherT[F, Throwable, Unit] = for {
    name <- EitherT(readName)
    g <- EitherT.right[Throwable](greetingF(name))
    result <- EitherT.right[Throwable](p(g))
  } yield result

  {
    implicit val aam: ApplicativeAsk[IO, String] = new ApplicativeAsk[IO, String] {
      override val applicative: Applicative[IO] = Applicative[IO]
      override def ask: IO[String] = readConfigF[IO]
      override def reader[A](f: String => A): IO[A] = ask.map(f)
    }

    implicit val sid: Sync[Id] = new Sync[Id] {
      override def suspend[A](thunk: => Id[A]): Id[A] = thunk

      override def bracketCase[A, B](acquire: Id[A])(use: A => Id[B])(release: (A, ExitCase[Throwable]) => Id[Unit]): Id[B] = {
        var result: B = null.asInstanceOf[B]
        var error: Throwable = null.asInstanceOf[Throwable]
        try {
          result = use(acquire)
        } catch {
          case t: Throwable => error = t
        } finally {
          release(acquire, (if (result == null) ExitCase.Error(new Exception) else ExitCase.Completed))
        }
        result
      }

      override def raiseError[A](e: Throwable): Id[A] = throw e

      override def handleErrorWith[A](fa: Id[A])(f: Throwable => Id[A]): Id[A] = fa

      override def pure[A](x: A): Id[A] = x

      override def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)

      override def tailRecM[A, B](a: A)(f: A => Id[Either[A, B]]): Id[B] = f(a).right.get
    }
    program[IO].value.unsafeRunSync()
    programWithConfig[IO].value.unsafeRunSync()

    implicit val idaa: ApplicativeAsk[Id, String] = new ApplicativeAsk[Id, String] {
      override val applicative: Applicative[Id] = sid
      override def ask: Id[String] = "Hallo, "
      override def reader[A](f: String => A): Id[A] = f(ask)
    }

    program[Id]
    programWithConfig[Id]

  }
}
