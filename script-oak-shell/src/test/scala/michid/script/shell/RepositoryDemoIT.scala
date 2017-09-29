package michid.script.shell

import michid.script.oak.fixtures.oakFixtures
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RepositoryDemoIT extends FunSuite with ScriptRunner {
  oakFixtures.foreach(oakFixture => {

    ignore(s"Run RepositoryDemo ($oakFixture)") { // michid enable
      run(oakFixture) {
        """script("RepositoryDemo.sc").run """
      } {
        case (out, err) =>
          assert(err.isEmpty)
          assert(out.contains(
            "root=Node[NodeDelegate{tree=/foo: { bar = 42, jcr:primaryType = nt:unstructured}}]"))
      }
    }

  })
}
