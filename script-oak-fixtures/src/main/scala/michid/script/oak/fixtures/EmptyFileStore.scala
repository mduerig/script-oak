package michid.script.oak.fixtures

import scala.util.Random
import ammonite.ops._
import michid.script.oak._

object EmptyFileStore {
  val path: Path = {
    val path = pwd / "target" / Random.nextInt(1000000).toString / "segmentstore"
    fileStoreAnalyser(path, readOnly = false).store.close()
    path
  }
}
