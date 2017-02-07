package michid.script

import ammonite.ops.{Path, pwd, read, resource}
import ammonite.runtime.InterpBridge
import michid.script.oak.filestore.{FileStoreAnalyser, SegmentAnalyser}
import michid.script.oak.nodestore.Items.{EMPTY, Node, Property}
import org.apache.jackrabbit.oak.api.PropertyState
import org.apache.jackrabbit.oak.plugins.blob.datastore.{DataStoreBlobStore, OakFileDataStore}
import org.apache.jackrabbit.oak.segment.Segment
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder.fileStoreBuilder
import org.apache.jackrabbit.oak.segment.file.tooling.BasicReadOnlyBlobStore
import org.apache.jackrabbit.oak.spi.blob.BlobStore
import org.apache.jackrabbit.oak.spi.state.NodeState

/** Common predefs used by script-oak */
package object oak {

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
    if (dataStoreDirectory.toIO.exists())
      new FileStoreAnalyser(segmentStoreDirectory, readOnly,
        builder(_).withBlobStore(newBlobStore(dataStoreDirectory)))
    else
      new FileStoreAnalyser(segmentStoreDirectory, readOnly, builder)
  }

  /** read a script from /scripts */
  def script(name: String): String = read! resource/'scripts/name

  /** Execute a string as a script */
  implicit class RunScript(script: String) {
    def run(): Unit = InterpBridge.value0.load(script)
  }

  implicit class AsNode(node: NodeState) {
    def analyse = new Node(node)
  }

  implicit class AsProperty(property: PropertyState) {
    def analyse = Property(EMPTY, property)
  }

  implicit class AsSegmentAnalyser(segment: Segment) {
    def analyse = new SegmentAnalyser(segment)
  }

}
