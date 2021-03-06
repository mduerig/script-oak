package michid.script.oak

import java.lang.System.getProperty

import ammonite.repl.ReplBridge

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

    def load(): Unit = {
      val repl = ReplBridge.value0
      if (repl != null) {
        repl.load.apply(predef)
      } else {
        throw new Error(s"Error loading fixture $oakVersion. No interpreter")
      }
    }
  }

  val oakFixtures: Map[String, OakFixture] = Map(
    oak_1_9_0.oakVersion -> oak_1_9_0,
    oak_1_9_1.oakVersion -> oak_1_9_1,
    oak_1_9_11.oakVersion -> oak_1_9_11,
    oak_1_10_0.oakVersion -> oak_1_10_0,
  )

  val latest: OakFixture = oak_1_10_0

  object oak_1_9_0 extends OakFixture {
    val oakVersion = "oak-1.9.0"

    override def toString: String = oakVersion

    // michid .m2 resolve should be there by default
    val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency("michid"%%"script-""" + oakVersion + """", """" + scriptOakVersion + """"))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
      |println("Welcome to Script Oak """ + scriptOakVersion + " / " + oakVersion + """")
    |""").stripMargin
  }

  object oak_1_9_1 extends OakFixture {
    val oakVersion = "oak-1.9.1"

    override def toString: String = oakVersion

    // michid .m2 resolve should be there by default
    val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency("michid"%%"script-""" + oakVersion + """", """" + scriptOakVersion + """"))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
      |println("Welcome to Script Oak """ + scriptOakVersion + " / " + oakVersion + """")
    |""").stripMargin
  }

  object oak_1_9_11 extends OakFixture {
    val oakVersion = "oak-1.9.11"

    override def toString: String = oakVersion

    // michid .m2 resolve should be there by default
    val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency("michid"%%"script-""" + oakVersion + """", """" + scriptOakVersion + """"))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
      |println("Welcome to Script Oak """ + scriptOakVersion + " / " + oakVersion + """")
    |""").stripMargin
  }

  object oak_1_10_0 extends OakFixture {
    val oakVersion = "oak-1.10.0"

    override def toString: String = oakVersion

    // michid .m2 resolve should be there by default
    // michid transitive loading of oak-segment-azure doesn't work for whatever reason, so put it here explicitly
    val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency("michid"%%"script-""" + oakVersion + """", """" + scriptOakVersion + """"))
      |interp.load.ivy(coursier.Dependency("org.apache.jackrabbit"%"oak-segment-azure", "1.10.0"))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
      |println("Welcome to Script Oak """ + scriptOakVersion + " / " + oakVersion + """")
    |""").stripMargin
  }

}
