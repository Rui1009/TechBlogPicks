package adapters.controllers.helpers

import adapters.AdapterError
import io.circe.Decoder
import play.api.libs.circe.Circe
import play.api.mvc.BodyParser

import scala.concurrent.ExecutionContext

trait JsonRequestMapper[B, C] extends Circe {
  def mapToValueObject(body: B): Either[AdapterError, C]
  def mapToCommand(implicit
    de: Decoder[B],
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, C]] = circe.json[B].map(mapToValueObject)
}
