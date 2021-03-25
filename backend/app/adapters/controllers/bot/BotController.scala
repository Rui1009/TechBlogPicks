package adapters.controllers.bot

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import cats.data.ValidatedNel
import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import usecases.{
  InstallBotUseCase,
  UninstallBotUseCase,
  UpdateBotClientInfoUseCase
}
import usecases.InstallBotUseCase.Params
import cats.syntax.apply._
import cats.implicits.catsSyntaxEither
import domains.{DomainError, EmptyStringError}
import domains.bot.Bot.BotId
import query.bots.BotsQueryProcessor
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class BotController @Inject() (
  val controllerComponents: ControllerComponents,
  installBotUseCase: InstallBotUseCase,
  botsQueryProcessor: BotsQueryProcessor,
  updateBotClientInfoUseCase: UpdateBotClientInfoUseCase,
  uninstallBotUseCase: UninstallBotUseCase
)(implicit val ec: ExecutionContext)
    extends BaseController with JsonHelper with FutureSyntax
    with UpdateClientInfoBodyMapper with UninstallBotBodyMapper {
  def install(code: String, bot_id: String): Action[AnyContent] =
    Action.async { implicit request =>
      val tempOauthCode: ValidatedNel[
        EmptyStringError,
        AccessTokenPublisherTemporaryOauthCode
      ]                                                = AccessTokenPublisherTemporaryOauthCode.create(code).toValidatedNel
      val botId: ValidatedNel[EmptyStringError, BotId] =
        BotId.create(bot_id).toValidatedNel
      (tempOauthCode, botId)
        .mapN((_, _))
        .toEither
        .leftMap(errors =>
          BadRequestError(
            errors
              .foldLeft("")((acc, cur: DomainError) => acc + cur.errorMessage)
          )
        )
        .fold(
          e => Future.successful(responseError(e)),
          tuple =>
            installBotUseCase
              .exec(Params(tuple._1, tuple._2))
              .ifFailedThenToAdapterError("error in BotController.install")
              .toSuccessGetResponse
              .recoverError
        )
    }

  def uninstall: Action[Either[AdapterError, UninstallBotCommand]] =
    Action.async(mapToUninstallBotCommand) { implicit request =>
      request.body.fold(
        e => Future.successful(responseError(e)),
        body =>
          uninstallBotUseCase
            .exec(UninstallBotUseCase.Params(body.token))
            .ifFailedThenToAdapterError("error in BotController.uninstall")
      )
    }

  def index: Action[AnyContent] = Action.async {
    botsQueryProcessor.findAll
      .ifFailedThenToAdapterError("error in BotController.index")
      .toSuccessGetResponse
      .recoverError
  }

  def update(
    id: String
  ): Action[Either[AdapterError, UpdateClientInfoCommand]] =
    Action.async(mapToUpdateClientInfoCommand) { implicit request =>
      BotId
        .create(id)
        .fold(
          e =>
            Future.successful(responseError(BadRequestError(e.errorMessage))),
          botId =>
            request.body.fold(
              e => Future.successful(responseError(e)),
              body =>
                updateBotClientInfoUseCase
                  .exec(
                    UpdateBotClientInfoUseCase
                      .Params(botId, body.clientId, body.clientSecret)
                  )
                  .ifFailedThenToAdapterError("error in BotController.update")
                  .toSuccessPostResponse
                  .recoverError
            )
        )
    }
}
