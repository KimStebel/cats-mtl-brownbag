// variance: .widen, .asRight/.asLeft
import cats._

class Test[F[_]: Monad] {
  import cats.implicits._

  val x: F[Either[String, Int]] = {
    val res = 4.asRight.pure[F]
    res.widen // or remove res
  }

}

// reduce cats imports when you see "diverging implicit expansion...."
// instead of importing cats.implicits._, import instances and syntax, e.g.
// import cats.syntax.flatmap._ for flatmap and for comprehensions
// import cats.syntax.applicative._ for pure
// import cats.syntax.functor._ for map and for comprehensions
// import cats.instances.all._ all instances without the syntax
