package app

import cats.effect.*
import cats.syntax.all.*
import sttp.tapir.server.*

trait Server {
  def endpointsWithLogic: List[ServerEndpoint[Any, IO]]
}

object Server:
  def apply(db: DB): Resource[IO, Server] =
    Resource.pure:
      new Server:
        def index =
          endpoints.index.serverLogicSuccess: _ =>
            for
              _ <- logger.info("called GET /tweets")
              tweets <- db.tweets()
            yield html.renderIndex(tweets).render

        def getTweets =
          endpoints.getTweets.serverLogicSuccess: _ =>
            for
              _ <- logger.info("called GET /tweets")
              tweets <- db.tweets()
            yield html.renderTweets(tweets).render
        def postTweet =
          endpoints.postTweet.serverLogicSuccess: body =>
            for
              _ <- logger.info("called POST /tweet")
              added <- db.tweet(author = body.author, text = body.text)
            yield html.renderTweet(added).render

        def endpointsWithLogic = List(index, getTweets, postTweet)
