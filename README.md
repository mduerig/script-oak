Support and scripts for working with [Jackrabbit Oak](http://jackrabbit.apache.org/oak/) in the
[Ammonite REPL](https://lihaoyi.github.io/Ammonite/).

After starting Ammonite load this library:

    import $ivy.`michid:script-oak:1.3-SNAPSHOT`

Now import some pre-defs:

    import michid.script.oak._

To create a new repository and open a session try this:

    val jcr = new org.apache.jackrabbit.oak.jcr.Jcr
    val repo = jcr.createRepository
    val creds = new javax.jcr.SimpleCredentials("admin", "admin".toCharArray)
    val session = repo.login(creds)
    val root = session.getRootNode
    root.addNode("foo")
    ...


To import a built in script:

    interp.load(script("TarAnalysis.scala"))
    
Now list list the tar files in `segmentstore` using the `tars()` function from the script just 
loaded:
    
    tars(cwd/"segmentstore")
    
    
To analyse a file store:
    
    // Assuming we have a data store. Otherwise pass None
    // as 2nd argument to fileStoreAnalyser() 
    val ds = newBlobStore(cwd/"datastore")
    val fs = fileStoreAnalyser(cwd/"segmentstore", Some(ds))
    
    val superRoot = fs.getNode()
    val rootNode = fs.getNode("root")
    ...

Analysing the items in a file store:

    import michid.script.oak.nodestore.Items._
    
    // All nodes flat
    val nodes = collectNodes(root(fs.getNode("root")))
    
    // Find all nodes with 2 child nodes
    nodes.filter(_.nodes.size == 2)
    
    // Group properties by number of values
    val properties = collectProperties(root(fs.getNode("root")))
    val byPropertyCount = properties.groupBy(_.values.size)
    
    // Number of nodes with a given number of properties in decreasing order
    val noOfProps = byPropertyCount.mapValues(_.size).toList.sortBy(-_._2)
    
    // Number values per value type 
    val values = collectValues(root(fs.getNode("root")))
    values.groupBy(_.tyqe).mapValues(_.size)