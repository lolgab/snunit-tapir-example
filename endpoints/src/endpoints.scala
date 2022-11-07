package app

import sttp.tapir._

val hello = endpoint.get
  .in("hello")
  .out(stringBody)

val endpoints = hello :: Nil
