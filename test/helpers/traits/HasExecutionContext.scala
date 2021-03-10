package helpers.traits

import scala.concurrent.ExecutionContext

trait HasExecutionContext {
  implicit lazy val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
