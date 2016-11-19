package michid.script.oak.filestore

import java.util.Date

import ammonite.ops.Path
import org.apache.jackrabbit.oak.segment.file.AbstractFileStore
import org.apache.jackrabbit.oak.segment.{RecordId, SegmentNodeState, SegmentReader}

import scala.io.Source

/**
  * Revision from a journal file
  */
class Journal(val storeAnalyser: FileStoreAnalyser) {
  val entries: Iterable[(String, Date)] = Journal.entries(storeAnalyser.directory/"journal.log")
  val ids: Iterable[RecordId] = Journal.ids(entries map (_._1), storeAnalyser.store)
  val nodes: Iterable[SegmentNodeState] = Journal.nodes(ids, storeAnalyser.store.getReader)
}

object Journal {
  val revision = """([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}:\d+) root (\d+)""".r

  def entries(journal: Path): Iterable[(String, Date)] = new Iterable[(String, Date)] {
    override def iterator: Iterator[(String, Date)] =
      Source.fromFile(journal.toNIO.toFile).getLines map {
      case revision(recordId, timestamp) => (recordId, new Date(timestamp.toLong))
    }
  }

  def ids(lines: Iterable[String], store: AbstractFileStore): Iterable[RecordId] =
    lines.map(line => RecordId.fromString(store, line))

  def nodes(recordIds: Iterable[RecordId], reader: SegmentReader): Iterable[SegmentNodeState] =
    recordIds.map(id => reader.readNode(id))
}
