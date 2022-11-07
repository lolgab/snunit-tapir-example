package app

import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.apispec.openapi.circe.yaml._

@main
def main =
  val docs =
    OpenAPIDocsInterpreter()
      .toOpenAPI(endpoints, "My App", "1.0")

  println(docs.toYaml)
