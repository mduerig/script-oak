package michid.script.oak.filestore

import java.util.UUID

import ammonite.ops._
import michid.script.oak.fixture.{EmptyFileStore, fileStoreAnalyser}
import michid.script.oak.nodestore.Items
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileStoreAnalyserTest extends FunSuite {
  object AccessMode extends Enumeration {
    type AccessMode = Value
    val ReadWrite, ReadOnly = Value
  }
  import AccessMode._

  AccessMode.values.foreach { accessMode =>

    def withFSA(test: FileStoreAnalyser => Any): Unit = {
      val directory = EmptyFileStore.path/up
      val fsa = fileStoreAnalyser(directory/"segmentstore", directory/"datastore", accessMode == ReadOnly)
      try {
        test(fsa)
      }
      finally {
        fsa.close()
      }
    }

    test(s"Create file store analyser ($accessMode)") {
      withFSA { identity }
    }

    test(s"Get root node ($accessMode)") {
      withFSA { fsa =>
        val node = fsa.getNode()
        assert(node != null)
        assert(node.isDefined)
      }
    }

    test(s"Get non existing node ($accessMode)") {
      withFSA { fsa =>
        val node = fsa.getNode("/not/there")
        assert(node != null)
        assert(node.isDefined)
        assert(!node.get.exists())
      }
    }

    test(s"Get journal ($accessMode)") {
      withFSA { fsa =>
        val journal = fsa.journal
        assert(journal != null)
        assert(journal.lengthCompare(1) == 0)
      }
    }

    // michid test journal with many entries

    test(s"Get node by id ($accessMode)") {
      withFSA { fsa =>
        val headRevision = fsa.journal.head
        assert(headRevision != null)
        val segmentId = headRevision.segmentId
        val recordNumber = headRevision.recordNumber
        assert(fsa.getNode() == fsa.getNode(segmentId, recordNumber))
      }
    }

    test(s"Get non existing node by id ($accessMode)") {
      withFSA { fsa =>
        val state = fsa.getNode(new UUID(0, 0), 0)
        assert(state.isEmpty)
      }
    }

    test(s"Get changes ($accessMode)") {
      withFSA { fsa =>
        val changes = fsa.changes()
        assert(changes != null)
        assert(changes.isEmpty)
      }
    }

    // michid test changes with many entries and projection

    test(s"Get segments ($accessMode)") {
      withFSA { fsa =>
        val segments = fsa.segments
        assert(segments != null)
        assert(segments.lengthCompare(1) == 0)
      }
    }

    test(s"Get segment ($accessMode)") {
      withFSA { fsa =>
        val expected = fsa.segments.head
        val actual =  fsa.segment(expected.id())
        assert(expected != null)
        assert(actual != null)
        assert(actual.map(_.id()).contains(expected.id))
      }
    }

    // michid test with many segments

    test(s"Get tars ($accessMode)") {
      withFSA { fsa =>
      val tars = fsa.tars
        assert(tars != null)
        assert(tars.nonEmpty)
      }
    }


    test(s"node analyser ($accessMode)") {
      import michid.script.oak._
      withFSA { fsa =>
        val node = fsa.getNode()
        val nodeAnalyser = node.analyse
        assert(node.contains(nodeAnalyser.state))
      }
    }

    test(s"node analyser for non existing node ($accessMode)") {
      import michid.script.oak._
      withFSA { _ =>
        val node = None
        val nodeAnalyser = node.analyse
        assert(nodeAnalyser == Items.EMPTY)
      }
    }

    test(s"segment analyser ($accessMode)") {
      import michid.script.oak._
      withFSA { fsa =>
        val segment = fsa.segments.head
        val segmentAnalyser = segment.analyse
        assert(segment == segmentAnalyser.segment)
      }
    }

    test(s"property analyser ($accessMode)") {
      import michid.script.oak._
      withFSA { fsa =>
        val node = fsa.getNode().analyse
        val propertyAnalyser = michid.script.oak.nodestore.Items.collectProperties(node).headOption
        val property = propertyAnalyser.map(_.state)
        assert(property == propertyAnalyser)
      }
    }

  }
}
