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
package michid.script.oak.nodestore

import org.apache.jackrabbit.oak.api.PropertyState
import org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState.EMPTY_NODE
import org.apache.jackrabbit.oak.spi.state.{NodeState, NodeStateDiff}

import scala.collection.mutable.ListBuffer

sealed abstract class Change(val path: String)
case class NodeAdded(override val path: String, after: NodeState) extends Change(path)
case class NodeRemoved(override val path: String, before: NodeState) extends Change(path)
case class NodeChanged(override val path: String, before: NodeState, after: NodeState) extends Change(path)
case class PropertyAdded(override val path: String, after: PropertyState) extends Change(path)
case class PropertyRemoved(override val path: String, before: PropertyState) extends Change(path)
case class PropertyChanged(override val path: String, before: PropertyState, after: PropertyState) extends Change(path)

object Change {
  def unapply(change: Change): Option[String] = Some(change.path)
}

/**
  * Catamorphism over content diff
  */
object Changes {
  def apply(before: NodeState, after: NodeState, path: String = ""): Stream[Change] = {
    var changes = new ListBuffer[Stream[Change]]

    after.compareAgainstBaseState(before, new NodeStateDiff {
      override def propertyAdded(after: PropertyState): Boolean = {
        changes += Stream(PropertyAdded(path + "/" + after.getName, after))
        true
      }
      override def propertyDeleted(before: PropertyState): Boolean = {
        changes += Stream(PropertyRemoved(path + "/" + before.getName, before))
        true
      }
      override def propertyChanged(before: PropertyState, after: PropertyState): Boolean = {
        changes += Stream(PropertyChanged(path + "/" + after.getName, before, after))
        true
      }
      override def childNodeAdded(name: String, after: NodeState): Boolean = {
        val childPath = path + "/" + name
        changes += NodeAdded(childPath, after) #:: Changes(EMPTY_NODE, after, childPath)
        true
      }
      override def childNodeDeleted(name: String, before: NodeState): Boolean = {
        val childPath = path + "/" + name
        changes += NodeRemoved(childPath, after) #:: Changes(before, EMPTY_NODE, childPath)
        true
      }
      override def childNodeChanged(name: String, before: NodeState, after: NodeState): Boolean = {
        val childPath = path + "/" + name
        changes += NodeChanged(childPath, before, after) #:: Changes(before, after, childPath)
        true
      }
    })

    changes.foldRight(Stream.empty[Change])(_ #::: _)
  }

  def apply(nodes: Iterable[NodeState], path: String): Stream[Stream[Change]] = {
    if (nodes.take(2).size < 2) Stream.empty
    else nodes.sliding(2).map(states => {
      val statesList = states.toList
      apply(statesList.head, statesList(1), path)
    }).toStream
  }

  case class TurnOver(
         addedProperties: Long,
         removedProperties: Long,
         changedProperties: Long,
         addedNodes: Long,
         removedNodes: Long,
         changedNodes: Long,
         addedContent: Long,
         removedContent: Long) {
    def this() { this(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)}
  }

  def turnOver(changes: Stream[Change], size: PropertyState => Long = PropertyStates.size)
  : TurnOver = {
    changes.foldLeft(new TurnOver())({
      case (turnOver, PropertyAdded(_, after)) =>
        turnOver.copy(
          addedProperties = turnOver.addedProperties + 1,
          addedContent = turnOver.addedContent + size(after))
      case (turnOver, PropertyRemoved(_, before)) =>
        turnOver.copy(
          removedProperties = turnOver.removedProperties + 1,
          removedContent = turnOver.removedContent + size(before))
      case (turnOver, PropertyChanged(_, before, after)) =>
        turnOver.copy(
          changedProperties = turnOver.changedProperties + 1,
          addedContent = turnOver.addedContent + size(after),
          removedContent = turnOver.removedContent + size(before))
      case (turnOver, NodeAdded(_,_)) =>
        turnOver.copy(
          addedNodes = turnOver.addedNodes + 1)
      case (turnOver, NodeRemoved(_,_)) =>
        turnOver.copy(
          removedNodes = turnOver.removedNodes + 1)
      case (turnOver, NodeChanged(_,_,_)) =>
        turnOver.copy(
          changedNodes = turnOver.changedNodes + 1)
    })
  }

}
