import ammonite.ops._
import michid.script.oak._
import michid.script.oak.fixture._

@main
def main(segmentStore: Path = pwd) {
  val segmentStoreDir = segmentStore/"segmentstore"
  val dataStoreDir = segmentStore/"datastore"
  println(s"Opening segment store at $segmentStoreDir, data store at $dataStoreDir")
  val fs = fileStoreAnalyser(segmentStoreDir, dataStoreDir)
  val superRoot = fs.getNode().analyse
  println(s"superRoot=$superRoot")

  val rootNode = fs.getNode("root");
  println(s"superNode=$rootNode")

  // Analysing the items in this file store
  import michid.script.oak.nodestore.Items._

  // All nodes flat
  val nodes = collectNodes(fs.getNode("root").analyse)
  println(s"nodes=${nodes take 10}")

  // Find all nodes with 2 child nodes
  val binaryNodes = nodes.filter(_.nodes.size == 2)
  println(s"binaryNodes=${binaryNodes take 10}")

  // Group properties by number of values
  val properties = collectProperties(fs.getNode("root").analyse)
  val byPropertyCount = properties.groupBy(_.values.size)
  println(s"byPropertyCount=${byPropertyCount take 10}")

  // Number of nodes with a given number of properties in decreasing order
  val noOfProps = byPropertyCount.mapValues(_.size).toList.sortBy(-_._2)
  println(s"noOfProps=${noOfProps take 10}")

  // Number values per value type
  val values = collectValues(fs.getNode("root").analyse)
  val valuesPerType = values.groupBy(_.tyqe).mapValues(_.size)
  println(s"valuesPerType=${valuesPerType}")

  // Number of bytes per value type
  val bytesPerValue = values.groupBy(_.tyqe).mapValues(_.map(v => v.parent.state.size(v.index)).sum)
  println(s"bytesPerValue=${bytesPerValue}")

  // Checkpoints ordered by creation time
  import org.apache.jackrabbit.oak.api.Type._
  val checkpoints = (superRoot / "checkpoints").nodes.sortBy(_ / ("created", DATE)).map(_ / "root")

  // Turnover across checkpoints
  import michid.script.oak.nodestore.Changes
  val roots = checkpoints :+ superRoot / "root"
  val changes = Changes(roots.map(_.state), "")
  val changeTurnover = changes.map(Changes.turnOver(_))
}

try {
  main()
} catch {
  case e => {
    println("\nError running FileStoreDemo. Try passing the repository path to main. " +
            "E.g. FileStoreDemo.main(<path to repository>)")
  }
}

