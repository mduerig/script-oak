package michid.script

import java.io.File.createTempFile
import java.io.PrintWriter

import ammonite.interp.InterpBridge
import ammonite.ops.{Path, read, resource}
import ammonite.repl.ReplBridge
import michid.script.oak.filestore.SegmentAnalyser
import michid.script.oak.nodestore.Items.{EMPTY, Node, Property}
import org.apache.jackrabbit.oak.api.PropertyState
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.tooling.filestore.Segment

/** Common predefs used by script-oak */
package object oak {

  /** read a script from /scripts */
  def script(name: String): String = read! resource/'scripts/name

  implicit class RunScript(script: String) {
    def run(): Unit = {
      val repl = ReplBridge.value0
      if (repl != null) {
        repl.load(script)
      } else {
        // Spool the script to a temporary file if the repl is not accessible.
        // I.e. when running the script directly
        // michid the interpreter should expose a method to interpret a string
        val interpreter = InterpBridge.value0
        if (interpreter != null) {
          val scriptFile = createTempFile("script", "sc")
          scriptFile.deleteOnExit()
          new PrintWriter(scriptFile) { write(script); close() }
          interpreter.load.module(Path(scriptFile))
        } else {
          throw new Error(s"No interpreter for running script ${script.substring(0, 40)} ...")
        }
      }
    }
  }

  implicit class AsNode(node: NodeState) {
    def analyse = new Node(node)
  }

  implicit class AsProperty(property: PropertyState) {
    def analyse = Property(EMPTY, property)
  }

  implicit class AsSegmentAnalyser(segment: Segment) {
    def analyse = new SegmentAnalyser(segment)
  }

}
