import mill._
import mill.scalalib._
import mill.scalanativelib._
import mill.util.Jvm
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.4`
import com.github.lolgab.mill.crossplatform._

object versions {
  val catsEffect = "3.5.2"
  val snunit = "0.7.2"
  val tapir = "1.8.1"
  val http4s = "0.23.23"
  val porcupine = "0.0.1"
  val scalatags = "0.12.0"
  val scribe = "3.12.2"
}

/** Download the sqlite amalgamation so we can compile sqlite with our
  * application and we don't need to install sqlite packages, nor add sqlite to
  * our Docker image
  */
def downloadSqlite = T {
  import java.util.zip._
  val zipName = "sqlite-amalgamation-3430200"
  val fileName = "sqlite3.c"
  val zis = new ZipInputStream(
    requests.get
      .stream(s"https://www.sqlite.org/2023/$zipName.zip")
      .readBytesThrough(identity)
  )
  def loop(): PathRef = {
    val entry = zis.getNextEntry()
    if (entry != null && entry.getName() == s"$zipName/$fileName") {
      val dir = T.dest / "scala-native"
      os.makeDir(dir)
      val file = dir / fileName
      os.write(file, zis)
      PathRef(T.dest)
    } else loop()
  }

  loop()
}

object Common {
  trait Base extends ScalaModule {
    def scalaVersion = "3.3.1"
  }
  trait JVM extends Base
  trait Native extends Base with ScalaNativeModule {
    def scalaNativeVersion = "0.4.16"
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
object server extends CrossPlatform {
  def moduleDeps = Seq(endpoints)
  trait Shared extends CrossPlatformScalaModule {
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"org.typelevel::cats-effect::${versions.catsEffect}",
      ivy"com.armanbilge::porcupine::${versions.porcupine}",
      ivy"com.outr::scribe-cats::${versions.scribe}",
      ivy"com.lihaoyi::scalatags::${versions.scalatags}"
    )
  }
  object jvm extends Shared with Common.JVM
  object native extends Shared with Common.Native
}
object snunit extends Common.Native {
  def moduleDeps = Seq(server.native)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.github.lolgab::snunit-tapir-cats::${versions.snunit}",
    ivy"com.github.lolgab::snunit-async-epollcat::${versions.snunit}"
  )
  def resources = super.resources() ++ Seq(downloadSqlite())
}
object ember extends Common.JVM {
  def moduleDeps = Seq(server.jvm)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.softwaremill.sttp.tapir::tapir-http4s-server::${versions.tapir}",
    ivy"org.http4s::http4s-ember-server::${versions.http4s}",
    ivy"com.outr::scribe-slf4j:${versions.scribe}"
  )
}

object openapi extends Common.JVM {
  def moduleDeps = Seq(endpoints.jvm)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.softwaremill.sttp.tapir::tapir-openapi-docs::${versions.tapir}",
    ivy"com.softwaremill.sttp.apispec::openapi-circe-yaml::0.7.1"
  )
  def buildYaml = T {
    val dest = T.dest / "api.yaml"
    val content = os.proc(assembly().path).call().out.text()
    os.write(dest, content)
    PathRef(dest)
  }
}

def unitConf = T.source { T.workspace / "conf.json" }
def staticDir = T.source { T.workspace / "static" }

/** Persistent directory for the dev sqlite database file */
def dbDir = T.persistent { T.dest }

def buildApp = T {
  val statedir = T.dest / "statedir"
  os.makeDir.all(statedir)
  os.symlink(T.dest / "db", dbDir())
  os.copy.into(unitConf().path, statedir)
  os.copy.into(snunit.nativeLink(), T.dest)
  os.copy.into(staticDir().path, T.dest)
  os.copy.into(openapi.buildYaml().path, T.dest / "static" / "api" / "docs")
  T.dest
}

object `dev-server` extends Common.JVM

def runUnit() = T.command {
  buildApp()

  `dev-server`.runBackground(
    "unitd --statedir statedir --log /dev/stdout --no-daemon --control 127.0.0.1:9000"
  )()
}
