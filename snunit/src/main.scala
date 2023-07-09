package app

import snunit.tapir._

import cats.effect._

val endpointsWithLogic =
  List(
    hello.serverLogicSuccess[IO](_ => IO("Hello world!"))
  )

object Main extends epollcat.EpollApp.Simple:
  def run = 
    SNUnitServerBuilder
      .default[IO]
      .withServerEndpoints(endpointsWithLogic)
      .run
