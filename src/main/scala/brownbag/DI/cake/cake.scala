package brownbag.DI.cake

import cats.effect._
import cats._
import cats.data._
import cats.implicits._

import brownbag.DI._

trait VotingService {
  self: VoteStoreComponent with VoterCheckComponent =>

  def vote(partyName: String, voter: Voter): IO[Unit] =
    for {
      checkResult <- voterCheck.check(voter)
      _ <- if (checkResult) voteStore.store(partyName) >> IO {
        println("vote counted")
      } else IO { println("not allowed to vote") }
    } yield ()
}

class RealVoteStore(db: DB) extends VoteStore {
  override def store(partyName: String): IO[Unit] = IO {}
}

trait VoteStoreComponent {
  def voteStore: VoteStore
}

trait RealVoteStoreComponent extends VoteStoreComponent {
  override val voteStore: VoteStore = new RealVoteStore(???)
}

class RealVoterCheck extends VoterCheck {
  override def check(voter: Voter): IO[Boolean] = {
    IO(true)
  }
}

trait VoterCheckComponent {
  def voterCheck: VoterCheck
}

trait RealVoterCheckComponent extends VoterCheckComponent {
  override val voterCheck: VoterCheck = new RealVoterCheck

}

class RealDb extends DB {
  override def incrementCounter(name: String): IO[Unit] = IO {}
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
