package michid.script.oak

import java.lang.System.getProperties

import ammonite.interp.InterpBridge
import coursier.{Dependency, Module}
import coursier.maven.MavenRepository

package object fixtures {
  val scriptOakVersion: String = Option(getClass.getPackage.getImplementationVersion)
        .getOrElse("1.3-SNAPSHOT")  // michid latest.integration equivalent?

  object oak_1_7_7 {
    val oakVersion = "oak-1.7.7"

    // michid .m2 resolve should be there by default
    val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency("michid"%"script-""" + oakVersion + """", """" + scriptOakVersion + """"))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
    |""").stripMargin

    def load(): Unit = {
      val interpreter = InterpBridge.value0
      if (interpreter != null) {
        interpreter.repositories() ++= Seq(MavenRepository("file://" + getProperties.get("user.home") + "/.m2/repository/"))
        interpreter.load.ivy(Dependency(Module("michid", s"script-$oakVersion"), scriptOakVersion))
      } else {
        throw new Error("No interpreter")
      }
    }
  }

  object oak_1_7_8 {
    val oakVersion = "oak-1.7.8"

    // michid .m2 resolve should be there by default
    val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency("michid"%"script-""" + oakVersion + """", """" + scriptOakVersion + """"))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
    |""").stripMargin

    def load(): Unit = {
      val interpreter = InterpBridge.value0
      if (interpreter != null) {
        interpreter.repositories() ++= Seq(MavenRepository("file://" + getProperties.get("user.home") + "/.m2/repository/"))
        interpreter.load.ivy(Dependency(Module("michid", s"script-$oakVersion"), scriptOakVersion))
      } else {
        throw new Error("No interpreter")
      }
    }
  }

}
