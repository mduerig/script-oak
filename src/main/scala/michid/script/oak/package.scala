package michid.script

import ammonite.ops._
import michid.script.oak.filestore.FileStoreAnalyser
import org.apache.jackrabbit.oak.spi.blob.BlobStore

/** Common predefs used by script-oak */
package object oak {

  /** Create a new file store analyser */
  def fileStoreAnalyser(
       directory: Path,
       blobStore: Option[BlobStore] = None,
       readOnly: Boolean = true): FileStoreAnalyser =
    new FileStoreAnalyser(directory, blobStore, readOnly)

  /** read a script from /scripts */
  def script(name: String) = read! resource/'scripts/name
}
