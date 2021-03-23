package query.publishposts

final case class PublishPostsView(
  posts: Seq[Post],
  token: String,
  channels: Seq[String]
)

final case class Post(url: String, title: String)
