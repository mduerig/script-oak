package michid.script.oak.filestore

import java.io.File
import java.lang.System.currentTimeMillis
import java.util.UUID

import ammonite.ops.Path

import scala.collection.concurrent.{Map, TrieMap}
import scala.collection.mutable.ListBuffer

// michid IOMonitor support
/**
  * In memory implementation of an IOMonitor mentoring read accesses to
  * segments.
  *
  */
class InMemoryIOMonitor /*extends IOMonitor*/ {
//  val reads: Map[(Long, Long), SegmentAccess] = TrieMap()
//
//  override def beforeSegmentRead(file: File, msb: Long, lsb: Long, length: Int): Unit = {
//    reads.getOrElseUpdate((msb, lsb), SegmentAccess(Path(file), msb, lsb))
//            .accessed(currentTimeMillis())
//  }
//
//  override def afterSegmentRead(file: File, msb: Long, lsb: Long, length: Int, elapsed: Long): Unit = {}
//  override def beforeSegmentWrite(file: File, msb: Long, lsb: Long, length: Int): Unit = {}
//  override def afterSegmentWrite(file: File, msb: Long, lsb: Long, length: Int, elapsed: Long): Unit = {}
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