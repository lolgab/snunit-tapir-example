package app

import cats.effect.*

val endpointsWithLogic = for
  db <- DB()
  server <- Server(db)
yield server.endpointsWithLogic
