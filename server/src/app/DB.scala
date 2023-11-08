package app

import cats.effect.*
import cats.effect.std.Env
import cats.syntax.all.*

trait DB:
  def tweet(author: String, text: String): IO[Tweet]
  def tweets(): IO[Seq[Tweet]]

object DB:
  import porcupine.*
  import Codec.*

  def apply(): Resource[IO, DB] =
    for
      file <- Resource.eval(Env[IO].get("SQLITE_FILE"))
      sqliteString <-
        Resource.eval(
          file match
            case Some(f) =>
              logger.info(s"SQLITE_FILE env defined. Using sqlite file: $f") *>
                IO.pure(f)
            case None =>
              logger.info(
                "SQLITE_FILE env not defined. Using in-memory sqlite."
              ) *>
                IO.pure(":memory:")
        )
      sqlite <- Database.open[IO](sqliteString)
      _ <- Resource.eval(
        sqlite.execute(
          sql"CREATE TABLE IF NOT EXISTS tweet (id INTEGER PRIMARY KEY ASC, author, text)".command
        )
      )
    yield new DB:
      def tweet(author: String, tweetText: String): IO[Tweet] =
        for
          ids <- sqlite.execute(
            sql"INSERT INTO tweet (id, author, text) VALUES (NULL, $text, $text) RETURNING id"
              .query(integer *: nil),
            (author, tweetText)
          )
          id <- ids.headOption
            .map(_._1)
            .liftTo[IO](new Exception("Nothing inserted"))
        yield Tweet(id, author, tweetText)

      def tweets(): IO[Seq[Tweet]] =
        for result <- sqlite.execute(
            sql"SELECT t.id, t.author, t.text from tweet t ORDER BY t.id DESC"
              .query(integer *: text *: text *: nil)
          )
        yield result.map((id, author, text) => Tweet(id, author, text))
