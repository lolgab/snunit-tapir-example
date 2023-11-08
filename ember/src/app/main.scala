package app

import cats.effect.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.staticcontent.*
import sttp.tapir.server.http4s.Http4sServerInterpreter

object main extends ResourceApp.Forever:
  def run(args: List[String]) = for
    endpoints <- endpointsWithLogic
    _ <- EmberServerBuilder
      .default[IO]
      .withHttpApp(
        Router(
          "/" -> Http4sServerInterpreter[IO]().toRoutes(endpoints),
          "/" -> fileService(FileService.Config("./out/buildApp.dest/static"))
        ).orNotFound
      )
      .build
  yield ()
