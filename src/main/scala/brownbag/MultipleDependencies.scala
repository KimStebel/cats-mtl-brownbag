package brownbag

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

trait Injector[F[+_], +E] extends Serializable {
  def ask: F[E]
}

trait Metrics[F[_]] {
  def incrementCounter(name: String): F[Unit]
}

trait Console[F[_]] {
  def println(s: String): F[Unit]
}

trait DB[F[+_]] {
  def get(implicit aa: Injector[F, Metrics[F]]): F[String]
}

object MultipleDependencies extends IOApp {

  type ProgramEnv[F[+_]] = Metrics[F] with Console[F] with DB[F]

  private def program[F[+_]](implicit m: Monad[F], i: Injector[F, Console[F] with DB[F] with Metrics[F]]): F[Unit] = for {
    services <- i.ask
    data <- services.get
    _ <- services.println(data)
  } yield ()


  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- IO(println("foo"))
  } yield ExitCode.Success
}
