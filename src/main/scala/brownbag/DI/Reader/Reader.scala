package brownbag.DI.Reader

import cats.effect._
import cats.data.ReaderT
import brownbag.DI._
import cats.syntax._
import cats.instances._
import cats.data.Kleisli

object VotingService {

  type IoReader[D, R] = ReaderT[IO, D, R]

  def vote(partyName: String, voter: Voter): IoReader[VoteStore, Unit] = {

    def fromF[F[_], A](a: F[A]): ReaderT[F, VoteStore, A] = ReaderT(_ => a)

    def ask[X]: IoReader[X, X] = ReaderT.ask[IO, X]

    for {
      vs <- ask
      _ <- fromF(vs.store(partyName))
    } yield ()
  }
}

object Main {
  def main = {
    VotingService.vote("BREXIT PARTY", Voter("Kim"))
  }
}
