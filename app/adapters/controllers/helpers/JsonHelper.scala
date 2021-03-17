package adapters.controllers.helpers

import adapters._
import play.mvc.Http.MimeTypes.JSON
import io.circe.Json
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._
import play.api.mvc.{Result, Results}
import play.mvc.Http.Status

object JsonHelper extends JsonHelper

trait JsonHelper {
  def responseError(e: AdapterError): Result = {
    val res = ErrorResponse(e.getMessage.trim).asJson.noSpaces
    e match {
      case _: BadRequestError     => Results.Status(Status.BAD_REQUEST)(res)
      case _: NotFoundError       => Results.Status(Status.BAD_REQUEST)(res)
      case _: InternalServerError =>
        Results.Status(Status.INTERNAL_SERVER_ERROR)(res)
    }
  }

  def responseSuccess(status: Results.Status): Json => Result = { data: Json =>
    status(Json.obj("data" -> data).noSpaces)
      .as(JSON)
      .withHeaders("Access-Control-Allow-Origin" -> "*")
  }
}

final case class ErrorResponse(message: String)
