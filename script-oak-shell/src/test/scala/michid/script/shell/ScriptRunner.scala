package michid.script.shell

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import michid.script.oak.fixtures.OakFixture

trait ScriptRunner {
  def run(oakFixture: OakFixture)
         (script: String)
         (result: (String, String) => Unit = (_,_) => ())
  : Unit = {
    val in = new ByteArrayInputStream(Array.empty[Byte])
    val sOut = new ByteArrayOutputStream
    val sErr = new ByteArrayOutputStream

    Console.withIn(in) {
      Console.withOut(sOut) {
        Console.withErr(sErr) {
          Main.run(script, oakFixture)
        }
      }
    }

    result(sOut.toString, sErr.toString)
  }
}

object ScriptRunner extends ScriptRunner
