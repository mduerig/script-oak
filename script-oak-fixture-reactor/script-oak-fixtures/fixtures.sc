def tryLoadFixtures(version: String): Boolean = {
  try {
    interp.load.ivy(coursier.Dependency("michid" %% "script-oak-fixtures", version))
    true
  }
  catch {
    case _ => false
  }
}

def loadFixtures(versions: List[String]): Unit = {
  val userHome = java.lang.System.getProperties.get("user.home")
  interp.repositories() ++= Seq(coursier.MavenRepository("file://" + userHome + "/.m2/repository/"))

  (versions.head :: versions.head + "-SNAPSHOT" :: versions.tail).foreach{ version =>
    if (tryLoadFixtures(version)) {
      println("Loaded script-oak " + version)
      return
    } else {
      println("Not found script-oak " + version)
    }
  }
}

val versions = List("1.6", "1.5", "1.4")
loadFixtures(versions)

def loadOak(): Unit = {
  repl.load.apply("michid.script.oak.fixtures.latest.load()")
}

