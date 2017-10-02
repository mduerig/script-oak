package michid.script.oak.filestore

import ammonite.ops._
import michid.script.oak.fixture.{EmptyFileStore, fileStoreAnalyser}
import org.apache.jackrabbit.oak.api.Type.LONG
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
        assert(node.exists)
      }
    }

    test(s"Get non existing node ($accessMode)") {
      withFSA { fsa =>
        val node = fsa.getNode("/not/there")
        assert(node != null)
        assert(!node.exists)
      }
    }

    test(s"Set head ($accessMode)") {
      withFSA { fsa =>
        val initialHead = fsa.getNode()
        assert(initialHead.getProperty("s") == null)
        val builder = initialHead.builder()
        builder.setProperty("s", 42)
        if (accessMode == ReadWrite) {
          assert(fsa.setHead(initialHead, builder.getNodeState))
          val property = fsa.getNode().getProperty("s")
          assert(property != null)
          assert(property.getValue(LONG) == 42)
        } else try {
            fsa.setHead(initialHead, builder.getNodeState)
            fail()
        } catch {
          case _: UnsupportedOperationException =>
        }
      }
    }

    test(s"Get journal ($accessMode)") {
      withFSA { fsa =>
        val journal = fsa.journal
        assert(journal != null)
        assert(journal.size == 1)
      }
    }

    // michid test journal with many entries

    test(s"Get node by id ($accessMode)") {
      withFSA { fsa =>
        val id = fsa.journal.head.rootId
        assert(id != null)
        assert(fsa.getNode() == fsa.getNode(id))
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
        assert(segments.size == 1)
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
        assert(tars.size == (if (accessMode == ReadOnly) 1 else 2))
      }
    }

    // michid test collectIOStats
  }
}
