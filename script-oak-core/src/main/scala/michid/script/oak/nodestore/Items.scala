package michid.script.oak.nodestore

import michid.script.oak.LazyIterators
import org.apache.jackrabbit.oak.api.{PropertyState, Type}
import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState.EMPTY_NODE
import org.apache.jackrabbit.oak.plugins.memory.EmptyPropertyState.emptyProperty
import org.apache.jackrabbit.oak.spi.state.NodeState

import scala.collection.JavaConverters._

/** Extract nodes, properties and values */
object Items {
  sealed trait Item {
    val name: String = ""
    def path: String = Items.path(this)
  }

  /** Empty node having no parent. Used as parent for root nodes */
  val EMPTY: Node = Node(null, "", EMPTY_NODE)

  /** NodeState wrapper providing access to the nodes name and parent */
  case class Node(parent: Node, override val name: String, state: NodeState) extends Item {
    def this(root: NodeState) = this(EMPTY, "", root)

    def node(name: String): Node =
      Node(this, name, state.getChildNode(name))

    def / (name: String): Node = node(name)

    /** Determine whether `node` is a (transitive) parent of this node.
      */
    def hasParent(node: Node): Boolean = parent match {
      case null => false
      case p if p == node => true
      case p => p.hasParent(node)
    }

    def property(name: String): Property = {
      Property(this, Option(
        state.getProperty(name))
          .getOrElse(emptyProperty(name, Type.STRINGS)))
    }

    def /[T] (name: String, tyqe: Type[T], index: Int = 0): T =
      property(name)(tyqe, index)

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

    def apply[T](tyqe: Type[T], index: Int = 0): T =
      state.getValue(tyqe, index)

    /** All values of this property */
    def values: Stream[Value] = {
      (0 until state.count).map(i => {
        val tyqe =
          if (state.isArray) state.getType.getBaseType
          else state.getType
        Value(this, i, tyqe, state.size(i), apply(tyqe, i))
      }).toStream
    }

    /** Size of the properties according the sum of the sizes of all its values */
    def size: Long = {
      ItemStates.propertySize(skipExternal = false)(state)
    }

    override def toString: String =
      path + "[" + state.getType + "(" + state.count() +  ")] @ " + state
  }

  /** Property value wrapper providing access to the parent property, the index of this value
    * in its parent, its value and its type.
    */
  case class Value(parent: Property, index: Int, tyqe: Type[_], size: Long, value: Any) extends Item {
    override val name: String = "[" + index + "]"

    def apply[T](tyqe: Type[T]): T =
      parent(tyqe, index)

    override def toString: String =
      path + "[" + tyqe + "] @ " + value
  }

  /** Fold function `g` over the tree rooted at `node`.
    * The function `g` is called recursively receiving the current
    * node and a stream of its recursively folded child nodes.
    */
  def fold[T](node: Node)(g: (Node, Stream[T]) => T): T = {
    g(node, node.nodes.map(fold(_)(g)))
  }

  /** Fold functions `addChild` and `addParent` over the tree rooted at `node`.
    * The `addChild` function is called to accumulate the recursively folded child
    * nodes of every node. Subsequently the `addParent` is called receiving the
    * current node and the child nodes accumulated through `addChild`.
    */
  def fold2[T](node: Node)(unitT: T)(addChild: (T, T) => T)(addParent: (Node, T) => T): T = {
    addParent(node, node.nodes.foldLeft(unitT) {
      case(children, child) =>
        addChild(children, fold2(child)(unitT)(addChild)(addParent))})
  }

  /** Weigher assigning a constant weight of `1` to every node.
    */
  val unitWeight: Node => Long = _ => 1

  /** Weigher assigning the sum of all sizes of all properties to a node.
    */
  def propertySizeWeight(skipExternals: Boolean = true): Node => Long =
    node => ItemStates.nodeSize(skipExternals)(node.state)

  /** Weigh the nodes in the tree rooted at `node` with the given `weigher`.
    */
  def weighNodes(weigher: Node => Long)(node: Node): Long = {
    fold[Long](node) { case(n, ts) => weigher(n) + ts.sum }
  }

  /** Collect those nodes in the tree rooted at `node` that are at or above
    * a given threshold `minWeight` according to the passed `weigher`.
    */
  def collectNodes(weigher: Node => Long, minWeight: Long)(node: Node): Stream[(Long, Node)] = {
    fold[(Long, Stream[(Long, Node)])](node) {
      case(parent, childCounts) =>
        val weight = childCounts.map(_._1).sum + weigher(parent)
        val nodes = childCounts.flatMap(_._2)

        if (weight >= minWeight) (weight, (weight, parent) #:: nodes)
        else (weight, nodes)
    }._2
  }

  /** Collect the top `count` node in the tree rooted at `node` using the passed
    * `weigher`.
    */
  def rankNodes(weigher: Node => Long, count: Int)(node: Node): Stream[(Long, Node)] = {

    /** Keep the heaviest `count` weighted nodes. */
    def topN(weightedNodes: Stream[(Long, Node)]): Stream[(Long, Node)] = {
      weightedNodes.drop(count).foldLeft(weightedNodes.take(count)) {
        case(ws, w) => (w #:: ws).sortBy(-_._1).take(count)
      }
    }

    fold2[(Long, Stream[(Long, Node)])](node)((0, Stream()))
    // fold children
    { case((weight1, weightedNodes1), (weight2, weightedNodes2)) =>
      (weight1 + weight2, topN(weightedNodes1 #::: weightedNodes2)) }
    // fold parent with folded children
    { case(parentNode, (weight, weightedNodes)) =>
        val parentWeight = weight + weigher(parentNode)
        (parentWeight, topN((parentWeight, parentNode) #:: weightedNodes))
    }._2
  }

  /** Filter retaining only maximal elements wrt. the order induced by the hierarchy
    */
  def filterParents(nodes: Stream[(Long, Node)]): Stream[(Long, Node)] = {
    nodes.filter { case(_, n) => !nodes.exists(_._2.hasParent(n)) }
  }

  /** Reflexive transitive closure over the child nodes of the passed parent node */
  def collectNodes(parent: Node): Iterable[Node] = {
    new Iterable[Node] {
      override def iterator: Iterator[Node] =
        LazyIterators.cons(parent,
          LazyIterators.flatten(parent.nodes.iterator.map(collectNodes(_).iterator)))
    }
  }

  /** All properties on the reflexive transitive closure over the child nodes of the passed parent node */
  def collectProperties(parent: Node): Iterable[Property] =
    collectNodes(parent).flatMap(_.properties)

  /** All values on the reflexive transitive closure over the child nodes of the passed parent node */
  def collectValues(parent: Node): Iterable[Value] =
    collectProperties(parent).flatMap(_.values)

  /** String representation of the path of an item */
  def path(item: Item): String = item match {
    case EMPTY => EMPTY.name
    case n@Node(parent, _, _) => path(parent) + "/" + n.name
    case p@Property(parent, _) => path(parent) + "/" + p.name
    case v@Value(parent, _, _, _, _) => path(parent) + v.name
  }

  /** Depth of an item in the hierarchy of its parents. */
  def depth(item: Item): Int = item match {
    case EMPTY => 0
    case _@Node(parent, _, _) => depth(parent) + 1
    case _@Property(parent, _) => depth(parent) + 1
    case _@Value(parent, _, _, _, _) => depth(parent) + 1
  }

}
