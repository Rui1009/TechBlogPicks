package adapters.controllers.post

import adapters.controllers.helpers.JsonRequestMapper
import adapters.{AdapterError, BadRequestError}
import cats.implicits._
import domains.DomainError
import domains.application.Application.ApplicationId
import domains.post.Post._
import play.api.mvc.{BaseController, BodyParser}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

final case class CreatePostBody(
  url: String,
  title: String,
  author: String,
  postedAt: Long,
  applicationIds: Seq[String]
)

final case class CreatePostCommand(
  url: PostUrl,
  title: PostTitle,
  author: PostAuthor,
  postedAt: PostPostedAt,
  applicationIds: Seq[ApplicationId]
)

trait PostCreateBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToCreateCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, CreatePostCommand]] =
    mapToValueObject[CreatePostBody, CreatePostCommand] { body =>
      (
        PostUrl.create(body.url).toValidatedNec,
        PostTitle.create(body.title).toValidatedNec,
        PostAuthor.create(body.author).toValidatedNec,
        PostPostedAt.create(body.postedAt).toValidatedNec,
        body.applicationIds.map(ApplicationId.create(_).toValidatedNec).sequence
      ).mapN(CreatePostCommand.apply)
        .toEither
        .leftMap(errors =>
          BadRequestError(
            errors
              .foldLeft("")((acc, curr: DomainError) => acc + curr.errorMessage)
          )
        )
    }
}
