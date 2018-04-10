package michid.script.oak.filestore

import java.util.UUID

import org.apache.jackrabbit.oak.tooling.filestore.api.{Record, Segment}

import scala.collection.JavaConverters._

class SegmentAnalyser(val segment: Segment) {
  def id: UUID = segment.id

  def info: Map[String, String] =
    segment.metaData.info.asScala.toMap

  def dump: String = segment.hexDump(true)

  def records: Stream[Record] =
    segment.records.asScala.toStream

  def references: Stream[UUID] = {
    segment.references.asScala.toStream
        .map(_.id())
  }

  override def toString: String = {
    val compacted = if (segment.metaData.compacted) "compacted" else ""

    s"${segment.id()} (${segment.length()} bytes): " +
    s"Version: ${segment.metaData.version}, " +
    s"Generation: ${segment.metaData.generation} / ${segment.metaData.fullGeneration()} / $compacted"
  }

  def isData: Boolean = segment.`type`() == Segment.Type.DATA
  def isBulk: Boolean = segment.`type`() == Segment.Type.BULK
}

object SegmentAnalyser {
  def isDataSegmentId(lsb: Long): Boolean = (lsb >>> 60) == 0xAL
  def isData(id: UUID): Boolean = (id.getLeastSignificantBits >>> 60) == 0xAL
  def isData(segment: Segment): Boolean = isData(segment.id)
  def isBulk(id: UUID): Boolean = !isData(id)
  def isBulk(segment: Segment): Boolean = isBulk(segment.id)
}

