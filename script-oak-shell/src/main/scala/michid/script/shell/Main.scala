package michid.script.shell

import michid.script.oak.fixtures.{oak_1_7_8, scriptOakVersion}

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {
  // michid inject via command line args, add good default
  private val fixture = oak_1_7_8

  def main(args: Array[String]): Unit = {
    ammonite.Main.main(Array(
      "--predef-code", fixture.predef,
      "--banner", s"Welcome to Script Oak $scriptOakVersion / ${fixture.oakVersion}") ++
      args)
  }

  def run(script: String): Unit =
    println(ammonite.Main.main0(List(
      "--predef-code", fixture.predef,
      "--code", script),
      System.in, System.out, System.err))
}
