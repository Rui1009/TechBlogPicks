package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.bot.Bot.BotId
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import usecases.GreetInInvitedChannelUseCase.Params
import cats.syntax.either._

import scala.concurrent.{ExecutionContext, Future}

trait GreetInInvitedChannelUseCase {
  def exec(params: Params): Future[Unit]
}

object GreetInInvitedChannelUseCase {
  final case class Params(
    workSpaceId: WorkSpaceId,
    channelId: ChannelId,
    applicationId: ApplicationId,
    botId: BotId
  )
}

final class GreetInInvitedChannelUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext)
    extends GreetInInvitedChannelUseCase {
  object BotNotFound extends Exception

  override def exec(params: Params) = (for {
    targetWorkSpace         <-
      workSpaceRepository
        .find(params.workSpaceId)
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in greet in invited channel use case"
        )
    workSpaceWithUpdatedBot <-
      targetWorkSpace.botCreateGreetingInInvitedChannel(params.botId) match {
        case Right(v) => Future.successful(v)
        case Left(_)  =>
          println("left enter")
          throw BotNotFound
      }

    workSpaceWithUpdatedChannel <-
      workSpaceWithUpdatedBot
        .botPostMessage(params.applicationId, params.channelId)
        .ifLeftThenToUseCaseError(
          "error while WorkSpace.botPostMessage in greet in invited channel use case"
        )
    _                           <- workSpaceRepository
                                     .sendMessage(
                                       workSpaceWithUpdatedChannel,
                                       params.applicationId,
                                       params.channelId
                                     )
                                     .ifNotExistsToUseCaseError(
                                       "error while workSpaceRepository.sendMessage in greet in invited channel use case"
                                     )
  } yield ()).recoverWith { case BotNotFound => Future.unit }
}
