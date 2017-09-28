package michid.script.shell

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {
  val version: String = Option(getClass.getPackage.getImplementationVersion)
          .getOrElse("1.3-SNAPSHOT")  // michid latest.integration equivalent?

  val oakVersion: String = "1.7.7"

  // michid .m2 resolve should be there by default
  private val predef: String = ("""
      |interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
      |interp.load.ivy(coursier.Dependency(coursier.Module("michid", "script-oak-fixtures"), """" + version +""""))
      |@
      |import michid.script.oak._
      |import michid.script.oak.fixture._
  |""").stripMargin

  def main(args: Array[String]): Unit = {
    val main = ammonite.Main(
      predefCode = predef,
      welcomeBanner = Some(s"Welcome to Script Oak $version")
    )
    if (args.length == 0) main.run()
    else main.runCode(args(0))

// michid allow passing parameters, fix running ITs
//    ammonite.Main.main(args)
//    ammonite.Main.main0("--predef-code" :: predef :: args.toList, System.in, System.out, System.err)
  }
}
