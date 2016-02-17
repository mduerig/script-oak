Support and scripts for working with [Jackrabbit Oak](http://jackrabbit.apache.org/oak/) in the
[Ammonite REPL](https://lihaoyi.github.io/Ammonite/).

After starting Ammonite load this library:

    load.ivy("michid" % "script-oak" % "1.1-SNAPSHOT")

Now import some pre-defs:

    import michid.script.oak._
    import michid.script.oak.Oak._

To create a new repository and open a session try this:

    val jcr = new org.apache.jackrabbit.oak.jcr.Jcr
    val repo = jcr.createRepository
    val creds = new javax.jcr.SimpleCredentials("admin", "admin".toCharArray)
    val session = repo.login(creds)
    val root = session.getRootNode
    root.addNode("foo")
    ....


To import a built in script:

    load(script("TarAnalysis.scala"))