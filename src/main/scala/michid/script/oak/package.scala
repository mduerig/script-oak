package michid.script

import ammonite.ops._
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore.ReadOnlyStore

/** Common predefs used by script-oak */
package object oak {
  /** open read only store at path */
  def readonlyStore(path: Path) = new ReadOnlyStore(path.toNIO.toFile)

  /** read a script from /scripts */
  def script(name: String) = read! resource/'scripts/name

}
