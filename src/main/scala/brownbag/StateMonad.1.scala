package brownbag

import cats._
import cats.data._

// individual syntax imports, avoids weird errors about diverging implicits
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._

import scala.annotation.tailrec
import org.scalactic.Bool

case class Box[A](a: A)

object Box {
  implicit val boxMonad: Monad[Box] = new Monad[Box] {
    override def flatMap[A, B](box: Box[A])(f: A => Box[B]): Box[B] = f(box.a)
    override def pure[A](x: A): Box[A] = Box(x)

    override def tailRecM[A, B](a: A)(f: A => Box[Either[A, B]]): Box[B] = ???
  }
}

case class MyState[S, +R](val run: S => (S, R))

/*
def insertItems(items: List[String]): Int = {
  store = items ++ items
  store.size
}
 */

object MyState {

  class Helpers[S] {
    def doNothing = ???
    def set(s: S): MyState[S, Unit] = ???
    def modify(f: S => S): MyState[S, Unit] = ???
  }

  // ? is _ on the type level
  implicit def stateMonad[S]: Monad[MyState[S, ?]] = new Monad[MyState[S, ?]] {
    override def flatMap[A, B](fa: MyState[S, A])(
        f: A => MyState[S, B]
    ): MyState[S, B] = {
      val res: S => (S, B) = initialState => {
        val (intermediateState, firstResult) = fa.run(initialState)
        f(firstResult).run(intermediateState)
      }
      MyState(res)
    }
    override def pure[A](a: A): MyState[S, A] = MyState(s => (s, a))

    override def tailRecM[A, B](a: A)(
        f: A => MyState[S, Either[A, B]]
    ): MyState[S, B] = ???
  }
  //todo: tests for monad laws
}

// val plus: Int => Int => Int = _ + _
// val plus2: Int => Int = plus(2)

// MyState : * -> * -> *
// MyState[Int, ?] : * -> *

object MyStateTest extends App {
  case class EmployeeCompensation(
      base: Int,
      bonus: Int,
      overtimeHours: Int,
      overtimeRate: Int,
      female: Boolean
  )
  val comp = EmployeeCompensation(50000, 5000, 50, 40, true)

  def total(comp: EmployeeCompensation) = {
    val myState = new MyState.Helpers[Int]
    import myState._
    import comp._

    def add(amount: Int) = modify(_ + amount)

    import cats.syntax.functor._
    import cats.instances.option._
    import cats.instances.either._
    import cats.syntax.either._

    // a very imperative program
    for {
      _ <- {
        val x = add(base)
        x
      }
      _ = println("added base pay")
      _ <- add(bonus)
      _ = println("added bonus")
      _ <- add(overtimeHours * overtimeRate)
      _ = println("added overtime")
      _ <- {
        if (female) {
          modify(_ / 2)
        } else {
          doNothing
        }
      }
    } yield ()
  }

  val program = total(comp)
  // nothing happens until we run it
  val (finalState, _) = program.run(0) // pass in initial state
  println(finalState)
}
