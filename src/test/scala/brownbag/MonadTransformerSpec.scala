package brownbag

import collection.mutable.Stack
import org.scalatest._
import MonadTransformers._
import cats.effect.IO

class MonadTransformerSpec extends FlatSpec with Matchers {

  "brexit" should "succeed and not nuke anyone if all steps succeed" in {
    val deps = new Dependencies {
      override def makeAgreement(): IO[Either[Exception, Agreement]] = IO.pure(Right(new Agreement))
      override def passAgreement(a: Agreement): IO[PassedAgreement] = IO.pure(new PassedAgreement)
      override def negotiateTrade(pA: PassedAgreement): IO[Option[BritainIsGreatAgain]] = IO.pure(Some(TradeDeal))
      override def nukeThem(): IO[BritainIsGreatAgain] = IO(fail("uh oh")) // shouldn't be called
    }
    MonadTransformers.brexit(deps).unsafeRunSync() should be a 'right
  }

  it should "nuke everyone if agreement can't be made" in {
    val deps = new Dependencies {
      override def makeAgreement(): IO[Either[Exception, Agreement]] = IO.pure(Left(new Exception()))
      override def passAgreement(a: Agreement): IO[PassedAgreement] = IO.pure(new PassedAgreement)
      override def negotiateTrade(pA: PassedAgreement): IO[Option[BritainIsGreatAgain]] = IO.pure(Some(TradeDeal))
      override def nukeThem(): IO[BritainIsGreatAgain] = IO.pure(NuclearWar)
    }
    MonadTransformers.brexit(deps).unsafeRunSync() should be(Right(NuclearWar))
  }

  it should "nuke everyone if no trade deal can be negotiated" in {
    val deps = new Dependencies {
      override def makeAgreement(): IO[Either[Exception, Agreement]] = IO.pure(Right(new Agreement))
      override def passAgreement(a: Agreement): IO[PassedAgreement] = IO.pure(new PassedAgreement)
      override def negotiateTrade(pA: PassedAgreement): IO[Option[BritainIsGreatAgain]] = IO.pure(None)
      override def nukeThem(): IO[BritainIsGreatAgain] = IO.pure(NuclearWar)
    }
    MonadTransformers.brexit(deps).unsafeRunSync() should be(Right(NuclearWar))
  }
}
