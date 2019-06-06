package brownbag.DI.cake

import cats.effect._
import cats._
import cats.data._
import cats.implicits._

import brownbag.DI._

// checked at compile time
// singletons can be modelled as objects

// boilerplate
// weird startup errors if you aren't very careful
// can be confusing with implicits
// uses some lesser known Scala features (self types, sometimes bounds on typedefs), thus harder to understand

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
  self: DbComponent =>

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

object RealDb extends DB {
  override def incrementCounter(name: String): IO[Unit] = IO {}
}

trait DbComponent {
  def db: DB
}

trait RealDbComponent extends DbComponent {
  override val db = RealDb
}

object Main {

  def main(args: Array[String]): Unit = {

    // setup
    object VotingServiceImpl
        extends VotingService
        with RealVoterCheckComponent
        with RealVoteStoreComponent
        with RealDbComponent

    // run
    VotingServiceImpl
      .vote("THE BREXIT PARTY", Voter("Kim Stebel"))
      .unsafeRunSync()
  }
}

trait FakeVoterCheckComponent extends VoterCheckComponent {
  override def voterCheck: VoterCheck = ???
}

trait FakeVoteStoreComponent extends VoteStoreComponent {
  override def voteStore: VoteStore = ???
}

object Test {

  //setup
  object VotingServiceWithTestDeps
      extends VotingService
      with FakeVoterCheckComponent
      with FakeVoteStoreComponent

}
