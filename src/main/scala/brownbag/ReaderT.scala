package brownbag

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import scala.util.Try

object ReaderTExamples extends App {

  case class Config(foo: String)
  val myConfig = Config("55")

  def doSomething = for {
    foo <- Reader[Config, String](config => config.foo)
    result = foo.toInt
  } yield result

  val result = doSomething.run(myConfig)

  println(result)



  val myConfigIO = IO.pure(myConfig)

  def doSomethingIO = for {
    config <- ReaderT.ask[IO, Config]
    result = config.foo.toInt
  } yield result
  myConfigIO.flatMap(config => doSomethingIO(config).map(println)).unsafeRunSync()



  def toInt(s: String): Either[Throwable, Int] = Try {
    s.toInt
  }.toEither

  type Error[R] = Either[Throwable, R]
  type IOError[R] = EitherT[IO, Throwable, R]
  type Stack[R] = ReaderT[IOError, Config, R]

  def fromError[R](e: Error[R]): Stack[R] = ReaderT[IOError, Config, R](_ => EitherT.fromEither[IO](e))

  def doSomethingIOEither = for {
    config <- ReaderT.ask[IOError, Config]
    result <- fromError(toInt(config.foo))
  } yield config.foo.toInt

  myConfigIO.flatMap(config => doSomethingIOEither.run(config).value).map(println).unsafeRunSync()



}
