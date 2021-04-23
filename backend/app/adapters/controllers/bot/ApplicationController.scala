package adapters.controllers.bot

import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import adapters.{AdapterError, BadRequestError}
import cats.data.ValidatedNel
import cats.implicits.catsSyntaxEither
import cats.syntax.apply._
import com.google.inject.Inject
import domains.application.Application.ApplicationId
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
import usecases.InstallApplicationUseCase.Params
import usecases.{InstallApplicationUseCase, UpdateApplicationClientInfoUseCase}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject() (
  val controllerComponents: ControllerComponents,
  installBotUseCase: InstallApplicationUseCase,
  botsQueryProcessor: BotsQueryProcessor,
  updateBotClientInfoUseCase: UpdateApplicationClientInfoUseCase
)(implicit val ec: ExecutionContext)
    extends BaseController with JsonHelper with FutureSyntax
    with UpdateClientInfoBodyMapper with UninstallApplicationBodyMapper {

  private lazy val logger = Logger(this.getClass)

  def install(code: String, application_id: String): Action[AnyContent] =
    Action.async { implicit request =>
      val tempOauthCode
        : ValidatedNel[EmptyStringError, WorkSpaceTemporaryOauthCode]  =
        WorkSpaceTemporaryOauthCode.create(code).toValidatedNel
      val applicationId: ValidatedNel[EmptyStringError, ApplicationId] =
        ApplicationId.create(application_id).toValidatedNel
      (tempOauthCode, applicationId)
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
                  Future.successful(Redirect("https://winkie.app/success"))
                case Failure(e) =>
                  logger.error(e.toString.trim)
                  Future.successful(Redirect("https://winkie.app/failure"))
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
      ApplicationId
        .create(id)
        .fold(
          e =>
            Future.successful(responseError(BadRequestError(e.errorMessage))),
          applicationId =>
            request.body.fold(
              e => Future.successful(responseError(e)),
              body =>
                updateBotClientInfoUseCase
                  .exec(
                    UpdateApplicationClientInfoUseCase
                      .Params(applicationId, body.clientId, body.clientSecret)
                  )
                  .ifFailedThenToAdapterError("error in BotController.update")
                  .toSuccessPostResponse
                  .recoverError
            )
        )
    }
}
