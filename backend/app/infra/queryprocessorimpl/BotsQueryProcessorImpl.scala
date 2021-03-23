package infra.queryprocessorimpl

import com.google.inject.Inject
import infra.dao.slack.UsersDao
import infra.dto.Tables._
import infra.syntax.all._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import query.bots.{BotsQueryProcessor, BotsView}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API

import scala.concurrent.{ExecutionContext, Future}

class BotsQueryProcessorImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val usersDao: UsersDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with BotsQueryProcessor
    with API {
  override def findAll: Future[Seq[BotsView]] = (for {
    res <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
  } yield for {
    member <- res.members.filter(m => m.isBot && !m.deleted)
  } yield for {
    clientInfo <-
      db.run(BotClientInfo.findBy(_.botId).apply(member.id).result.headOption)
  } yield BotsView(
    member.id,
    member.name,
    clientInfo.flatMap(_.clientId),
    clientInfo.flatMap(_.clientSecret)
  )).flatMap(Future.sequence(_))
    .ifFailedThenToInfraError("error while BotsQueryProcessorImpl.findAll")
}
