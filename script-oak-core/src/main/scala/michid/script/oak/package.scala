package michid.script

import ammonite.ops.{read, resource}
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
    def run(): Unit =
      ReplBridge.value0.load(script)
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
