package michid.script.shell

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {
  def main(args: Array[String]): Unit = {
    val version = getClass.getPackage.getImplementationVersion
    ammonite.Main(
      welcomeBanner = Some(s"Welcome to Script Oak $version"),
      predef = s"import $$ivy.`michid:script-oak:$version`, michid.script.oak._"
    ).run()
  }
}