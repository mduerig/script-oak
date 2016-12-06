package michid.script.oak.nodestore

import org.apache.jackrabbit.oak.api.Type.BINARY
import org.apache.jackrabbit.oak.api.{Blob, PropertyState}
import org.apache.jackrabbit.oak.segment.SegmentBlob

object PropertyStates {
  def size(property: PropertyState): Long =
    (0 until property.count)
            .map(property.size)
            .sum

  def sizeSkipExternal(property: PropertyState): Long = {
    def size(blob: Blob): Long = blob match {
      case blob: SegmentBlob if blob.isExternal => 0
      case _ => blob.length
    }

    (0 until property.count)
            .map(k => size(property.getValue(BINARY, k)))
            .sum
  }
}
