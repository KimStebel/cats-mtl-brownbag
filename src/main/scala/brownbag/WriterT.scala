package brownbag

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

object WriterT extends App {

  val res = for {
    initial <- Writer.value[Vector[String], Int](1)
    _ <- Writer.tell(Vector("foo"))
    twice <- Writer.value[Vector[String], Int](2 * initial)
    _ <- Writer.tell(Vector("bar"))
  } yield twice

  println(res.run)
}
