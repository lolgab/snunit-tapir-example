import java.io.File

@main
def main(command: String) =
  import sys.process.*

  val bgProc = Process(command, cwd = new File("out/buildApp.dest")).run()

  sys.addShutdownHook {
    println("Caught shutdown, killing process")
    bgProc.destroy
  }
  val ev = bgProc.exitValue
  println(s"Process finished naturally with code $ev")
