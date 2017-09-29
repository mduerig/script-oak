package michid.script.shell

import michid.script.oak.fixtures.oakFixtures
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RunScriptIT extends FunSuite with ScriptRunner {
  oakFixtures.values.foreach(oakFixture => {

    test(s"run script ($oakFixture)") {
      run(oakFixture) {
        """println("foo")"""
      } {
        case (out, err) =>
          assert(err.isEmpty)
          assert(out.startsWith("foo"))
      }
    }

    test(s"run script through interpreter ($oakFixture)") {
      run(oakFixture) {
        """"println(42)".run"""
      } {
        case (out, err) =>
          assert(err.isEmpty)
          assert(out.startsWith("42"))
      }
    }

  })
}
