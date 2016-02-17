/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
load.ivy("org.apache.jackrabbit" % "oak-jcr" % oakVersion)
load.ivy("org.apache.jackrabbit" % "oak-segment" % oakVersion)
load.ivy("org.kamranzafar" % "jtar" % "2.3")
@
import java.io.{FileInputStream, BufferedInputStream}
import java.util.Date
import ammonite.ops._
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore.ReadOnlyStore
import org.kamranzafar.jtar.TarInputStream

/** Common predefs used by script-oak */


/** open read only store at path */
def readonlyStore(path: Path) = new ReadOnlyStore(path.nio.toFile)

case class TarEntry(size: Long, date: Date, name: String)

class Tar(path: Path) extends Iterator[TarEntry] with java.io.Closeable {
  val tar = new TarInputStream(new BufferedInputStream(new FileInputStream(path.toString())))

  var e = tar.getNextEntry
  override def hasNext: Boolean = e != null
  override def next(): TarEntry = {
    val n = e
    e = tar.getNextEntry
    if (n == null) {
      close()
    }
    TarEntry(n.getSize, n.getModTime, n.getName)
  }

  override def close(): Unit = tar.close()
}

/** List content of tar file */
def tarls(path: Path) = new Tar(path)
