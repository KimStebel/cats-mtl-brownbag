package brownbag

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

class Agreement
class PassedAgreement
trait BritainIsGreatAgain
case object NuclearWar extends BritainIsGreatAgain
case object TradeDeal extends BritainIsGreatAgain

object MonadTransformers {

  trait Dependencies {
    def makeAgreement(): IO[Either[Exception, Agreement]]
    def passAgreement(a: Agreement): IO[PassedAgreement]
    def negotiateTrade(pA: PassedAgreement): IO[Option[BritainIsGreatAgain]]
    def nukeThem(): IO[BritainIsGreatAgain]
  }

  // do the three steps, handle any error by nuking everyone
  def brexit(deps: Dependencies): IO[Either[Exception, BritainIsGreatAgain]] = {
    ???
  }

}