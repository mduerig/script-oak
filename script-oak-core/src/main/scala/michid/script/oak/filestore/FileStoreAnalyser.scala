package michid.script.oak.filestore

import java.io.Closeable
import java.util.Objects.requireNonNull
import java.util.UUID

import michid.script.oak.nodestore.Changes.Change
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Changes, Projection}
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.tooling.filestore.api.{JournalEntry, Segment, SegmentStore, Tar}

import scala.collection.JavaConverters._

abstract class FileStoreAnalyser(store: SegmentStore) extends Closeable {
  requireNonNull(store)

  def getNode(path: String = "/"): NodeState

  protected val missingNode: NodeState

// michid
//  def getNode(id: RecordId): NodeState = {
//    val node = store.node(id)
//    if (node == NULL_NODE) missingNode
//    else store.cast(node, classOf[NodeState])
//      .orElse(missingNode)
//  }

// michid
//  def addIOMonitor(ioMonitor: IOMonitor): Closeable =
//    store.addIOMonitor(ioMonitor)

  val journal: Stream[JournalEntry] =
    store.journalEntries.asScala.toStream

  def changes(projection: Projection = root): Stream[(Stream[Change], Long)] = {
    val states = journal
        .map(_.getRoot)
        .map(projection)
    val timestamps = journal.map(_.timestamp)

    val changes = Changes(states, projection.path)
    changes.zip(timestamps)
  }

  def segment(id: UUID): Option[Segment] = {
    val value = store.segment(id)
    if (value.isPresent) Some(value.get())
    else None
  }

  val tars: Stream[Tar] =
    store.tars.asScala.toStream

  def segments: Stream[Segment] =
    tars.flatMap(_.segments.asScala)

// michid
//  def collectIOStats[T <: IOMonitor](ioMonitor: => T)(thunk: => Unit): T = {
//    val monitor: T = ioMonitor
//    val closer = addIOMonitor(monitor)
//    try {
//      thunk
//      monitor
//    } finally {
//        closer.close()
//    }
//  }

  override def close(): Unit = store match {
    case closeable: Closeable => closeable.close()
    case _ =>
  }
}
