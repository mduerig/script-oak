package michid.script.shell

import michid.script.oak.fixtures.oakFixtures
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileStoreDemoIT extends FunSuite with ScriptRunner {
  oakFixtures.values.foreach(oakFixture => {

    test(s"Run FileStoreDemo without a repository ($oakFixture)") {
      run(oakFixture) {
        """script("FileStoreDemo.sc").run"""
      } {
        case (out, err) =>
          assert(err.isEmpty)
          assert(out.contains(
            "Error running FileStoreDemo. Try passing the repository path to main. E.g. FileStoreDemo.main(<path to repository>)"))
      }
    }

    test(s"Run FileStoreDemo with an empty repository ($oakFixture)") {
      run(oakFixture) {
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

  })

}
