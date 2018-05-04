interp.repositories() ++= Seq(coursier.MavenRepository("file://" + java.lang.System.getProperties.get("user.home") + "/.m2/repository/"))
interp.load.ivy(coursier.Dependency("michid"%%"script-oak-1.9.0", "1.3"))
