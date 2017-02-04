package michid.script.oak.filestore

import java.io.File
import java.lang.System.currentTimeMillis
import java.util.UUID

import ammonite.ops.Path
import org.apache.jackrabbit.oak.segment.file.IOMonitor

import scala.collection.concurrent.{Map, TrieMap}
import scala.collection.mutable.ListBuffer

/**
  * In memory implementation of an IOMonitor mentoring read accesses to
  * segments.
  */
class InMemoryIOMonitor extends IOMonitor{
  val reads: Map[(Long, Long), SegmentAccess] = TrieMap()

  override def onSegmentRead(file: File, msb: Long, lsb: Long, length: Int): Unit = {
    reads.getOrElseUpdate((msb, lsb), SegmentAccess(Path(file), msb, lsb))
            .accessed(currentTimeMillis())
  }
}

case class SegmentAccess(path: Path, lsb: Long, msb: Long) {
  val timeStamps: ListBuffer[Long] = ListBuffer.empty

  def accessed(ts: Long): Unit = this.synchronized {
    timeStamps += ts
  }

  override def toString: String =
    s"${new UUID(msb, lsb)} @ ${path.name}: ${timeStamps.mkString(",")}"
}

object InMemoryIOMonitor {
  def apply() = new InMemoryIOMonitor
}