package michid.script.shell

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {
  val scriptOakVersion: String = Option(getClass.getPackage.getImplementationVersion)
          .getOrElse("1.3-SNAPSHOT")  // michid latest.integration equivalent?

  // michid .m2 resolve should be there by default
  private val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency("michid"%"script-oak-1.7.7", """" + scriptOakVersion +""""))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
  |""").stripMargin

  def main(args: Array[String]): Unit = {
    ammonite.Main.main(Array(
      "--predef-code", predef,
      "--banner", "Welcome to Script Oak " + scriptOakVersion) ++
      args)
  }

  def run(script: String): Unit =
    println(ammonite.Main.main0(List(
      "--predef-code", predef,
      "--code", script),
      System.in, System.out, System.err))
}
