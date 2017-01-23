package michid.script

import ammonite.ops.{Path, pwd, read, resource}
import michid.script.oak.filestore.{FileStoreAnalyser, SegmentAnalyser}
import michid.script.oak.nodestore.Items.{EMPTY, Node, Property}
import org.apache.jackrabbit.oak.api.PropertyState
import org.apache.jackrabbit.oak.plugins.blob.datastore.{DataStoreBlobStore, OakFileDataStore}
import org.apache.jackrabbit.oak.segment.Segment
import org.apache.jackrabbit.oak.segment.file.tooling.BasicReadOnlyBlobStore
import org.apache.jackrabbit.oak.spi.blob.BlobStore
import org.apache.jackrabbit.oak.spi.state.NodeState

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

  /** Create a new file store analyser. If no data store exists at the given path the
    * segment store is created without an external data store. */
  def fileStoreAnalyser(
        segmentStoreDirectory: Path = pwd / "segmentstore",
        dataStoreDirectory: Path = pwd / "datastore",
        readOnly: Boolean = true)
  : FileStoreAnalyser = {
    val blobStore =
      if (dataStoreDirectory.toIO.exists()) Some(newBlobStore(dataStoreDirectory))
      else None
    new FileStoreAnalyser(segmentStoreDirectory, blobStore, readOnly)
  }

  /** read a script from /scripts */
  def script(name: String): String = read! resource/'scripts/name

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
