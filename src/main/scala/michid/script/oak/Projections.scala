package michid.script.oak

import org.apache.jackrabbit.oak.spi.state.NodeState

/**
  * Some pre-defined projections from the root to child
  */
object Projections {
  val root: NodeState => NodeState = root => root.getChildNode("root")
  val checkpoints: NodeState => NodeState = root => root.getChildNode("checkpoints")
}
