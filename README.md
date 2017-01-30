Support and scripts for working with [Jackrabbit Oak](http://jackrabbit.apache.org/oak/) in the
[Ammonite REPL](https://lihaoyi.github.io/Ammonite/).

After starting Ammonite load this library and import some pre-defs:

    import $ivy.`michid:script-oak:1.3-SNAPSHOT`, michid.script.oak._

Now you can explore the various built in [scripts](src/main/resources/scripts) to learn about 
script-oak's capabilities.
 
Creating and accessing a JCR repository ([RepositoryDemo.sc](src/main/resources/scripts/RepositoryDemo.sc)):
 
    // Load the script
    val repoDemo = script("RepositoryDemo.sc")
    
    // Browse the script
    browse(repoDemo)
    
    // Execute the script
    repoDemo.run

Analysing a file store ([FileStoreDemo.sc](src/main/resources/scripts/FileStoreDemo.sc)):

    script("FileStoreDemo.sc").run
