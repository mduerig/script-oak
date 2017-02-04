package michid.script.oak.filestore

import java.io.File
import java.util.Date

import scala.collection.JavaConverters._
import ammonite.ops.{Path, ls}
import michid.script.oak.nodestore.Changes.Change
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Changes, Projection}
import org.apache.jackrabbit.oak.segment.Segment
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder.fileStoreBuilder
import org.apache.jackrabbit.oak.segment.file._
import org.apache.jackrabbit.oak.spi.blob.BlobStore
import org.apache.jackrabbit.oak.spi.state.NodeState

class FileStoreAnalyser(
       val directory: Path,
       val blobStore: Option[BlobStore] = None,
       val readOnly: Boolean = true) {

  val ioMonitorTracker = new IOMonitor {
    @volatile
    var ioMonitor: IOMonitor = new IOMonitorAdapter

    override def onSegmentRead(file: File, msb: Long, lsb: Long, length: Int): Unit =
      ioMonitor.onSegmentRead(file, msb, lsb, length)
  }

  val eitherStore: Either[FileStore, ReadOnlyFileStore] = {
    val builder = fileStoreBuilder(directory.toNIO.toFile)
            .withIOMonitor(ioMonitorTracker)
    blobStore.foreach(blobStore => builder.withBlobStore(blobStore))
    if (readOnly) Right(builder.buildReadOnly())
    else Left(builder.build())
  }

  val store: AbstractFileStore =
    eitherStore.fold(rw => rw, ro => ro)

  val readOnlyStore: Option[ReadOnlyFileStore] =
    eitherStore.fold(rw => None, ro => Some(ro))

  val readWriteStore: Option[FileStore] =
    eitherStore.fold(rw => Some(rw), ro => None)

  def setIOMonitor(ioMonitor: IOMonitor): Unit =
    ioMonitorTracker.ioMonitor = ioMonitor

  def getNode(path: String = "/"): NodeState =
    Projection(path)(store.getHead)

  val journal: Journal = {
    val entries = Journal.entries(directory/"journal.log")
    val ids = Journal.ids(entries map (_._1), store)
    val roots = Journal.nodes(ids, store.getReader)
    new Journal(entries, ids, roots)
  }

  def changes(projection: Projection = root): Stream[(Stream[Change], Date)] =
    Changes(journal.roots map projection, projection.path) zip (journal.entries map (_._2))

  def segments: Stream[Segment] = readOnlyStore.map {
      _.getSegmentIds.asScala.toStream.map(_.getSegment)
    }.getOrElse(sys.error("Cannot iterate segment on a r/w store"))

  val tars: Iterable[Tar] =
    (ls ! directory) |? (_.ext == "tar") | (Tar(_))

  def collectIOStats[T <: IOMonitor](ioMonitor: => T)(thunk: => Unit): T = {
    val monitor: T = ioMonitor
    setIOMonitor(monitor)
    try {
      thunk
      monitor
    } catch {
      case e: Throwable =>
        setIOMonitor(new IOMonitorAdapter)
        throw e
    }
  }
}
