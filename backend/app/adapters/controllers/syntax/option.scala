package adapters.controllers.syntax

import adapters.controllers.helpers.JsonHelper._
import io.circe.Json
import play.api.mvc.Result
import play.api.mvc.Results.Ok

import scala.concurrent.Future

object option extends OptionSyntax

trait OptionSyntax {
  implicit final def optionEitherSyntax(
    optFuture: Option[Future[Result]]
  ): OptionFutureOps = new OptionFutureOps(optFuture)
}

final private[syntax] class OptionFutureOps(
  val optFuture: Option[Future[Result]]
) extends AnyVal {
  def toSuccessResponseForEvent: Future[Result] = optFuture match {
    case Some(v) => v
    case None    => Future.successful(responseSuccess(Ok)(Json.Null))
  }
}
