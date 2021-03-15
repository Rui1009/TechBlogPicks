package adapters.controllers.post

import adapters.controllers.helpers.JsonRequestMapper
import adapters.{AdapterError, BadRequestError}
import cats.implicits._
import domains.DomainError
import domains.bot.Bot.BotId
import domains.post.Post._
import play.api.mvc.BaseController

final case class CreatePostBody(
  url: Option[String],
  title: String,
  author: String,
  postedAt: Long,
  botIds: Seq[String]
)

final case class CreatePostCommand(
  url: Option[PostUrl],
  title: PostTitle,
  author: PostAuthor,
  postedAt: PostPostedAt,
  botIds: Seq[BotId]
)

trait PostCreateBodyMapper
    extends JsonRequestMapper[CreatePostBody, CreatePostCommand] {
  this: BaseController =>
  override def mapToValueObject(
    body: CreatePostBody
  ): Either[AdapterError, CreatePostCommand] = (
    body.url.traverse(PostUrl.create(_).toValidatedNec),
    PostTitle.create(body.title).toValidatedNec,
    PostAuthor.create(body.author).toValidatedNec,
    PostPostedAt.create(body.postedAt).toValidatedNec,
    body.botIds.map(BotId.create(_).toValidatedNec).sequence
  ).mapN(CreatePostCommand.apply)
    .toEither
    .leftMap(errors =>
      BadRequestError(
        errors.foldLeft("")((acc, curr: DomainError) => acc + curr.errorMessage)
      )
    )
}
