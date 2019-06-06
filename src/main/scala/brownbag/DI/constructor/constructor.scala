package brownbag.DI.constructor

import cats.effect._
import cats.Id
import cats.data.ReaderT
import brownbag.DI._
import cats.syntax.flatMap._

// checked at compile time
// familiar to OO developers

// dependencies need to be passed around explicitly
// all methods in a class get the same dependencies, no easy way to see what "effects" a method might have
// requires use `class` where you could otherwise use `object`

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

class FakeVoteStore extends VoteStore {
  override def store(partyName: String): IO[Unit] = ???
}

class FakeVoterCheck extends VoterCheck {
  override def check(voter: Voter): IO[Boolean] = ???
}

object Test {
  val votingServiceWithTestDeps =
    new VotingService(new FakeVoteStore, new FakeVoterCheck)
}
