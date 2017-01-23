package michid.script.oak.nodestore

import org.apache.jackrabbit.oak.api.{PropertyState, Type}
import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState.EMPTY_NODE
import org.apache.jackrabbit.oak.spi.state.NodeState

import scala.collection.JavaConverters._

/** Extract nodes, properties and values */
object Items {
  def root(root: NodeState): Node =
    new Node(root)

  sealed trait Item {
    val name: String = ""
    def path: String = Items.path(this)
  }

  /** Empty node having no parent. Used as parent for root nodes */
  val EMPTY: Node = Node(null, "", EMPTY_NODE)

  /** NodeState wrapper providing access to the nodes name and parent */
  case class Node(parent: Node, override val name: String, state: NodeState) extends Item {
    def this(root: NodeState) = this(EMPTY, "", root)

    /** Direct child nodes of this node */
    def nodes: Stream[Node] =
      state.getChildNodeEntries.asScala.toStream.map {
        cne => Node(this, cne.getName, cne.getNodeState)}

    /** Direct properties of this node */
    def properties: Stream[Property] =
      state.getProperties.asScala.toStream.map(Property(this, _))

    /** All values of all direct properties of this node */
    def values: Stream[Value] =
      properties.flatMap(_.values)

    /**
      * Recursively apply f to all children of this node and combine the
      * results through r.
      *
      * Examples:
      * <pre>
      *   val nodesSubtree = root.mapReduce[Int](_.nodes.size, _ + _)
      *   val collectNodes = root.mapReduce(Stream(_), (m: Stream[Node], n: Stream[Node]) => m #::: n)
      * </pre>
      */
    def mapReduce[A](f: Node => A, r: (A, A)  => A): A =
      nodes.map(_.mapReduce(f, r)).fold(f(this))(r)

    override def toString: String =
      path + " @ " + state
  }

  /** PropertyState wrapper providing access to the parent of the */
  case class Property(parent: Node, state: PropertyState) extends Item {
    override val name: String = state.getName

    /** All values of this property */
    def values: Stream[Value] = {
      (0 until state.count).map(i => {
        val tyqe =
          if (state.isArray) state.getType.getBaseType
          else state.getType
        Value(this, i, tyqe, state.getValue(tyqe, i))
      }).toStream
    }

    override def toString: String =
      path + "[" + state.getType + "(" + state.count() +  ")] @ " + state
  }

  /** Property value wrapper providing access to the parent property, the index of this value
    * in its parent, its value and its type.
    */
  case class Value(parent: Property, index: Int, tyqe: Type[_], value: Any) extends Item {
    override val name: String = "[" + index + "]"

    override def toString: String =
      path + "[" + tyqe + "] @ " + value
  }

  /** Reflexive transitive closure over the child nodes of the passed parent node */
  def collectNodes(parent: Node): Stream[Node] =
    Stream(parent) #::: parent.nodes.flatMap(collectNodes)

  /** All properties on the reflexive transitive closure over the child nodes of the passed parent node */
  def collectProperties(parent: Node): Stream[Property] =
    collectNodes(parent).flatMap(_.properties)

  /** All values on the reflexive transitive closure over the child nodes of the passed parent node */
  def collectValues(parent: Node): Stream[Value] =
    collectProperties(parent).flatMap(_.values)

  /** String representation of the path of an item */
  def path(item: Item): String = item match {
    case EMPTY => EMPTY.name
    case n@Node(parent, _, _) => path(parent) + "/" + n.name
    case p@Property(parent, _) => path(parent) + "/" + p.name
    case v@Value(parent, _, _, _) => path(parent) + v.name
  }

}
