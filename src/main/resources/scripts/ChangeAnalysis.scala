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
import ammonite.ops.Path
import michid.script.oak.{PropertyChanged, PropertyRemoved, _}
import org.apache.jackrabbit.oak.api.PropertyState
import org.apache.jackrabbit.oak.plugins.segment.SegmentNodeState
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore
import org.apache.jackrabbit.oak.spi.state.NodeState

/**
  * Analyse changes to a file store across a range of revisions
  */
class ChangeAnalysis(store: FileStore, revs: Iterable[String], projection: NodeState => NodeState) {

  def this(store: FileStore, journal: Path, fromRev: String, toRev: String,
           projection: NodeState => NodeState = Projections.root) {
    this(
      store,
      Revisions(journal)
              .dropWhile(_ != fromRev)
              .takeWhile(_ != toRev),
      projection)
  }

  val roots: Iterable[SegmentNodeState] =
    Revisions.nodes(revs, store)

  val changes: Stream[Stream[Change]] =
    Changes(roots map projection, "")

  private def size(property: PropertyState): Long =
    (0 until property.count)
            .map(property.size)
            .sum

  case class Delta(added: Long, removed: Long)

  def propertyDeltas(changes: Stream[Change]): Delta = {
    // revision log is processed in reverse chronological order,
    // so each addition appears as deletion and vice versa
    changes.foldLeft(Delta(0L, 0L))({
      case (delta, PropertyAdded(_, after)) =>
        delta.copy(removed = delta.removed + size(after))
      case (delta, PropertyRemoved(_, before)) =>
        delta.copy(added = delta.added + size(before))
      case (delta, PropertyChanged(_, before, after)) =>
        delta.copy(delta.added + size(before), delta.removed + size(after))
      case (delta, _) => delta
    })
  }
}
