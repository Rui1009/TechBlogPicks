package domains.post

import domains.application.Application
import domains.post.Post._
import domains._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.Url
import io.estatico.newtype.macros.newtype

final case class Post(
  id: PostId,
  url: PostUrl,
  title: PostTitle,
  author: PostAuthor,
  postedAt: PostPostedAt,
  testimonial: Option[PostTestimonial]
) {
  def assign(applications: Seq[Application]): Seq[Application] =
    applications.map(app => app.copy(posts = app.posts :+ this.id))
}

object Post {
  @newtype case class PostId(value: Long Refined Positive)
  object PostId extends VOFactory[NegativeNumberError] {
    override def castError(e: ValidationError): NegativeNumberError =
      NegativeNumberError("PostId")
  }

  @newtype case class PostUrl(value: String Refined Url)
  object PostUrl extends VOFactory[RegexError] {
    override def castError(e: ValidationError): RegexError =
      RegexError("PostUrl")
  }

  @newtype case class PostTitle(value: String Refined NonEmpty)
  object PostTitle extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("PostTitle")
  }

  @newtype case class PostAuthor(value: String Refined NonEmpty)
  object PostAuthor extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("PostAuthor")
  }

  @newtype case class PostPostedAt(value: Long Refined Positive)
  object PostPostedAt extends VOFactory[NegativeNumberError] {
    override def castError(e: ValidationError): NegativeNumberError =
      NegativeNumberError("PostPostedAt")
  }

  @newtype case class PostTestimonial(value: String Refined NonEmpty)
  object PostTestimonial extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("PostTestimonial")
  }
}

final case class UnsavedPost(
  url: PostUrl,
  title: PostTitle,
  author: PostAuthor,
  postedAt: PostPostedAt,
  testimonial: Option[PostTestimonial]
)
