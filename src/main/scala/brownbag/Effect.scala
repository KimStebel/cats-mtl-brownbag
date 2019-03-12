package brownbag

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Effect extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = IO {



    "" match {
      case s:String => for {
        _ <- IO { ??? }
        result <- IO { 5 }
      } yield result
    }




    def printHello = { println("hello cats") }

    val effect = IO("effect")
    val action = effect.flatMap(s => IO { println(s"Hello, cats $s!") })
    Thread.sleep(1000)
    println("nothing so far")
    action.unsafeRunSync()
    action.flatMap(_ => action).unsafeRunSync()
    IO {

    }
    (for {
      _ <- action
      _ <- action
    } yield ()).unsafeRunSync()
    IO(println("I want to be a future!")).unsafeToFuture()
    1.to(300).toList.parTraverse(s => IO(println(s"Hello, $s!"))).unsafeRunSync()

    println()
    println("and now with Future")
    println()

    val actionF = Future { println("Hello, cats effect!") }
    Thread.sleep(1000)
    println("nothing so far")
    Await.result(actionF, Duration.Inf)
    Await.result(actionF.flatMap(_ => actionF), Duration.Inf)
    Await.result((for {
      _ <- actionF
      _ <- actionF
    } yield ()), Duration.Inf)
    Await.result(Future(println("I want to be a future!")), Duration.Inf)
    Await.result(Future.traverse(1.to(3).toList)(s => Future(println(s"Hello, $s!"))), Duration.Inf)



    ExitCode.Success
  }

}
