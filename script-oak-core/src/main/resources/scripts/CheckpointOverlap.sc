import $ivy.`michid:script-oak:${project.version}`, michid.script.oak._

import michid.script.oak._
import michid.script.oak.filestore.InMemoryIOMonitor
import michid.script.oak.nodestore.Items._
import org.apache.jackrabbit.oak.api.Type._

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

/** Root paths: the paths of all the checkpoints in chronological order followed by the path
  * of the root.*/
def rootPaths: List[String] = {
  val fs = fileStoreAnalyser(builder = dummyBlobStoreBuilder)
  val sRoot = fs.getNode().analyse
  val checkpoints = (sRoot/"checkpoints").nodes.sortBy(_/("created", DATE)).map(_.name)
  checkpoints.toList.map("checkpoints/" + _)  ++ List("root")
}

/** Segment covers of the root paths. */
def segmentCovers: List[Set[(Long, Long)]] =
  rootPaths.map(segmentCover)

/** Overlap of two segment covers c1 and c2: the percentage of segments shared between
  * c1 and c2. */
def overlap(c1: Set[(Long, Long)], c2: Set[(Long, Long)]): Double =
  c1.intersect(c2).size.toDouble / c1.union(c2).size

/** Overlap of segment covers of adjacent items in covers. */
def overlaps(covers: Seq[Set[(Long, Long)]]): List[Double] = covers.sliding(2).map {
    case List(c1, c2) => overlap(c1, c2)
}.toList
