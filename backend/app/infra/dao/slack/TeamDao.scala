package infra.dao.slack

import com.google.inject.Inject
import infra.dao.ApiDao
import infra.dao.slack.TeamDaoImpl.InfoResponse
import play.api.libs.ws.WSClient
import infra.syntax.all._
import io.circe.generic.auto._
import io.circe._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}

trait TeamDao {
  def info(toke: String): Future[InfoResponse]
}

class TeamDaoImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext)
    extends ApiDao(ws) with TeamDao {
  def info(token: String): Future[InfoResponse] = {
    val url = "https://slack.com/api/team.info"
    (for {
      res <- ws.url(url)
               .withHttpHeaders("Authorization" -> s"Bearer $token")
               .get()
               .ifFailedThenToInfraError(s"error while getting $url")
               .map(_.json.toString)
    } yield decode[InfoResponse](res))
      .ifLeftThenToInfraError("error while converting info api response")
  }
}

object TeamDaoImpl {
  final case class InfoResponse(team: Team)
  final case class Team(id: String)
}
