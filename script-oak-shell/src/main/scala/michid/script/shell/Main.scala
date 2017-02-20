package michid.script.shell

import java.io.{InputStream, OutputStream}

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {
  def main(args: Array[String]): Unit = Main().run()

  def apply(inputStream: InputStream = System.in,
            outputStream: OutputStream = System.out,
            errorStream: OutputStream = System.err
           ): ammonite.Main = {
    val version:String =
      Option(getClass.getPackage.getImplementationVersion)
            .getOrElse("latest.integration")
    ammonite.Main(
      predef = s"import $$ivy.`michid:script-oak:$version`, michid.script.oak._",
      welcomeBanner = Some(s"Welcome to Script Oak $version"),
      inputStream = inputStream,
      outputStream = outputStream,
      errorStream = errorStream
    )
  }
}