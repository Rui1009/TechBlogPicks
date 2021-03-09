package domains.post

import domains.{EmptyStringError, NegativeNumberError, RegexError}
import domains.post.Post._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Url

final case class Post(id: PostId,
                      url: PostUrl,
                      title: PostTitle,
                      postedAt: PostedAt)

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

  @newtype case class PostedAt(value: Long Refined Positive)
  object PostedAt {
    def create(value: Long): Either[NegativeNumberError, PostedAt] =
      refineV[Positive](value) match {
        case Right(v) => Right(PostedAt(v))
        case Left(_)  => Left(NegativeNumberError("PostedAt"))
      }
  }
}
