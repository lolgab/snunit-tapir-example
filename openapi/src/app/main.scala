package app

import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.apispec.openapi.circe.yaml.*

@main
def main =
  val docs =
    OpenAPIDocsInterpreter()
      .toOpenAPI(endpoints.list, "SNUnit Twitter", "1.0")

  println(docs.toYaml)
