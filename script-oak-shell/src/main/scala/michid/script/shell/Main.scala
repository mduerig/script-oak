package michid.script.shell

import michid.script.oak.fixtures.{OakFixture, oak_1_7_8, scriptOakVersion}

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {

  def main(args: Array[String]): Unit = {
    // michid inject via command line args, add good default
    val fixture = oak_1_7_8
    ammonite.Main.main(Array(
      "--predef-code", fixture.predef,
      "--banner", s"Welcome to Script Oak $scriptOakVersion / ${fixture.oakVersion}") ++
      args)
  }

  def run(script: String, fixture: OakFixture): Unit =
    println(ammonite.Main.main0(List(
      "--predef-code", fixture.predef,
      "--code", script),
      System.in, System.out, System.err))
}
