package michid.script.shell

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileStoreDemoIT extends FunSuite with ScriptRunner {

  test("Run FileStoreDemo without a repository") {
    run {
      """script("FileStoreDemo.sc").run"""
    } {
      case (out, err) =>
        assert(err.isEmpty)
        assert(out.contains(
          "Error running FileStoreDemo. Try passing the repository path to main. E.g. FileStoreDemo.main(<path to repository>)"))
    }
  }

  ignore("Run FileStoreDemo with an empty repository") { // michid enable: script needs fixing
    run {
      """
        |import michid.script.oak.fixture.EmptyFileStore._
        |script("FileStoreDemo.sc").run
        |@
        |main(path/up)
      """.stripMargin
    } {
      case (out, err) =>
        assert(err.isEmpty)
        assert(out.contains("superRoot=/ @ { root : { } }"))
        assert(out.contains("superNode={ }"))
        assert(out.contains("nodes=List(/ @ { })"))
        assert(out.contains("binaryNodes=List()"))
        assert(out.contains("byPropertyCount=Map()"))
        assert(out.contains("noOfProps=List()"))
        assert(out.contains("valuesPerType=Map()"))
        assert(out.contains("bytesPerValue=Map()"))
    }
  }
}
