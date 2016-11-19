package michid.script.oak.filestore

import ammonite.ops.{Path, ls}
import michid.script.oak.nodestore.Projection.root
import michid.script.oak.nodestore.{Change, Changes, Projection}
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder.fileStoreBuilder
import org.apache.jackrabbit.oak.segment.file.{AbstractFileStore, FileStore, ReadOnlyFileStore}
import org.apache.jackrabbit.oak.spi.blob.BlobStore

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

  val journal: Journal =
    new Journal(this)

  def changes(projection: Projection = root): Stream[Stream[Change]] =
    Changes(journal.nodes map projection, projection.path)

  /* TODO implement segments */
  def segments = ???

  val tars: Iterable[Tar] =
    (ls ! directory) |? (_.ext == "tar") | (Tar(_))
}
