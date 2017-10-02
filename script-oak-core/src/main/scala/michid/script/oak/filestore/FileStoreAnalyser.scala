package michid.script.oak.filestore

import java.io.Closeable
import java.util.Objects.requireNonNull
import java.util.{Date, UUID}

import michid.script.oak.nodestore.Changes.Change
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Changes, Projection}
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.tooling.filestore.Node.NULL_NODE
import org.apache.jackrabbit.oak.tooling.filestore.{IOMonitor, RecordId, Segment, Store, Tar}

import scala.collection.JavaConverters._

abstract class FileStoreAnalyser(store: Store) extends Closeable {
  requireNonNull(store)

  def setHead(expected: NodeState, head: NodeState): Boolean

  def getNode(path: String = "/"): NodeState

  protected val missingNode: NodeState

  def getNode(id: RecordId): NodeState = {
    val node = store.node(id)
    if (node == NULL_NODE) missingNode
    else store.cast(node, classOf[NodeState])
      .orElse(missingNode)
  }

  def addIOMonitor(ioMonitor: IOMonitor): Closeable =
    store.addIOMonitor(ioMonitor)

  val journal: Stream[JournalEntry] =
    store.journalEntries().asScala.toStream.map(JournalEntry(_))

  def changes(projection: Projection = root): Stream[(Stream[Change], Date)] =
    Changes(journal.map(entry => projection(getNode(entry.rootId))), projection.path) zip journal.map(_.timestamp)

  def segment(id: UUID): Option[Segment] = {
    val value = store.segment(id)
    if (value.isPresent) Some(value.get()) else None
  }

  // michid pimp Segment
  def segments: Stream[Segment] =
    tars.flatMap(_.segmentIds().asScala)
      .flatMap(segment)

  // michid pimp Tar
  val tars: Stream[Tar] =
    store.tars().asScala.toStream

  def collectIOStats[T <: IOMonitor](ioMonitor: => T)(thunk: => Unit): T = {
    val monitor: T = ioMonitor
    val closer = addIOMonitor(monitor)
    try {
      thunk
      monitor
    } finally {
        closer.close()
    }
  }

  override def close(): Unit = store match {
    case closeable: Closeable => closeable.close()
    case _ =>
  }
}
