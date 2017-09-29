package michid.script.shell

import michid.script.oak.fixtures.oakFixtures
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CheckpointOverlapIT extends FunSuite with ScriptRunner {
  oakFixtures.foreach(oakFixture => {

    ignore(s"Run CheckpointOverlap.sc script ($oakFixture)") { // michid enable
      run(oakFixture) {
        """script("CheckpointOverlap.sc").run"""
      } {
        case (out, err) =>
          assert(err.isEmpty)
          assert(out.isEmpty)
      }
    }

    // TODO: Exercise deeper tests of the individual methods exposed by CheckpointOverlap.sc
  })
}
