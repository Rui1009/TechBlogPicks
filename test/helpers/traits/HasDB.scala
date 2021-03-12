package helpers.traits

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API

trait HasDB extends HasApplication with API {
  val profile = slick.jdbc.PostgresProfile

  final lazy val db =
    app.injector.instanceOf[DatabaseConfigProvider].get[PostgresProfile].db
}
