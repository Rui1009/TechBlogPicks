package infra.queryprocessorimpl

import com.google.inject.Inject
import infra.dao.slack.UsersDao
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import query.bots.{BotsQueryProcessor, BotsView}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

class BotsQueryProcessorImpl @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val usersDao: UsersDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile]
    with BotsQueryProcessor
    with API {
  override def findAll: Future[Seq[BotsView]] =
    (for {
      res <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
    } yield
      for {
        member <- res.members.filter(_.isBot)
      } yield BotsView(member.id, member.name))
      .ifFailedThenToInfraError("error while BotsQueryProcessorImpl.findAll")
}
