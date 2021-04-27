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

  "WorkSpace.installBot" should {
    "return its token" in {
      forAll(workSpaceGen, botGen) { (workSpace, bot) =>
        val expected = workSpace.copy(botIds = workSpace.botIds :+ bot.id)

        assert(workSpace.install(bot) === expected)
      }
    }
  }

  "WorkSpace.uninstallBot" should {
    "return self with updated tokens & botIds" in {
      forAll(workSpaceGen, botGen, accessTokenGen) {
        (_workSpace, _bot, targetToken) =>
          val workSpace = _workSpace.copy(
            botIds = _workSpace.botIds :+ _bot.id,
            tokens = _workSpace.tokens :+ targetToken
          )
          val bot       = _bot.copy(accessTokens = _bot.accessTokens :+ targetToken)
          val result    = workSpace.uninstallBot(bot)

          assert(
            result === workSpace.copy(
              tokens = workSpace.tokens.diff(bot.accessTokens),
              botIds = workSpace.botIds.filter(id => id != bot.id)
            )
          )
      }
    }
  }
}
