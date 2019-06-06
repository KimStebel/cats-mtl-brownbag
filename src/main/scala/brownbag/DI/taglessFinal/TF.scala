package brownbag.DI.taglessFinal

import cats.effect._
import cats.Id
import brownbag.DI.Voter

import cats._
import cats.data._
import cats.instances._
import cats.syntax.flatMap._
import cats.syntax.applicative._
import cats.syntax.functor._

trait VoteStore[F[_]] {
  def store(partyName: String): F[Unit]
}
trait VoterCheck[F[_]] {
  def check(voter: Voter): F[Boolean]
}
trait DB[F[_]] {
  def incrementCounter(name: String): F[Unit]
}
trait Logger[F[_]] {
  def info(message: String): F[Unit]
}

object VotingService {

  def vote[F[_]](
      partyName: String,
      voter: Voter
  )(
      implicit vc: VoterCheck[F],
      vs: VoteStore[F],
      logger: Logger[F],
      monad: Monad[F]
  ): F[Unit] =
    for {
      checkResult <- vc.check(voter)
      _ <- if (checkResult)
        vs.store(partyName) >> logger.info(s"vote counted for $partyName")
      else logger.info("not allowed to vote")
    } yield ()
}

object MyLogger extends Logger[IO] {
  def info(message: String): IO[Unit] = IO(println(message))
}

class RealVoteStore[F[_]: Monad](implicit db: DB[F]) extends VoteStore[F] {
  override def store(
      partyName: String
  ): F[Unit] = ().pure[F]
}

class RealVoterCheck[F[_]](implicit F: Monad[F]) extends VoterCheck[F] {
  override def check(voter: Voter): F[Boolean] = {
    F.pure(true)
  }
}

class RealDb[F[_]](implicit F: Monad[F]) extends DB[F] {
  override def incrementCounter(name: String): F[Unit] =
    F.pure(())
}

object Main {

  def main(args: Array[String]): Unit = {
    // setup
    implicit val db = new RealDb[IO]
    implicit val voteStore = new RealVoteStore[IO]
    implicit val voterCheck = new RealVoterCheck[IO]
    implicit val logger = MyLogger
    // run
    val io = VotingService
      .vote("THE BREXIT PARTY", Voter("Kim Stebel"))
      .unsafeRunSync()
  }
}

object Test extends App {

  type TestMonad[A] = State[TestState, A]

  case class TestState(votes: Map[String, Int], logs: List[String])

  implicit object FakeVoterCheck extends VoterCheck[TestMonad] {
    def check(voter: Voter): TestMonad[Boolean] = true.pure[TestMonad]
  }

  implicit object FakeVoteStore extends VoteStore[TestMonad] {
    def store(partyName: String): TestMonad[Unit] = {
      State.modify {
        case TestState(votes, logs) => {
          val count = votes.get(partyName).getOrElse(0)
          val newVotes = votes + (partyName -> (count + 1))
          TestState(newVotes, logs)
        }
      }
    }
  }

  implicit object FakeLogger extends Logger[TestMonad] {
    def info(message: String): TestMonad[Unit] = State.modify {
      case TestState(votes, logs) => {
        TestState(votes, message :: logs)
      }
    }
  }

  val result = VotingService
    .vote[TestMonad]("TORIES", Voter("Mark Garland"))
    .run(TestState(Map("LABOUR" -> 100), Nil))
    .value
  println(result)
}
