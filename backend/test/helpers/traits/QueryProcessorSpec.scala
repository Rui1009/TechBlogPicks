package helpers.traits

import scala.reflect.ClassTag

abstract class QueryProcessorSpec[T: ClassTag] extends UnitSpec with HasDB {
  final lazy val queryProcessor: T = app.injector.instanceOf[T]
}
