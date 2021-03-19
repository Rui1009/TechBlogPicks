package adapters.controllers.helpers

import adapters.AdapterError
import io.circe.Decoder
import play.api.libs.circe.Circe
import play.api.mvc.BodyParser

import scala.concurrent.ExecutionContext

trait JsonRequestMapper extends Circe {
  def mapToValueObject[B, C](f: B => Either[AdapterError, C])(implicit
    rds: Decoder[B],
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, C]] = circe.json[B].map(f)
}
