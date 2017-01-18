package michid.script.oak.filestore

import java.util.Date

import ammonite.ops.Path
import org.apache.jackrabbit.oak.segment.file.AbstractFileStore
import org.apache.jackrabbit.oak.segment.{RecordId, SegmentNodeState, SegmentReader}

import scala.io.Source
import scala.util.matching.Regex

/**
  * Revision from a journal file
  */
case class Journal(
    entries: Iterable[(String, Date)],
    ids: Iterable[RecordId],
    roots: Iterable[SegmentNodeState]) {
}

object Journal {
  val revision: Regex = """([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}:\d+) root (\d+)""".r

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
