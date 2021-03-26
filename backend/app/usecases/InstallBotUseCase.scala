package usecases

import com.google.inject.Inject
import domains.workspace.WorkSpace.WorkSpaceTemporaryOauthCode
import domains.workspace.WorkSpaceRepository
import domains.bot.{Bot, BotRepository}
import domains.bot.Bot.BotId
import usecases.InstallBotUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait InstallBotUseCase {
  def exec(params: Params): Future[Unit]
}

object InstallBotUseCase {
  final case class Params(
    temporaryOauthCode: WorkSpaceTemporaryOauthCode,
    botId: BotId
  )
}

final class InstallBotUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository,
  botRepository: BotRepository
)(implicit val ec: ExecutionContext)
    extends InstallBotUseCase {
  override def exec(params: Params): Future[Unit] = for {
    targetBot <- botRepository
                   .find(params.botId)
                   .ifFailThenToUseCaseError(
                     "error while botRepository.find in install bot use case"
                   )

    targetBotClientId     <-
      targetBot.clientId.ifNotExistsToUseCaseError(
        "error while get bot client id in install bot use case"
      )
    targetBotClientSecret <-
      targetBot.clientSecret.ifNotExistsToUseCaseError(
        "error while get bot client secret in install bot use case"
      )

    workSpace <-
      workSpaceRepository
        .find(
          params.temporaryOauthCode,
          targetBotClientId,
          targetBotClientSecret
        )
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in install bot use case"
        )

    updatedWorkSpace = workSpace.installBot(targetBot)

    _ <- workSpaceRepository
           .update(updatedWorkSpace)
           .ifFailThenToUseCaseError(
             "error while workSpace.update in install bot use case"
           )
  } yield ()
}
