package michid.script.oak.filestore

import michid.script.oak.fixture.{EmptyFileStore, fileStoreAnalyser}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import ammonite.ops._

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

    test(s"Get journal ($accessMode)") {
      withFSA { fsa =>
        val journal = fsa.journal
        assert(journal != null)
        assert(journal.size == 1)
      }
    }

    // michid test journal with many entries

    test(s"Get changes ($accessMode)") {
      withFSA { fsa =>
        val changes = fsa.changes()
        assert(changes != null)
        assert(changes.isEmpty)
      }
    }

    // michid test changes with many entries and projection

  }
}
