package michid.script

import ammonite.ops._
import michid.script.oak.filestore.FileStoreAnalyser
import org.apache.jackrabbit.oak.plugins.blob.datastore.{DataStoreBlobStore, OakFileDataStore}
import org.apache.jackrabbit.oak.segment.file.tooling.BasicReadOnlyBlobStore
import org.apache.jackrabbit.oak.spi.blob.BlobStore

/** Common predefs used by script-oak */
package object oak {

  /** A blob store that actually ignores the binaries. Useful if the blob store is not available  */
  val dummyBlobStore: BlobStore = new BasicReadOnlyBlobStore

  /** A file data store based blob store */
  def newBlobStore(directory: Path): BlobStore = {
    val delegate = new OakFileDataStore
    delegate.setPath(directory.toString)
    delegate.init(null)
    new DataStoreBlobStore(delegate)
  }

  /** Create a new file store analyser */
  def fileStoreAnalyser(
       directory: Path,
       blobStore: Option[BlobStore] = None,
       readOnly: Boolean = true): FileStoreAnalyser =
    new FileStoreAnalyser(directory, blobStore, readOnly)

  /** read a script from /scripts */
  def script(name: String) = read! resource/'scripts/name
}
