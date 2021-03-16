package helpers.traits

import scala.reflect.ClassTag

abstract class RepositorySpec[T: ClassTag] extends UnitSpec with HasDB {
  final lazy val repository: T = app.injector.instanceOf[T]
}
