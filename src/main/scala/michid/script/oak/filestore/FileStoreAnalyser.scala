package michid.script.oak.filestore

import java.util.Date

import ammonite.ops.{Path, ls}
import michid.script.oak.nodestore.Changes.Change
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Changes, Projection}
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder.fileStoreBuilder
import org.apache.jackrabbit.oak.segment.file.{AbstractFileStore, FileStore, ReadOnlyFileStore}
import org.apache.jackrabbit.oak.spi.blob.BlobStore
import org.apache.jackrabbit.oak.spi.state.NodeState

class FileStoreAnalyser(
       val directory: Path,
       val blobStore: Option[BlobStore] = None,
       val readOnly: Boolean = true) {

  val eitherStore: Either[FileStore, ReadOnlyFileStore] = {
    val builder = fileStoreBuilder(directory.toNIO.toFile)
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

  /* TODO implement segments */
  def segments = ???

  val tars: Iterable[Tar] =
    (ls ! directory) |? (_.ext == "tar") | (Tar(_))
}
