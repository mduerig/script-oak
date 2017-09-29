package michid.script.oak.fixture

import ammonite.ops._

import scala.util.Random

object EmptyFileStore {
  val path: Path = {
    val path = pwd / "target" / Random.nextInt(1000000).toString / "segmentstore"
    fileStoreAnalyser(path, readOnly = false).close()
    path
  }
}
