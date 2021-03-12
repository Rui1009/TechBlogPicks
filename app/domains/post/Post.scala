package domains.post

import domains.{EmptyStringError, NegativeNumberError, RegexError}
import domains.post.Post._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Url

final case class Post(
  id: Option[PostId],
  url: Option[PostUrl],
  title: PostTitle,
  author: PostAuthor,
  postedAt: PostPostedAt
)

object Post {
  @newtype case class PostId(value: Long Refined Positive)
  object PostId {
    def create(value: Long): Either[NegativeNumberError, PostId] =
      refineV[Positive](value) match {
        case Right(v) => Right(PostId(v))
        case Left(_)  => Left(NegativeNumberError("PostId"))
      }
  }

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
