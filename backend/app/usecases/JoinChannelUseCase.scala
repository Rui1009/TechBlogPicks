package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.bot.{Bot, BotRepository}
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.WorkSpaceRepository
import usecases.JoinChannelUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait JoinChannelUseCase {
  def exec(params: Params): Future[Unit]
}

object JoinChannelUseCase {
  final case class Params(
    channelId: ChannelId,
    applicationId: ApplicationId,
    workSpaceId: WorkSpaceId
  )
}

final class JoinChannelUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext)
    extends JoinChannelUseCase {
//  override def exec(params: Params): Future[Unit] = for {
//    targetBot <- botRepository
//                   .find(params.botId, params.workSpaceId)
//                   .ifNotExistsToUseCaseError(
//                     "error while botRepository.find in join channel use case"
//                   )
//    _         <- botRepository
//                   .join(targetBot.joinTo(params.channelId))
//                   .ifFailThenToUseCaseError(
//                     "error while botRepository.join in join channel use case"
//                   )
//  } yield ()

  override def exec(params: Params) = for {
    workSpace <-
      workSpaceRepository.find(params.workSpaceId).ifNotExistsToUseCaseError("")

    updatedWorkSpace <-
      workSpace.addBotToChannel(params.applicationId, params.channelId) match {
        case Right(v) => Future.successful(v)
        case Left(e)  => Future.failed(
            NotFoundError(
              "" + "\n" +
                e.errorMessage
            )
          )
      }

    _ <- workSpaceRepository
           .update(updatedWorkSpace)
           .ifFailThenToUseCaseError(
             "error while WorkSpace.addBotToChannel in join channel use case"
           )
  } yield ()
//    .ifFailThenToUseCaseError(
//      "error while botRepository.join in join channel use case"
//    )
}
