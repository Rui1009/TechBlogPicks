package adapters.controllers.bot

import adapters.{BadRequestError}
import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import cats.data.ValidatedNel
import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import usecases.InstallBotUseCase
import usecases.InstallBotUseCase.Params
import cats.syntax.apply._
import cats.implicits.catsSyntaxEither
import domains.{DomainError, EmptyStringError}
import domains.bot.Bot.BotId

import scala.concurrent.{ExecutionContext, Future}

class BotController @Inject() (
   val controllerComponents: ControllerComponents,
   installBotUseCase: InstallBotUseCase
  )(implicit val ec: ExecutionContext) extends BaseController with JsonHelper with FutureSyntax {
  def install(code: String, bot_id: String): Action[AnyContent] =
    Action.async { implicit request =>
      val tempOauthCode: ValidatedNel[EmptyStringError, AccessTokenPublisherTemporaryOauthCode] = AccessTokenPublisherTemporaryOauthCode.create(code).toValidatedNel
      val botId: ValidatedNel[EmptyStringError, BotId] = BotId.create(bot_id).toValidatedNel
      (tempOauthCode, botId)
          .mapN((_, _))
          .toEither
          .leftMap(errors => BadRequestError(errors.foldLeft("")((acc, cur: DomainError) => acc + cur.errorMessage)))
          .fold(
            e => Future.successful(responseError(e)),
            tuple => installBotUseCase.exec(
              Params(
                tuple._1,
                tuple._2
              )
            )
              .ifFailedThenToAdapterError("error in BotController.install")
              .toSuccessGetResponse
              .recoverError
          )

    }

}
