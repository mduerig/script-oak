import $ivy.`michid:script-oak:1.3+`, michid.script.oak._

import michid.script.oak._
import michid.script.oak.nodestore.Items._
import michid.script.oak.filestore.InMemoryIOMonitor
import michid.script.oak.filestore.SegmentAnalyser._
import org.apache.jackrabbit.oak.segment.SegmentId

import scala.collection.Set

/** Calculate the segment cover for that given path. The segment cover includes exactly
  * those segments that need to be read from when traversing all items on the given path. */
def segmentCover(path: String): Set[(Long, Long)] = {
  val fs = fileStoreAnalyser(builder = dummyBlobStoreBuilder)
  fs.collectIOStats(InMemoryIOMonitor.apply()) {
    collectValues(fs.getNode(path).analyse).size
  }
  .reads.keySet
}

/** Segment usage of the given path: ordered list of all *data* segments with a flag indicating
  * whether a segment is covered by the given path or not.*/
def segmentUsage(path: String): Stream[(SegmentId, Boolean)] = {
  val fs = fileStoreAnalyser(builder = dummyBlobStoreBuilder)
  val cover = segmentCover(path)
  val dataSegments = fs.segments
          .map(_.analyse)
          .filter(isData)
          .sortBy(_.info("t"))
          .map(_.id)

  dataSegments.map(id =>
    (id, cover.contains(id.getMostSignificantBits, id.getLeastSignificantBits)))
}

/** Indexes of the used segments wrt. to the list of all *data* segments. */
def indexOfUsedSegments(path: String): Stream[Int] =
  segmentUsage(path)
          .map(_._2)
          .zipWithIndex.filter(_._1)
          .map(_._2)
