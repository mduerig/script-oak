package michid.script.shell

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {
  val version:String = Option(getClass.getPackage.getImplementationVersion)
          .getOrElse("latest.integration")

  val predef: String = s"import $$ivy.`michid:script-oak:$version`, michid.script.oak._, ammonite.ops._"

  def main(args: Array[String]): Unit = {
    val main = ammonite.Main(
      predef = predef,
      welcomeBanner = Some(s"Welcome to Script Oak $version")
    )

    if (args.length == 0) main.run()
    else main.runCode(args(0), replApi = true)
  }
}
