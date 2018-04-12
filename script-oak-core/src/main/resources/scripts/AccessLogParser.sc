import $ivy.`org.aicer.grok:grok:0.9.0`
import $ivy.`log4j:log4j:1.2.11`

import org.aicer.grok.dictionary.GrokDictionary
import scala.collection.JavaConverters._

val d = new GrokDictionary()
d.addBuiltInDictionaries
d.bind
val compiledPattern = d.compileExpression("""%{IPORHOST:clientip} %{USER:ident} %{USER:auth} %{HTTPDATE:timestamp} "(?:%{WORD:verb} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion})?|%{DATA:rawrequest})" %{NUMBER:response} (?:%{NUMBER:bytes}|-) %{QS:referrer} %{QS:agent}""")

var log = cwd/"access.log"
read(log).lines.map(compiledPattern.extractNamedGroups(_).asScala).toStream
