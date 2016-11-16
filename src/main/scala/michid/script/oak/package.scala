package michid.script

import ammonite.ops._
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder.fileStoreBuilder

/** Common predefs used by script-oak */
package object oak {
  /** open read only store at path */
  def readonlyStore(path: Path) = fileStoreBuilder(path.toNIO.toFile).buildReadOnly()

  /** read a script from /scripts */
  def script(name: String) = read! resource/'scripts/name

}
