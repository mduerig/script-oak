package michid.script.oak.nodestore

import org.apache.jackrabbit.oak.api.PropertyState

object PropertyStates {
  def size(property: PropertyState): Long =
    (0 until property.count)
            .map(property.size)
            .sum
}
