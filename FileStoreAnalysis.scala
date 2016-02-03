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
load.exec("Oak.scala")
@
import java.util.{UUID, HashSet}
import org.apache.jackrabbit.oak.plugins.segment.SegmentGraph.SegmentGraphVisitor
import org.apache.jackrabbit.oak.plugins.segment.SegmentId
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore.ReadOnlyStore

/** File store statistics */

/** list segment ids, infos and sizes */
def segments(store: ReadOnlyStore): List[SegmentInfo] = {
  var infos: List[SegmentInfo] = Nil

  store.traverseSegmentGraph(new HashSet(), new SegmentGraphVisitor {
    // store.getTracker.getSegmentId()
    override def accept(from: UUID, to: UUID): Unit = {
      val id = store.getTracker.getSegmentId(from.getMostSignificantBits, from.getLeastSignificantBits)
      infos = SegmentInfo(id)::infos
    }
  })

  infos
}

object SegmentInfo {
  def apply(id: SegmentId): SegmentInfo = {
    val segment = id.getSegment
    SegmentInfo(id.toString, segment.getSegmentInfo, segment.size())
  }
}

case class SegmentInfo(id: String, info: String, size: Int)