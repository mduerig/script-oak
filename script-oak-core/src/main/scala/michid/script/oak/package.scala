package michid.script

import ammonite.ops.{read, resource}
import ammonite.runtime.InterpBridge
import michid.script.oak.filestore.SegmentAnalyser
import michid.script.oak.nodestore.Items.{EMPTY, Node, Property}
import org.apache.jackrabbit.oak.api.PropertyState
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.tooling.filestore.Segment

// michid interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))

/** Common predefs used by script-oak */
package object oak {

  /** read a script from /scripts */
  def script(name: String): String = read! resource/'scripts/name

  /** Execute a string as a script */
  implicit class RunScript(script: String) {
    def run(): Unit = InterpBridge.value0.load(script)
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
