Support and scripts for working with [Jackrabbit Oak](http://jackrabbit.apache.org/oak/) in the
[Ammonite Shell](https://lihaoyi.github.io/Ammonite/). 

In a running Ammonite shell import the Script Oak library and its main API entry point:

    $ import $url.{`https://raw.githubusercontent.com/mduerig/script-oak/master/script-oak-fixture-reactor/script-oak-fixtures/fixtures.sc` => bootstrap}
    Loaded script-oak 1.5
    
    $ bootstrap.loadOak
    Welcome to Script Oak 1.5 / oak-1.9.11
    
Alternatively start the pre-built shell, which already includes Script Oak and doesn't require
above extra step:

    java -jar script-oak-shell*.jar

Now you can explore the various built in [scripts](script-oak-core/src/main/resources/scripts) to learn about 
script-oak's capabilities.
 
Creating and accessing a JCR repository ([RepositoryDemo.sc](script-oak-core/src/main/resources/scripts/RepositoryDemo.sc)):
 
    // Load the script
    val repoDemo = script("RepositoryDemo.sc")
    
    // Execute the script
    repoDemo.run

Analysing a file store ([FileStoreDemo.sc](script-oak-core/src/main/resources/scripts/FileStoreDemo.sc)):

    script("FileStoreDemo.sc").run
