Support and scripts for working with [Jackrabbit Oak](http://jackrabbit.apache.org/oak/) in the
[Ammonite Shell](https://lihaoyi.github.io/Ammonite/). 

*NOTE*: Ammonite 0.8.5 is currently the most recent version supported. Ammonite's move to the
Coursier library for resolving dependencies in 0.9 does currently not work properly with script-oak. 

In a running Ammonite shell import the Script Oak library and its main API entry point:

    import $ivy.`michid:script-oak:latest.integration`, michid.script.oak._
    
Alternatively start the pre-built shell, which already includes Script Oak and doesn't require
above extra step:

    java -jar script-oak-shell*.jar

Now you can explore the various built in [scripts](script-oak-core/src/main/resources/scripts) to learn about 
script-oak's capabilities.
 
Creating and accessing a JCR repository ([RepositoryDemo.sc](script-oak-core/src/main/resources/scripts/RepositoryDemo.sc)):
 
    // Load the script
    val repoDemo = script("RepositoryDemo.sc")
    
    // Browse the script
    browse(repoDemo)
    
    // Execute the script
    repoDemo.run

Analysing a file store ([FileStoreDemo.sc](script-oak-core/src/main/resources/scripts/FileStoreDemo.sc)):

    script("FileStoreDemo.sc").run
