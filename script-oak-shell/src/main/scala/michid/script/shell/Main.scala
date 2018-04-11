package michid.script.shell

import michid.script.oak.fixtures.{OakFixture, oakFixtures, latest, scriptOakVersion}

/**
  * Main class for an interactive Scala shell including all Oak dependencies
  */
object Main {

  def main(args: Array[String]): Unit = {
    var fixture = latest
    var ammArgs = args

    if (args.length == 1) {
      if (args(0) == "--help") {
        println(s"--oak-version [${oakFixtures.values.mkString("|")}]: " +
                "set Oak version")
        println(s"--amm-predef [${oakFixtures.values.mkString("|")}]: " +
                "print code predef Ammonite standalone usage of Script Oak")
        sys.exit(0)
      }
    } else if (args.length > 1) {
      fixture = oakFixtures.getOrElse(args(1), {
        throw new Error(s"No such version ${args(1)}")
      })
      if (args(0) == "--oak-version") {
        ammArgs = args.drop(2)
      } else if (args(0) == "--amm-predef") {
        println(s"${fixture.predef}")
        sys.exit(0)
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
