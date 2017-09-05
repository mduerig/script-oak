package michid.script.oak.filestore

import java.io.File
import java.util.Date

import ammonite.ops.{Path, ls}
import michid.script.oak.nodestore.Changes.Change
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Changes, Projection}
import org.apache.jackrabbit.oak.segment.Segment
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder.fileStoreBuilder
import org.apache.jackrabbit.oak.segment.file.{AbstractFileStore, FileStore, FileStoreBuilder, ReadOnlyFileStore}
import org.apache.jackrabbit.oak.segment.file.tar.{IOMonitor, IOMonitorAdapter}
import org.apache.jackrabbit.oak.spi.state.NodeState

import scala.collection.JavaConverters._

class FileStoreAnalyser(
        directory: Path,
        readOnly: Boolean = true,
        builder: Path => FileStoreBuilder = path => fileStoreBuilder(path.toNIO.toFile)) {

  val ioMonitorTracker = new IOMonitorAdapter {
    @volatile
    var ioMonitor: IOMonitor = new IOMonitorAdapter

    override def beforeSegmentRead(file: File, msb: Long, lsb: Long, length: Int): Unit =
      ioMonitor.beforeSegmentRead(file, msb, lsb, length)
  }

  val eitherStore: Either[FileStore, ReadOnlyFileStore] = {
    val fsb = builder(directory).withIOMonitor(ioMonitorTracker)
    if (readOnly) Right(fsb.buildReadOnly())
    else Left(fsb.build())
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
    } finally {
        setIOMonitor(new IOMonitorAdapter)
    }
  }
}
