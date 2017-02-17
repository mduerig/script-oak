package michid.script.shell

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {
  def main(args: Array[String]): Unit = {
    ammonite.Main(
      welcomeBanner = Some("Welcome to Script Oak 1.3-SNAPSHOT"),  // michid dont hc
      predef = "import $ivy.`michid:script-oak:1.3+`, michid.script.oak._"  // michid dont hc -> also fix this in the scripts
    ).run()
  }
}