package michid.script.shell

import java.io.ByteArrayOutputStream

import michid.script.shell.Main.main

trait ScriptRunner {
  def run(script: String)
         (result: (String, String) => Unit = (_,_) => ())
  : Unit = {
    val sOut = new ByteArrayOutputStream
    val sErr = new ByteArrayOutputStream

    Console.withOut(sOut) {
      Console.withErr(sErr) {
        main(Array(script))
      }
    }

    result(sOut.toString, sErr.toString)
  }
}

object ScriptRunner extends ScriptRunner
