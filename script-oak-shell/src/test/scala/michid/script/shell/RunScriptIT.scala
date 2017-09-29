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

  test("run script through interpreter") {
    run {
      """"println(42)".run"""
    } {
      case (out, err) =>
        assert(err.isEmpty)
        assert(out.startsWith("42"))
    }
  }

}
