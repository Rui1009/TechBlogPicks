package domains.application

import domains.{EmptyStringError, NegativeNumberError, RegexError}
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId,
  ApplicationName
}
import domains.application.Post.{PostAuthor, PostPostedAt, PostTitle, PostUrl}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Url
import io.estatico.newtype.macros.newtype

final case class Application(
  id: ApplicationId,
  name: ApplicationName,
  clientId: Option[ApplicationClientId],
  clientSecret: Option[ApplicationClientSecret],
  posts: Seq[Post]
) {
  def updateClientInfo(
    clientId: Option[ApplicationClientId],
    clientSecret: Option[ApplicationClientSecret]
  ): Application = this.copy(clientId = clientId, clientSecret = clientSecret)

  def addPost(post: Post): Application = this.copy(posts = posts :+ post)
}

object Application {
  @newtype case class ApplicationId(value: String Refined NonEmpty)
  object ApplicationId {
    def create(value: String): Either[EmptyStringError, ApplicationId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationId"))
        case Right(v) => Right(ApplicationId(v))
      }
  }

  @newtype case class ApplicationName(value: String Refined NonEmpty)
  object ApplicationName {
    def create(value: String): Either[EmptyStringError, ApplicationName] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationName"))
        case Right(v) => Right(ApplicationName(v))
      }
  }

  @newtype case class ApplicationClientId(value: String Refined NonEmpty)
  object ApplicationClientId {
    def create(value: String): Either[EmptyStringError, ApplicationClientId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationClientId"))
        case Right(v) => Right(ApplicationClientId(v))
      }
  }

  @newtype case class ApplicationClientSecret(value: String Refined NonEmpty)
  object ApplicationClientSecret {
    def create(
      value: String
    ): Either[EmptyStringError, ApplicationClientSecret] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationClientSecret"))
        case Right(v) => Right(ApplicationClientSecret(v))
      }
  }
}

final case class Post(
  url: PostUrl,
  title: PostTitle,
  author: PostAuthor,
  postedAt: PostPostedAt
) {}

object Post {
  @newtype case class PostUrl(value: String Refined Url)
  object PostUrl {
    def create(value: String): Either[RegexError, PostUrl] =
      refineV[Url](value) match {
        case Right(v) => Right(PostUrl(v))
        case Left(_)  => Left(RegexError("PostUrl"))
      }
  }

  @newtype case class PostTitle(value: String Refined NonEmpty)
  object PostTitle {
    def create(value: String): Either[EmptyStringError, PostTitle] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(PostTitle(v))
        case Left(_)  => Left(EmptyStringError("PostTitle"))
      }
  }

  @newtype case class PostAuthor(value: String Refined NonEmpty)
  object PostAuthor {
    def create(value: String): Either[EmptyStringError, PostAuthor] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(PostAuthor(v))
        case Left(_)  => Left(EmptyStringError("PostAuthor"))
      }
  }

  @newtype case class PostPostedAt(value: Long Refined Positive)
  object PostPostedAt {
    def create(value: Long): Either[NegativeNumberError, PostPostedAt] =
      refineV[Positive](value) match {
        case Right(v) => Right(PostPostedAt(v))
        case Left(_)  => Left(NegativeNumberError("PostPostedAt"))
      }
  }
}
