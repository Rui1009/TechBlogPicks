package infra.lib

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API

trait HasDB extends HasDatabaseConfigProvider[PostgresProfile] with API
