package doobie.util

import doobie.util.lens._

import org.specs2.mutable.Specification

#+scalaz
import scalaz.State
#-scalaz
#+cats
import cats.data.State
#-cats

object lensspec extends Specification {

  case class Name(first: String, last: String)
  object Name {
    val first: Name @> String = Lens(_.first, (a, b) => a.copy(first = b))
    val last:  Name @> String = Lens(_.last, (a, b) => a.copy(last = b))
  }

  case class Address(name: Name, street: String)
  object Address {
    val name:   Address @> Name   = Lens(_.name, (a, b) => a.copy(name = b))
    val street: Address @> String = Lens(_.street, (a, b) => a.copy(street = b))
    val first:  Address @> String = name >=> Name.first
    val last:   Address @> String = name >=> Name.last
  }

  val bob = Address(Name("Bob", "Dole"), "123 Foo St.")

  def exec[S](st: State[S, _], s: S): S =
#+cats
    st.runS(s).value
#-cats
#+scalaz
    st.exec(s)
#-scalaz

  import Address._

  "lens" should {

    "modify ok" in {
      val prog: State[Address, Unit] =
        for {
          _ <- first  %= (_.toUpperCase)
          _ <- last   %= (_.toLowerCase)
          _ <- street %= (_.replace('o', '*'))
        } yield ()
      exec(prog, bob) must_== Address(Name("BOB", "dole"), "123 F** St.")
    }

    "set ok" in {
      val prog: State[Address, Unit] =
        for {
        _ <- first  := "Jimmy"
        _ <- last   := "Carter"
        _ <- street := "12 Peanut Dr."
      } yield ()
      exec(prog, bob) must_== Address(Name("Jimmy", "Carter"), "12 Peanut Dr.")
    }

  }

}
