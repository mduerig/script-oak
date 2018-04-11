import $ivy.`org.apache.jackrabbit:oak-jcr:1.8.0`
import michid.script.oak._

val jcr = new org.apache.jackrabbit.oak.jcr.Jcr
val repo = jcr.createRepository
val creds = new javax.jcr.SimpleCredentials("admin", "admin".toCharArray)
val session = repo.login(creds)
val root = session.getRootNode
root.addNode("foo").setProperty("bar", 42)
println(s"root=${root.getNode("foo")}")
