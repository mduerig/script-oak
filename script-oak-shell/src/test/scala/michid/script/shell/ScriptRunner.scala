package michid.script.shell

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

trait ScriptRunner {
  def run(script: String)
         (result: (String, String) => Unit = (_,_) => ())
  : Unit = {
    val in = new ByteArrayInputStream(Array.empty[Byte])
    val sOut = new ByteArrayOutputStream
    val sErr = new ByteArrayOutputStream

    Console.withIn(in) {
      Console.withOut(sOut) {
        Console.withErr(sErr) {
          Main.run(script)
        }
      }
    }

    result(sOut.toString, sErr.toString)
  }
}

object ScriptRunner extends ScriptRunner
