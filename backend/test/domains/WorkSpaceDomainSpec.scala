package domains

import domains.workspace.WorkSpace._
import helpers.traits.ModelSpec
import cats.syntax.either._

class WorkSpaceDomainSpec extends ModelSpec {
  "WorkSpaceId.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = WorkSpaceId.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = WorkSpaceId.create("")
        assert(result.leftSide == EmptyStringError("WorkSpaceId").asLeft)
      }
    }
  }

  "WorkSpaceTemporaryOauthCode.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = WorkSpaceTemporaryOauthCode.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = WorkSpaceTemporaryOauthCode.create("")
        assert(result.leftSide == EmptyStringError("temporaryOauthCode").asLeft)
      }
    }
  }

  "WorkSpace.installApplication" should {
    "return WorkSpace which bots are updated" in {
      forAll(workSpaceGen, botGen)((workSpace, bot) => pending)
    }
  }
}
