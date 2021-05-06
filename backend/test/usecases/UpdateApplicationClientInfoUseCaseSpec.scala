package usecases

import domains.application.ApplicationRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import org.scalacheck.Gen
import usecases.UpdateApplicationClientInfoUseCase._

import scala.concurrent.Future

class UpdateApplicationClientInfoUseCaseSpec extends UseCaseSpec {
  val repo = mock[ApplicationRepository]

  "exec" when {
    "succeed" should {
      "invoke ApplicationRepository.find & update once" in {
        forAll(
          applicationGen,
          Gen.option(applicationClientIdGen),
          Gen.option(applicationClientSecretGen)
        ) { (model, clientId, secret) =>
          val params  = Params(model.id, clientId, secret)
          val updated = model.updateClientInfo(clientId, secret)

          when(repo.find(model.id)).thenReturn(Future.successful(Some(model)))
          when(repo.update(updated)).thenReturn(Future.unit)

          new UpdateApplicationClientInfoUseCaseImpl(repo)
            .exec(params)
            .futureValue

          verify(repo).find(model.id)
          verify(repo).update(updated)

          reset(repo)
        }
      }
    }

    "find failed" should {
      "return UseCaseError & never invoke update" in {
        forAll(
          applicationGen,
          Gen.option(applicationClientIdGen),
          Gen.option(applicationClientSecretGen)
        ) { (model, clientId, secret) =>
          val params  = Params(model.id, clientId, secret)
          val updated = model.updateClientInfo(clientId, secret)

          when(repo.find(model.id)).thenReturn(Future.successful(None))

          val result =
            new UpdateApplicationClientInfoUseCaseImpl(repo).exec(params)

          whenReady(result.failed)(e =>
            assert(
              e === NotFoundError(
                "error while applicationRepository.find in update application client info use case"
              )
            )
          )

          verify(repo, never).update(updated)

          reset(repo)
        }
      }
    }

    "update failed" should {
      "return UseCaseError & invoke ApplicationRepository.find & update once" in {
        forAll(
          applicationGen,
          Gen.option(applicationClientIdGen),
          Gen.option(applicationClientSecretGen)
        ) { (model, clientId, secret) =>
          val params  = Params(model.id, clientId, secret)
          val updated = model.updateClientInfo(clientId, secret)

          when(repo.find(model.id)).thenReturn(Future.successful(Some(model)))
          when(repo.update(updated)).thenReturn(Future.failed(DBError("error")))

          val result =
            new UpdateApplicationClientInfoUseCaseImpl(repo).exec(params)

          val msg = """
              |SystemError
              |error while applicationRepository.update in update application client info use case
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
