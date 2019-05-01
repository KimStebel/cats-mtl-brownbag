package brownbag.DI.CI

import cats.effect._
import cats._
import cats.data._
import cats.implicits._

import brownbag.DI._

class VotingService(voteStore: VoteStore, voterCheck: VoterCheck) {
  def vote(partyName: String, voter: Voter): IO[Unit] =
    for {
      checkResult <- voterCheck.check(voter)
      _ <- if (checkResult) voteStore.store(partyName) >> IO {
        println("vote counted")
      } else IO { println("not allowed to vote") }
    } yield ()
}

class RealVoteStore(db: DB) extends VoteStore {
  override def store(partyName: String): IO[Unit] = IO.pure(())
}

class RealVoterCheck extends VoterCheck {
  override def check(voter: Voter): IO[Boolean] = {
    IO.pure(true)
  }
}

class RealDb extends DB {
  override def incrementCounter(name: String): IO[Unit] = IO.pure(())
}

object Main {

  def main(args: Array[String]): Unit = {
    // setup
    val votingService =
      new VotingService(
        new RealVoteStore(new RealDb),
        new RealVoterCheck
      )
    // run
    votingService.vote("THE BREXIT PARTY", Voter("Kim Stebel")).unsafeRunSync()
  }
}
