package michid.script.shell

import michid.script.oak.fixtures.{OakFixture, oakFixtures, latest, scriptOakVersion}

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {

  def main(args: Array[String]): Unit = {
    var fixture = latest
    var ammArgs = args

    if (args.length > 1) {
      if (args(0) == "--oak-version") {
        fixture = oakFixtures.getOrElse(args(1), {
          throw new Error(s"No such version ${args(1)}")
        })
        ammArgs = args.drop(2)
      }
    }

    ammonite.Main.main(Array(
      "--predef-code", fixture.predef,
      "--banner", s"Welcome to Script Oak $scriptOakVersion / ${fixture.oakVersion}") ++
      ammArgs)
  }

  def run(script: String, fixture: OakFixture): Unit =
    println(ammonite.Main.main0(List(
      "--predef-code", fixture.predef,
      "--code", script),
      System.in, System.out, System.err))
}
