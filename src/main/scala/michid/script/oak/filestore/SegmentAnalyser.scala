package michid.script.oak.filestore

import java.util.UUID

import scala.collection.JavaConverters._

import michid.script.oak.filestore.SegmentAnalyser.Record
import org.apache.jackrabbit.oak.commons.json.{JsonObject, JsopTokenizer}
import org.apache.jackrabbit.oak.segment.{RecordType, Segment, SegmentId}
import org.apache.jackrabbit.oak.segment.Segment.RecordConsumer
import org.apache.jackrabbit.oak.segment.SegmentId.isDataSegmentId

import scala.collection.mutable

class SegmentAnalyser(val segment: Segment) {
  def id: SegmentId = segment.getSegmentId

  def info: Map[String, String] = {
    val tokenizer = new JsopTokenizer(segment.getSegmentInfo)
    tokenizer.read('{')
    val properties = JsonObject.create(tokenizer).getProperties
    properties.asScala.toMap
  }

  def dump: String = segment.toString

  def records: List[Record] = {
    val rs = mutable.ArrayBuffer.empty[Record]
    segment.forEachRecord(new RecordConsumer {
      override def consume(number: Int, tyqe: RecordType, offset: Int): Unit =
        rs += Record(segment, number, tyqe, offset)
    })
    rs.toList
  }

  def references(implicit fileStoreAnalyser: FileStoreAnalyser): Iterable[SegmentAnalyser] = {
    val uuids =
      (0 until segment.getReferencedSegmentIdCount)
            .map(segment.getReferencedSegmentId)

    uuids.map{ uuid => new SegmentAnalyser(
      fileStoreAnalyser.store.newSegmentId(
        uuid.getMostSignificantBits, uuid.getLeastSignificantBits).getSegment)}
  }

  override def toString: String =
    s"${segment.getSegmentId} (${segment.size()}) bytes: ${segment.getSegmentInfo}, " +
    s"Generation: ${segment.getGcGeneration}, Version: ${segment.getSegmentVersion}"
}

object SegmentAnalyser {
  case class Record(segment: Segment, number: Int, tyqe: RecordType, offset: Int) {
    override def toString: String =
      f"${segment.getSegmentId} $tyqe $number%08x: $offset%08x"
  }

  def isData(id: UUID): Boolean = isDataSegmentId(id.getLeastSignificantBits)
  def isData(id: SegmentId): Boolean = isData(id.asUUID())
  def isData(segment: Segment): Boolean = isData(segment.getSegmentId)
  def isData(segmentAnalyser: SegmentAnalyser): Boolean = isData(segmentAnalyser.segment)

  def isBulk(id: UUID): Boolean =  !isData(id)
  def isBulk(id: SegmentId): Boolean = isBulk(id.asUUID())
  def isBulk(segment: Segment): Boolean = isBulk(segment.getSegmentId)
  def isBulk(segmentAnalyser: SegmentAnalyser): Boolean = isBulk(segmentAnalyser.segment)
}