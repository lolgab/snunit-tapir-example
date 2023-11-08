package app

import sttp.tapir._

object endpoints:
  val index = endpoint.get
    .in("")
    .out(htmlBodyUtf8)

  val getTweets = endpoint.get
    .in("tweets")
    .out(htmlBodyUtf8)

  case class PostTweetBody(author: String, text: String) derives Schema
  val postTweet = endpoint.post
    .in("tweet")
    .in(formBody[PostTweetBody])
    .out(htmlBodyUtf8)

  val list = index :: getTweets :: postTweet :: Nil
