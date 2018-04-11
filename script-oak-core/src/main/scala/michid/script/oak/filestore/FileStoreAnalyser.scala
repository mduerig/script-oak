package michid.script.oak.filestore

import java.io.Closeable
import java.util.Objects.requireNonNull
import java.util.{Optional, UUID}

import michid.script.oak.nodestore.Changes.Change
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Changes, Projection}
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.tooling.filestore.api.{JournalEntry, Segment, SegmentStore, Tar}

import scala.collection.JavaConverters._

abstract class FileStoreAnalyser(store: SegmentStore) extends Closeable {
  requireNonNull(store)

  implicit class AsOption[A](optional: Optional[A]) {
    def asScala: Option[A] = {
      if (optional.isPresent) Some(optional.get)
      else None
    }
  }

  def getNode(path: String = "/"): Option[NodeState] = {
    store.head.asScala.map(Projection(path).apply)
  }

  def getNode(segmentId: UUID, recordNumber: Int): Option[NodeState] = {
    store.node(segmentId, recordNumber).asScala
  }

  val journal: Stream[JournalEntry] = {
    store.journalEntries.asScala.toStream
  }

  def changes(projection: Projection = root): Stream[(Stream[Change], Long)] = {
    val states = journal
        .map(_.getRoot)
        .map(projection)
    val timestamps = journal.map(_.timestamp)

    val changes = Changes(states, projection.path)
    changes.zip(timestamps)
  }

  def segment(id: UUID): Option[Segment] = {
    store.segment(id).asScala
  }

  val tars: Stream[Tar] = {
    store.tars.asScala.toStream
  }

  def segments: Stream[Segment] = {
    tars.flatMap(_.segments.asScala)
  }

  override def close(): Unit = store match {
    case closeable: Closeable => closeable.close()
    case _ =>
  }
}
