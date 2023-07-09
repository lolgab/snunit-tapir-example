import mill._
import mill.scalalib._
import mill.scalanativelib._
import mill.util.Jvm
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.3`
import com.github.lolgab.mill.crossplatform._

object versions {
  val tapir = "1.6.0"
  val snunit = "0.7.1"
}

object Common {
  trait Base extends ScalaModule {
    def scalaVersion = "3.3.0"
  }
  trait JVM extends Base
  trait Native extends Base with ScalaNativeModule {
    def scalaNativeVersion = "0.4.14"
  }
}

object endpoints extends CrossPlatform {
  trait Shared extends CrossPlatformScalaModule {
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.softwaremill.sttp.tapir::tapir-core::${versions.tapir}"
    )
  }
  object jvm extends Shared with Common.JVM
  object native extends Shared with Common.Native
}
object snunit extends Common.Native {
  def moduleDeps = Seq(endpoints.native)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.github.lolgab::snunit-tapir-cats::${versions.snunit}",
    ivy"com.github.lolgab::snunit-async-epollcat::${versions.snunit}"
  )
}
object openapi extends Common.JVM {
  def moduleDeps = Seq(endpoints.jvm)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.softwaremill.sttp.tapir::tapir-openapi-docs::${versions.tapir}",
    ivy"com.softwaremill.sttp.apispec::openapi-circe-yaml::0.5.3"
  )
  def buildYaml = T {
    val dest = T.dest / "api.yaml"
    val content = os.proc(launcher().path).call().out.text()
    os.write(dest, content)
    PathRef(dest)
  }
}

def unitConf = T.source { T.workspace / "conf.json" }
def staticDir = T.source { T.workspace / "static" }
def runUnit() = T.command {
  val wd = T.workspace
  val statedir = T.dest / "statedir"
  os.makeDir.all(statedir)
  os.copy.into(unitConf().path, statedir)
  os.copy.into(snunit.nativeLink(), T.dest)
  os.copy.into(staticDir().path, T.dest)
  os.copy.into(openapi.buildYaml().path, T.dest / "static" / "api" / "docs")

  Jvm.runSubprocess(
    commandArgs = Seq(
      "unitd",
      "--statedir",
      "statedir",
      "--log",
      "/dev/stdout",
      "--no-daemon",
      "--control",
      "127.0.0.1:9000"
    ),
    envArgs = Map(),
    workingDir = T.dest
  )
}
