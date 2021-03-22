package query.posts

case class PostsView(
  id: Long,
  url: String,
  title: String,
  author: String,
  postedAt: Long,
  createdAt: Long
)
