package helpers.traits

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

trait UnitSpec
    extends AnyWordSpec
    with ScalaFutures
    with OptionValues
    with EitherValues
    with BeforeAndAfter
    with Inside
    with ScalaCheckDrivenPropertyChecks
