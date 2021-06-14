package domains

import domains.application.Application._
import helpers.traits.ModelSpec
import org.scalacheck.Gen

class ApplicationDomainSpec extends ModelSpec {
  "ApplicationId.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = ApplicationId.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = ApplicationId.create("")
        assert(result.leftSide === Left(EmptyStringError("ApplicationId")))
      }
    }
  }

  "ApplicationName.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = ApplicationName.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = ApplicationName.create("")
        assert(result.leftSide === Left(EmptyStringError("ApplicationName")))
      }
    }
  }

  "ApplicationClientId.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = ApplicationClientId.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = ApplicationClientId.create("")
        assert(
          result.leftSide === Left(EmptyStringError("ApplicationClientId"))
        )
      }
    }
  }

  "ApplicationClientSecret.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = ApplicationClientSecret.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = ApplicationClientSecret.create("")
        assert(
          result.leftSide === Left(EmptyStringError("ApplicationClientSecret"))
        )
      }
    }
  }

  "Application.updateClientInfo" should {
    "return Application model which client info updated" in {
      forAll(
        applicationGen,
        Gen.option(applicationClientIdGen),
        Gen.option(applicationClientSecretGen)
      ) { (model, id, secret) =>
        val result = model.updateClientInfo(id, secret)

        assert(result.clientId === id)
        assert(result.clientSecret === secret)
      }
    }
  }
}
