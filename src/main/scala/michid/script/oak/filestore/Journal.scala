package michid.script.oak.filestore

import ammonite.ops.Path
import org.apache.jackrabbit.oak.segment.{RecordId, SegmentNodeState, SegmentNotFoundException, SegmentReader}
import org.apache.jackrabbit.oak.segment.file.{AbstractFileStore, JournalReader}

import scala.collection.JavaConversions._
import scala.util.control.Exception.catching

/**
  * Revision from a journal file
  */
class Journal(val storeAnalyser: FileStoreAnalyser) {
  val lines: Iterable[String] = Journal.entries(storeAnalyser.directory/"journal.log")
  val ids: Iterable[RecordId] = Journal.ids(lines, storeAnalyser.store)
  val nodes: Iterable[Option[SegmentNodeState]] = Journal.nodes(ids, storeAnalyser.store.getReader)
}

object Journal {
  def entries(journal: Path): Iterable[String] = new Iterable[String] {
    override def iterator: Iterator[String] = new JournalReader(journal.toNIO.toFile)
  }

  def ids(lines: Iterable[String], store: AbstractFileStore): Iterable[RecordId] =
    lines.map(line => RecordId.fromString(store, line))

  def nodes(recordIds: Iterable[RecordId], reader: SegmentReader): Iterable[Option[SegmentNodeState]] =
    recordIds.map(id => catching(classOf[SegmentNotFoundException])
                          .opt(reader.readNode(id)))
}
