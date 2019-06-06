// package brownbag.DI.function

// import cats.effect._
// import cats._
// import cats.data._
// import cats.implicits._

// import brownbag.DI._

// // checked at compile time
// // you can use object for singletons

// // dependencies need to be passed around explicitly to every method

// object VotingService {
//   def vote(
//       partyName: String,
//       voter: Voter
//   )(voteStore: VoteStore, voterCheck: VoterCheck): IO[Unit] =
//     for {
//       checkResult <- voterCheck.check(voter)
//       _ <- if (checkResult) voteStore.store(partyName) >> IO {
//         println("vote counted")
//       } else IO { println("not allowed to vote") }
//     } yield ()
// }

// class RealVoteStore extends VoteStore {
//   override def store(partyName: String)(db: DB): IO[Unit] = IO.pure(())
// }

// class RealVoterCheck extends VoterCheck {
//   override def check(voter: Voter): IO[Boolean] = {
//     IO.pure(true)
//   }
// }

// class RealDb extends DB {
//   override def incrementCounter(name: String): IO[Unit] = IO.pure(())
// }

// object Main {

//   def main(args: Array[String]): Unit = {
//     // run
//     VotingService
//       .vote("THE BREXIT PARTY", Voter("Kim Stebel"))(
//         new RealVoteStore,
//         new RealVoterCheck
//       )
//       .unsafeRunSync()
//   }
// }

// class FakeVoteStore extends VoteStore {
//   override def store(partyName: String): IO[Unit] = ???
// }

// class FakeVoterCheck extends VoterCheck {
//   override def check(voter: Voter): IO[Boolean] = ???
// }

// object Test {}
