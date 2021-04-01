package adapters.controllers.bot

import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import adapters.{AdapterError, BadRequestError}
import cats.data.ValidatedNel
import cats.implicits.catsSyntaxEither
import cats.syntax.apply._
import com.google.inject.Inject
import domains.bot.Bot.BotId
import domains.workspace.WorkSpace.WorkSpaceTemporaryOauthCode
import domains.{DomainError, EmptyStringError}
import io.circe.generic.auto._
import play.api.Logger
import play.api.mvc.{
  Action,
  AnyContent,
  BaseController,
  ControllerComponents,
  Result
}
import query.bots.BotsQueryProcessor
import usecases.InstallBotUseCase.Params
import usecases.{InstallBotUseCase, UpdateBotClientInfoUseCase}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

class BotController @Inject() (
  val controllerComponents: ControllerComponents,
  installBotUseCase: InstallBotUseCase,
  botsQueryProcessor: BotsQueryProcessor,
  updateBotClientInfoUseCase: UpdateBotClientInfoUseCase
)(implicit val ec: ExecutionContext)
    extends BaseController with JsonHelper with FutureSyntax
    with UpdateClientInfoBodyMapper with UninstallBotBodyMapper {

  private lazy val logger = Logger(this.getClass)

  def install(code: String, bot_id: String): Action[AnyContent] =
    Action.async { implicit request =>
      val tempOauthCode
        : ValidatedNel[EmptyStringError, WorkSpaceTemporaryOauthCode] =
        WorkSpaceTemporaryOauthCode.create(code).toValidatedNel
      val botId: ValidatedNel[EmptyStringError, BotId]                =
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
              .transformWith[Result]({
                case Success(_) =>
                  Future.successful(Redirect("https://google.com"))
                case Failure(e) =>
                  logger.error(e.toString)
                  Future.successful(Redirect("https://yahoo.com"))
              })
              .recoverError
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
