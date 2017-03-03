package michid.script.oak.nodestore

import org.apache.jackrabbit.oak.api.Type.{BINARY, BINARIES}
import org.apache.jackrabbit.oak.api.{Blob, PropertyState}
import org.apache.jackrabbit.oak.segment.SegmentBlob
import org.apache.jackrabbit.oak.spi.state.NodeState
import scala.collection.JavaConverters._

object ItemStates {

  /** The size of a node as the size of the sizes of all its properties */
  def nodeSize(skipExternals: Boolean)(node: NodeState): Long = {
    node.getProperties.asScala.map(propertySize(skipExternals)).sum
  }

  /** The size of a property skipping optionally skipping externals:
    * the sum of the sizes of all its values where values from blob stores are
    * optionally counted as zero
    */
  def propertySize(skipExternal: Boolean)(property: PropertyState): Long = {
    def blobSize(blob: Blob): Long = blob match {
      case blob: SegmentBlob if blob.isExternal => 0
      case _ => blob.length
    }

    val isBinary = property.getType == BINARY || property.getType == BINARIES

    def sizeAt(index: Integer) = {
      if (skipExternal && isBinary) blobSize(property.getValue(BINARY, index))
      else property.size(index)
    }

    (0 until property.count)
            .map(sizeAt(_))
            .sum
  }

}
