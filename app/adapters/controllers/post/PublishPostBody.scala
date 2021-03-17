package adapters.controllers.post

import query.publishposts.PublishPostsView

case class PublishPostBody(token: String, channel: String, text: Option[String])

object PublishPostBody {
  def fromViewModels(
    models: Seq[PublishPostsView],
    tempMsg: String
  ): Seq[PublishPostBody] =
    for {
      model   <- models
      channel <- model.channels
    } yield PublishPostBody(
      model.token,
      channel,
      model.posts.foldLeft(Option(tempMsg))((acc, curr) =>
        for {
          accText <- acc
        } yield curr.url match {
          case Some(v) => accText + "\n" + v
          case None    => accText
        }
      )
    )
}

case class PublishPostResponse(channel: String)
