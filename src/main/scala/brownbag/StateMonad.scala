// package brownbag

// import cats._
// import cats.data._
// import cats.syntax.flatMap._
// import cats.syntax.functor._
// import cats.syntax.applicative._
// import scala.annotation.tailrec
// import org.scalactic.Bool

// case class MyState[S, +R](val run: S => (S, R))

// object MyState {

//   class Helpers[S] {
//     def doNothing = stateMonad[S].pure(())
//     def set(s: S): MyState[S, Unit] = MyState[S, Unit](_ => (s, ()))
//     def modify(f: S => S): MyState[S, Unit] =
//       MyState[S, Unit](oldState => (f(oldState), ()))
//   }

//   implicit def stateMonad[S]: Monad[MyState[S, ?]] = new Monad[MyState[S, ?]] {

//     override def flatMap[A, B](fa: MyState[S, A])(
//         f: A => MyState[S, B]
//     ): MyState[S, B] =
//       MyState[S, B](s => {
//         val (newState, a) = fa.run(s)
//         f(a).run(newState)
//       })

//     override def pure[A](x: A): MyState[S, A] = MyState(s => (s, x))

//     override def tailRecM[A, B](a: A)(
//         ff: A => MyState[S, Either[A, B]]
//     ): MyState[S, B] = {
//       @tailrec def loop(s: S, ab: Either[A, B]): (S, B) = {
//         ab match {
//           case Left(a) => {
//             val (newS, ab) = ff(a).run(s)
//             loop(newS, ab)
//           }
//           case Right(b) => s -> b
//         }
//       }

//       MyState[S, B](s => loop(s, Left(a)))
//     }
//   }
// }

// object MyStateTest extends App {
//   case class EmployeeCompensation(
//       base: Int,
//       bonus: Int,
//       overtimeHours: Int,
//       overtimeRate: Int,
//       female: Boolean
//   )
//   val comp = EmployeeCompensation(50000, 5000, 50, 40, true)

//   def total(comp: EmployeeCompensation) = {
//     val myState = new MyState.Helpers[Int]
//     import myState._
//     import comp._

//     def add(amount: Int) = modify(_ + amount)

//     for {
//       _ <- add(base)
//       _ = println("added base pay")
//       _ <- add(bonus)
//       _ = println("added bonus")
//       _ <- add(overtimeHours * overtimeRate)
//       _ = println("added overtime")
//       _ <- {
//         if (female) {
//           modify(_ / 2)
//         } else {
//           doNothing
//         }
//       }
//     } yield ()
//   }

//   val program = total(comp)
//   val (finalState, _) = program.run(0)
//   println(finalState)
// }
