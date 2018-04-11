package michid.script.oak.nodestore

import java.util.Date

import michid.script.oak.nodestore.Changes._
import michid.script.oak.nodestore.LifeLine.{allNodes, allProperties, mapToSize}
import michid.script.oak.nodestore.ItemStates.propertySize
import org.apache.jackrabbit.oak.api.{PropertyState, Type}
import org.apache.jackrabbit.oak.spi.state.NodeState

/**
  * Life lines as a sequence of lists. Each list pertains to a change set
  * from the initial stream of changes passed to the constructor. The elements
  * of the lists are tuples where the first component is represents a quantification
  * of a change realised by the valueMap passed to the constructor and the
  * second component is a time stamp indicating when the change happened.
  */
class LifeLine (
        changes: Stream[(Stream[Change], Date)],
        valueMap: (Change) => (Long, Long) = mapToSize) {

  /**
    * @return  a new LifeLine instance filtered through the passed filter
    */
  def filter(
        includeProperty: (String, PropertyState) => Boolean = allProperties,
        includeNode: (String, NodeState) => Boolean = allNodes)
  : LifeLine = {
    def filter(changeSet: Stream[Change]) = changeSet filter {
      case PropertyAdded(path, after) => includeProperty(path, after)
      case PropertyRemoved(path, before) => includeProperty(path, before)
      case PropertyChanged(path, before, after) => includeProperty(path, before) || includeProperty(path, after)
      case NodeAdded(path, after) => includeNode(path, after)
      case NodeRemoved(path, before) => includeNode(path, before)
      case NodeChanged(path, before, after) => includeNode(path, before) || includeNode(path, after)
      case _ => false
    }

    val filtered = changes map { case (changeSet, date) => (filter(changeSet), date) }
    new LifeLine(filtered, valueMap)
  }

  /**
    * The changes grouped by path
    */
  lazy val byPath: Map[String, Stream[(Change, Date)]] = {
    val dateInside: Stream[(Change, Date)] =
      changes flatMap { case (changeSet, date) => changeSet.map((_, date)) }

    dateInside groupBy { case (Change(path), _) => path }
  }

  /**
    * @return  the life lines as described in the class comment
    */
  def lifeLines: Iterator[List[(Long, Date)]] = {
    val mapped: Iterable[Stream[((Long, Long), Date)]] =
      byPath.values map(_ map { case (change, date) => (valueMap(change), date) })

    val flatted: Iterable[List[(Long, Date)]] =
      mapped.map(_.foldRight(Nil: List[(Long, Date)]) {
        case (((y1, y2), t), ys) => (y1, t) :: (y2, t) :: ys })

    flatted.iterator
  }

  /**
    * @return  a string executable as MatLab script for plotting the life lines
    */
  def matlabPlot(epoch: Long): String = {
    val filtered: Iterator[List[(Long, Date)]] =
      lifeLines map (_ filter {case (y, _) => y != 0})

    val transposed: Iterator[(List[Long], List[Date])] =
      filtered map(_.foldRight((Nil: List[Long], Nil: List[Date])) {
        case ((y, t), (ys, ts)) => (y :: ys, t :: ts) })

    def mkMatrix(xs: List[Long]): String =
      xs.mkString("[", ",", "]")

    transposed
        .map { case (ys, ts) => (mkMatrix(ys), mkMatrix(ts map (_.getTime - epoch))) }
        .map { case (ys, ts) => "plot(" + ts + "," + ys + ",\".-\"); hold on;" }.mkString("\n")
  }

  /**
    * @return  a sequence of life times of individual items
    */
  def lifeTimes: Iterable[Long] = {
    byPath.values filter
      (_.lengthCompare(1) > 0) map { changes =>            // For each set of changes of size at least 2
        (changes map { _._2.getTime})          // extract time stamp
          .sorted(Ordering[Long].reverse)      // order reverse chronological
    } flatMap (ts =>                           // Combine list of
      ts.sliding(2) map (_.reduce(_ - _)))     // the differences between time stamps
  }
}

object LifeLine {
  val allProperties: (String, PropertyState) => Boolean = (_, _) => true
  val noProperties: (String, PropertyState) => Boolean = (_, _) => false

  val allNodes: (String, NodeState) => Boolean = (_, _) => true
  val noNodes: (String, NodeState) => Boolean = (_, _) => false

  private def size(property: PropertyState): Long = {
    propertySize(skipExternal = true)(property)
  }

  def bigBinary(minSize: Int = 0)(path: String, property: PropertyState): Boolean =
    (property.getType == Type.BINARY || property.getType == Type.BINARIES) && size(property) >= minSize

  def mapToSize(change: Change): (Long, Long) = change match {
    case PropertyAdded(_, after) => (0L, size(after))
    case PropertyRemoved(_, before) => (size(before), 0L)
    case PropertyChanged(_, before, after) => (size(before), size(after))
    case NodeAdded(_, _) => (0L, 0L)
    case NodeRemoved(_, _) => (0L, 0L)
    case NodeChanged(_, _, _) => (0L, 0L)
  }
}
