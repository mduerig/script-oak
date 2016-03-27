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
package michid.script.oak


import ammonite.ops._
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore.ReadOnlyStore

/** Common predefs used by script-oak */

object Oak {
  /** open read only store at path */
  def readonlyStore(path: Path) = new ReadOnlyStore(path.toNIO.toFile)

  /** read a script from /scripts */
  def script(name: String) = read! resource/'scripts/name
}

