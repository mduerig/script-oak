import $ivy.`${groupId}:${artifactId}:${project.version}`
import $ivy.`org.sameersingh.scalaplot:scalaplot:latest.integration`

import org.apache.jackrabbit.oak.segment.SegmentId
import michid.script.oak._
import michid.script.oak.nodestore.Items._
import michid.script.oak.filestore.SegmentAnalyser._
import michid.script.oak.filestore.{FileStoreAnalyser, InMemoryIOMonitor}
import org.sameersingh.scalaplot.Implicits._
import org.sameersingh.scalaplot.XYPlotStyle
import org.sameersingh.scalaplot.Style.PointType
import org.sameersingh.scalaplot.XYData
import org.sameersingh.scalaplot.LegendPosX
import org.sameersingh.scalaplot.XYChart

/** Segment usage of the given path: chronologically ordered list of all *data*
  * segments with a flag indicating whether a segment is covered by the given
  * path or not.*/
def segmentUsage(
        path: String,
        fileStoreAnalyserFactory: () => FileStoreAnalyser =
          () => fileStoreAnalyser(builder = dummyBlobStoreBuilder))
    : Stream[(SegmentId, Boolean)] = {
  val fs = fileStoreAnalyserFactory()
  val segmentCover = fs.collectIOStats(InMemoryIOMonitor.apply()) {
    collectValues(fs.getNode(path).analyse).size
  }
  .reads.keySet

  val dataSegments = fs.segments
          .map(_.analyse)
          .filter(isData)
          .sortBy(_.info("t"))
          .map(_.id)

  dataSegments.map(id =>
    (id, segmentCover.contains(id.getMostSignificantBits, id.getLeastSignificantBits)))
}

/** Incidence series (as scalaplot XYData) from a sequence of segment usages. */
def incidenceSeries(segmentUsages: Seq[Seq[(SegmentId, Boolean)]], labels: Seq[String]): XYData = {
  def segmentUsageGraph(segmentUsage: Seq[(SegmentId, Boolean)]): Seq[Double] = {
    segmentUsage.zipWithIndex
            .filter{case((_,b),_) => b}
            .map{case(_, x) => x.toDouble}
  }

  val graphs = segmentUsages.map(segmentUsageGraph)
  (graphs zip labels).zipWithIndex.map {
    case((x, path), y) => x -> Yf(_ => -y - 0.5, label=path, style=XYPlotStyle.Points, pt = Some(PointType.fullO))}
}

/** Incidence series (as scalaplot XYData) for the segment usages of a list of paths. */
def incidenceSeries(
      paths: Seq[String],
      fileStoreAnalyserFactory: () => FileStoreAnalyser =
        () => fileStoreAnalyser(builder = dummyBlobStoreBuilder))
    : XYData =
  incidenceSeries(paths.map(segmentUsage(_, fileStoreAnalyserFactory)), paths)

/** Write a plot in png format */
def writePlot(xyData: XYData, file: Path, showLegend: Boolean = true, range: Option[(Double, Double)] = None, size: Option[(Double, Double)] = Some(1000, 400)): Unit = {
  val chart = xyChart(xyData, x = Axis(range = range), showLegend=showLegend, legendPosX=LegendPosX.Right, size = size)
  output(PNG(file.toIO.getParent + "/", file.toIO.getName), chart)
}