package michid.script.shell

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RunScriptIT extends FunSuite with ScriptRunner {

  test("run script") {
    run {
      """println("foo")"""
    } {
      case (out, err) =>
        assert(err.isEmpty)
        assert(out.startsWith("foo"))
    }
  }

}
