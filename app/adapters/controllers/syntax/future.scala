package adapters.controllers.syntax

import adapters.{AdapterError, InternalServerError}
import io.circe.Encoder
import io.circe.syntax._
import adapters.controllers.helpers.JsonHelper._
import play.api.mvc.{Result, Results}
import usecases.UseCaseError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object future extends FutureSyntax

trait FutureSyntax {
  implicit class FutureOps[T](val future: Future[T])(implicit
    ec: ExecutionContext
  ) {
    def toSuccessPostResponse(implicit encoder: Encoder[T]): Future[Result] =
      _toSuccessResponse(Results.Created)

    def ifFailedThenToAdapterError(message: String): Future[T] =
      future.transformWith {
        case Success(v)               => Future.successful(v)
        case Failure(e: UseCaseError) =>
          Future.failed(AdapterError.fromUseCaseError(message, e))
        case Failure(e)               => Future.failed(
            InternalServerError("\n" + message + "\n" + e.getMessage)
          )
      }

    private def _toSuccessResponse(
      status: Results.Status
    )(implicit enc: Encoder[T]): Future[Result] =
      future.map(value => responseSuccess(status)(value.asJson))
  }

  implicit class FutureResultOps[T <: Result](future: Future[T])(implicit
    ec: ExecutionContext
  ) {
    def recoverError: Future[Result] = future.recover { case e: AdapterError =>
      responseError(e)
    }
  }
}
