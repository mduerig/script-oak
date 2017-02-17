package michid.script.oak.nodestore

import org.apache.jackrabbit.oak.commons.PathUtils.elements
import org.apache.jackrabbit.oak.spi.state.NodeState

import scala.collection.JavaConverters._

/**
  * Projection from the root to a child
  */
class Projection(val path: String) extends (NodeState => NodeState) {
  override def apply(root: NodeState): NodeState =
    elements(path).asScala.foldLeft(root) {
      (node, childName) => node.getChildNode(childName)}
}

object Projection {
  val none: Projection = Projection("")
  val root: Projection = Projection("root")
  val checkpoints: Projection = Projection("checkpoints")

  def apply(path: String): Projection =
    new Projection(path)
}
