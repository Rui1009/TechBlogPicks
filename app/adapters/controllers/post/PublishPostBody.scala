package adapters.controllers.post

import query.publishposts.PublishPostsView

case class PublishPostBody(token: String, channel: String, text: String)

object PublishPostBody {
  def fromViewModels(models: Seq[PublishPostsView]): Seq[PublishPostBody] =
    for {
      model   <- models
      channel <- model.channels
    } yield PublishPostBody(
      model.token,
      channel,
      model.posts.foldLeft("")((acc, curr) => acc + "\n" + curr.url)
    )
}
