package usecases

import domains.bot.BotRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import org.scalacheck.Gen
import usecases.UpdateBotClientInfoUseCase._

import scala.concurrent.Future

class UpdateBotClientInfoUseCaseSpec extends UseCaseSpec {
  val repo = mock[BotRepository]

  "exec" when {
    "succeed" should {
      "invoke BotRepository.find & update once" in {
        forAll(
          botGen,
          Gen.option(botClientIdGen),
          Gen.option(botClientSecretGen)
        ) { (model, clientId, secret) =>
          val params  = Params(model.id, clientId, secret)
          val updated = model.updateClientInfo(clientId, secret)

          when(repo.find(model.id)).thenReturn(Future.successful(Some(model)))
          when(repo.update(updated)).thenReturn(Future.unit)

          new UpdateBotClientInfoUseCaseImpl(repo).exec(params).futureValue

          verify(repo).find(model.id)
          verify(repo).update(updated)

          reset(repo)
        }
      }
    }

    "find failed" should {
      "return UseCaseError & never invoke update" in {
        forAll(
          botGen,
          Gen.option(botClientIdGen),
          Gen.option(botClientSecretGen)
        ) { (model, clientId, secret) =>
          val params  = Params(model.id, clientId, secret)
          val updated = model.updateClientInfo(clientId, secret)

          when(repo.find(model.id)).thenReturn(Future.successful(None))

          val result = new UpdateBotClientInfoUseCaseImpl(repo).exec(params)

          val msg = """
              |SystemError
              |error while botRepository.find in update bot client info use case
              |DBError
              |error
              |""".stripMargin.trim

          whenReady(result.failed)(e => assert(e.getMessage.trim === msg))

          verify(repo, never).update(updated)

          reset(repo)
        }
      }
    }

    "update failed" should {
      "return UseCaseError & invoke BotRepository.find & update once" in {
        forAll(
          botGen,
          Gen.option(botClientIdGen),
          Gen.option(botClientSecretGen)
        ) { (model, clientId, secret) =>
          val params  = Params(model.id, clientId, secret)
          val updated = model.updateClientInfo(clientId, secret)

          when(repo.find(model.id)).thenReturn(Future.successful(Some(model)))
          when(repo.update(updated)).thenReturn(Future.failed(DBError("error")))

          val result = new UpdateBotClientInfoUseCaseImpl(repo).exec(params)

          val msg = """
              |SystemError
              |error while botRepository.update in update bot client info use case
              |DBError
              |error
              |""".stripMargin.trim

          whenReady(result.failed) { e =>
            verify(repo).find(model.id)
            verify(repo).update(updated)
            assert(e.getMessage.trim === msg)
          }

          reset(repo)
        }
      }
    }
  }
}
