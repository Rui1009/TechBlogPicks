package infra.dao

import io.circe.Json
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class ApiDao(ws: WSClient)(implicit ec: ExecutionContext) {
  private lazy val logger = Logger(this.getClass)

  def notifyError(error: String): Future[Unit] = {
    logger.error(error)
    (for {
      url <- sys.env.get("ERROR_NOTIFICATION_URL")
    } yield for {
      _ <- ws.url(url).post(Json.obj("text" -> Json.fromString(error)).noSpaces)
    } yield ()) match {
      case Some(v) => v
      case None    =>
        logger.warn("can not specify ERROR_NOTIFICATION_URL")
        Future.unit
    }
  }

  implicit class DecodedResOps[E <: Throwable, T](
    futureEither: Future[Either[E, T]]
  ) {
    def anywaySuccess(emptyRes: T): Future[T] = futureEither.transformWith {
      case Success(Right(v)) => Future.successful(v)
      case Success(Left(e))  => notifyError(e.getMessage.trim).map(_ => emptyRes)
      case Failure(e)        => notifyError(e.getMessage.trim).map(_ => emptyRes)
    }
  }
}
