package michid.script.shell

import michid.script.oak.fixtures.oakFixtures
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SegmentUsageIT extends FunSuite with ScriptRunner {
  oakFixtures.foreach(oakFixture => {

    ignore(s"Run SegmentUsage.sc script ($oakFixture)") { // michid enable
      run(oakFixture) {
        """script("SegmentUsage.sc").run"""
      } {
        case (out, err) =>
          assert(err.isEmpty)
          assert(out.isEmpty)
      }
    }

    // TODO: Exercise deeper tests of the individual methods exposed by SegmentUsage.sc
  })
}
