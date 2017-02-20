package michid.script.shell

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CheckpointOverlapIT extends FunSuite with ScriptRunner {

  test("Run CheckpointOverlap.sc script") {
    run {
      """script("CheckpointOverlap.sc").run"""
    } {
      case (out, err) =>
        assert(err.isEmpty)
        assert(out.isEmpty)
    }
  }

  // TODO: Exercise deeper tests of the individual methods exposed by CheckpointOverlap.sc
}
