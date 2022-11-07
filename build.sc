import mill._
import mill.scalalib._
import mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.0.3`
import com.github.lolgab.mill.crossplatform._

object Common {
  trait Base extends ScalaModule {
    def scalaVersion = "3.2.0"
  }
  trait JVM extends Base
  trait Native extends Base with ScalaNativeModule {
    def scalaNativeVersion = "0.4.7"
  }
}

object endpoints extends CrossPlatform {
  trait Shared extends CrossPlatformScalaModule {
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.softwaremill.sttp.tapir::tapir-core::1.1.4"
    )
  }
  object jvm extends Shared with Common.JVM
  object native extends Shared with Common.Native
}
object snunit extends Common.Native {
  def moduleDeps = Seq(endpoints.native)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.github.lolgab::snunit-tapir-cats::0.2.4",
    ivy"com.github.lolgab::snunit-async-epollcat::0.2.4"
  )
}
object openapi extends Common.JVM {
  def moduleDeps = Seq(endpoints.jvm)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.softwaremill.sttp.tapir::tapir-openapi-docs::1.1.4",
    ivy"com.softwaremill.sttp.apispec::openapi-circe-yaml::0.3.1"
  )
  def buildYaml = T {
    val dest = T.dest / "api.yaml"
    val content = os.proc(launcher().path).call().out.text
    os.write(dest, content)
    PathRef(dest)
  }
}

def deploy() = T.command {
  val binary = snunit.nativeLink()
  val openapiYaml = openapi.buildYaml().path

  val config = s"""
  {
    "applications": {
      "app": {
        "executable": "$binary",
        "type": "external"
      }
    },
    "listeners": {
      "*:8081": {
        "pass": "routes"
      }
    },
    "routes": [
      {
        "match": {
          "uri": "/api/docs"
        },
        "action": {
          "share": "${T.workspace}/docs/index.html"
        }
      },
      {
        "match": {
          "uri": "/api/docs/api.yaml"
        },
        "action": {
          "share": "$openapiYaml"
        }
      },
      {
        "action": {
          "pass": "applications/app"
        }
      }
    ]
  }
  """

  println(config)

  os.proc(
    "curl",
    "-s",
    "-XPUT",
    "-d",
    config,
    "--unix-socket",
    "/usr/local/var/run/unit/control.sock",
    "localhost/config"
  ).call()
}
