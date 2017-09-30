package michid.script.oak

import java.io.Closeable

import ammonite.ops.{Path, pwd}
import michid.script.oak.filestore.FileStoreAnalyser
import org.apache.jackrabbit.oak.plugins.blob.datastore.{DataStoreBlobStore, OakFileDataStore}
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder.fileStoreBuilder
import org.apache.jackrabbit.oak.segment.file.tooling.BasicReadOnlyBlobStore
import org.apache.jackrabbit.oak.segment.tooling.{FileStoreWrapper, IOMonitorBridge}
import org.apache.jackrabbit.oak.spi.blob.BlobStore
import org.apache.jackrabbit.oak.tooling.filestore.Store

/** Common predefs used by script-oak */
package object fixture {

  /** A blob store that actually ignores the binaries. Useful if the blob store is not available  */
  val dummyBlobStore: BlobStore = new BasicReadOnlyBlobStore

  /** A plain file store builder that can be customised and passed to pass to fileStoreAnalyser */
  val plainFileStoreBuilder: Path => FileStoreBuilder =
    path => fileStoreBuilder(path.toNIO.toFile)

  /** A customised file store builder, which configures a dummyBlobStore */
  val dummyBlobStoreBuilder: Path => FileStoreBuilder =
    plainFileStoreBuilder(_).withBlobStore(dummyBlobStore)

  /** A file data store based blob store */
  def newBlobStore(directory: Path): BlobStore = {
    val delegate = new OakFileDataStore
    delegate.setPath(directory.toString)
    delegate.init(null)
    new DataStoreBlobStore(delegate)
  }

  /** Create a new file store analyser. If no data store exists at the given path the
    * segment store is created without an external data store unless one is specified
    * within the passed builder. */
  def fileStoreAnalyser(
     segmentStoreDirectory: Path = pwd / "segmentstore",
     dataStoreDirectory: Path = pwd / "datastore",
     readOnly: Boolean = true,
     builder: Path => FileStoreBuilder = plainFileStoreBuilder)
  : FileStoreAnalyser = {
    val fileStoreBuilder = builder(segmentStoreDirectory)
    if (dataStoreDirectory.toIO.exists())
      fileStoreBuilder.withBlobStore(newBlobStore(dataStoreDirectory))

    val iOMonitor = new IOMonitorBridge
    var toolAPI: Store = null
    fileStoreBuilder.withProbes((fileStoreProbe, tarProbe) =>
      toolAPI = new FileStoreWrapper(fileStoreProbe, tarProbe, iOMonitor.addIOMonitor(_)))

    val fileStore = if (readOnly)
      fileStoreBuilder.buildReadOnly() else
      fileStoreBuilder.build()

    new FileStoreAnalyser(toolAPI) with Closeable {
      override def close(): Unit = {
        super.close()
        fileStore.close()
      }
    }
  }
}
