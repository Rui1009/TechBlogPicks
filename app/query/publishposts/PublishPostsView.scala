package query.publishposts

final case class PublishPostsView(posts: Seq[Post], token: String)

final case class Post(url: Option[String], title: String)
