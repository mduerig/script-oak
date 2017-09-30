package michid.script.oak

import java.lang.System.{getProperties, getProperty}

import ammonite.interp.InterpBridge
import coursier.{Dependency, Module}
import coursier.maven.MavenRepository

package object fixtures {
  val scriptOakVersion: String = Option(getClass.getPackage.getImplementationVersion)
        .getOrElse({
          val version = getProperty("project.version")
          if (version == null) {
            throw new Error("Cannot determine implementation version. " +
                    "Use -Dproject.version to specify the implementation version.")
          }
          version
        })

  trait OakFixture {
    val oakVersion: String
    val predef: String
    def load(): Unit
  }

  val oakFixtures: Map[String, OakFixture] = Map(
    oak_1_7_7.oakVersion -> oak_1_7_7,
    oak_1_7_8.oakVersion -> oak_1_7_8,
  )

  val latest: OakFixture = oak_1_7_8

  object oak_1_7_7 extends OakFixture {
    val oakVersion = "oak-1.7.7"

    override def toString: String = oakVersion

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

  object oak_1_7_8 extends OakFixture {
    val oakVersion = "oak-1.7.8"

    override def toString: String = oakVersion

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
