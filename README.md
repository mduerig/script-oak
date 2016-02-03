Scripts for working with [Jackrabbit Oak](http://jackrabbit.apache.org/oak/) in the 
[Ammonite REPL](https://lihaoyi.github.io/Ammonite/).

After starting Ammonite set the Oak version:

    val oakVersion = "1.3.14"
    
and load some Oak specific predefs:

    load.exec("Oak.scala")

To create a new repository and open a session try this:

     val jcr = new org.apache.jackrabbit.oak.jcr.Jcr
     val repo = jcr.createRepository
     val creds = new javax.jcr.SimpleCredentials("admin", "admin".toCharArray)
     val session = repo.login(creds)
     val root = session.getRootNode
     root.addNode("foo")
     ....

