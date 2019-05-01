package brownbag.DI

import cats.effect._
import cats._
import cats.data._
import cats.implicits._

case class Voter(name: String)

trait VoteStore {
  def store(partyName: String): IO[Unit]
}
trait VoterCheck {
  def check(voter: Voter): IO[Boolean]
}
trait DB {
  def incrementCounter(name: String): IO[Unit]
}
