package michid.script.oak.filestore

import java.io.Closeable
import java.util.{Date, Optional, UUID}

import michid.script.oak.nodestore.Changes.Change
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Changes, Projection}
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.tooling.filestore.{IOMonitor, RecordId, Segment, Store, Tar}

import scala.collection.JavaConverters._

class FileStoreAnalyser(store: Store) {

  private def nodeState(id: RecordId): NodeState = ??? // michid inject

  def addIOMonitor(ioMonitor: IOMonitor): Closeable =
    store.addIOMonitor(ioMonitor)

  // michid wrap
  def getNode(path: String = "/"): NodeState =
    Projection(path)(nodeState(journal.head.rootId))

  val journal: Stream[JournalEntry] =
    store.journalEntries().asScala.toStream.map(JournalEntry(_))

  // michid wrap
  def changes(projection: Projection = root): Stream[(Stream[Change], Date)] =
    Changes(journal.map(entry => projection(nodeState(entry.rootId))), projection.path) zip journal.map(_.timestamp)

  def segment(id: UUID): Option[Segment] = {
    asOption(store.segment(id))
  }

  private def asOption[T](value: Optional[T]): Option[T] =
    if (value.isPresent) Some(value.get)
    else None

  // michid wrap Segment
  def segments: Stream[Segment] = {
    tars.flatMap(_.segmentIds().asScala)
      .flatMap(segment)
  }

  // michid wrap Tar
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
}
