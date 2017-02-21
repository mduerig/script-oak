import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Outcome, fixture}

@RunWith(classOf[JUnitRunner])
class ScriptOakIntegrationTest extends fixture.FunSuite {
  type FixtureParam = String

  override def withFixture(test: OneArgTest): Outcome = {
    val fixture = "a fixture object"
    try test(fixture)
    finally {}
  }

  test("Main.run") { _ => // michid can't get redirecting io to work properly
    val i = new ByteArrayInputStream("println(42)\nexit\n".getBytes(StandardCharsets.UTF_8))
    val o = new ByteArrayOutputStream
    val e = new ByteArrayOutputStream

    michid.script.shell.Main(i, o, e).run()

    println("<out>" + o.toString + "</out>")
    println("<err>" + e.toString + "</err>")
  }

  test("Main.runCode") { _ =>  // michid can't get redirecting io to work properly
    val i = new ByteArrayInputStream(new Array[Byte](0))
    val o = new ByteArrayOutputStream
    val e = new ByteArrayOutputStream

    michid.script.shell.Main(i, o, e).runCode("println(42)", replApi = true)

    println("<out>" + o.toString + "</out>")
    println("<err>" + e.toString + "</err>")
  }

  test("Main.main") { _ =>  // michid can't get redirecting io to work properly
    val i = new ByteArrayInputStream("println(42)\nexit\n".getBytes(StandardCharsets.UTF_8))
    val o = new ByteArrayOutputStream
    val e = new ByteArrayOutputStream

    Console.withIn(i) {
      Console.withOut(o) {
        Console.withErr(e) {
          ammonite.Main.main(new Array[String](0))
        }
      }
    }

    println("<out>" + o.toString + "</out>")
    println("<err>" + e.toString + "</err>")
  }

  test("Main.main(-c)") { _ =>  // michid can't get redirecting io to work properly
    val i = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))
    val o = new ByteArrayOutputStream
    val e = new ByteArrayOutputStream

    Console.withIn(i) {
      Console.withOut(o) {
        Console.withErr(e) {
          ammonite.Main.main(Array("""-c "println(42)" """))
        }
      }
    }

    println("<out>" + o.toString + "</out>")
    println("<err>" + e.toString + "</err>")
  }

}