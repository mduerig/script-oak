package michid.script.oak.fixture

import java.io.{ByteArrayInputStream, IOException, InputStream}

import org.apache.jackrabbit.oak.spi.blob.{BlobOptions, BlobStore}

/**
  * Utility BlobStore implementation to be used in tooling that can work with a
  * FileStore without the need of the DataStore being present locally
  */
class BasicReadOnlyBlobStore extends BlobStore {
  @throws[IOException]
  override def writeBlob(in: InputStream) = throw new UnsupportedOperationException

  /**
    * Ignores the options provided and delegates to `writeBlob(InputStream)`
    *
    * @param in      the input stream to write
    * @param options the options to use
    * @return
    * @throws IOException
    */
  @throws[IOException]
  override def writeBlob(in: InputStream, options: BlobOptions): String = writeBlob(in)

  @throws[IOException]
  override def readBlob(blobId: String, pos: Long, buff: Array[Byte], off: Int, length: Int) = throw new UnsupportedOperationException

  @throws[IOException]
  override def getBlobLength(blobId: String): Long = { // best effort length extraction
    val indexOfSep = blobId.lastIndexOf("#")
    if (indexOfSep != -1) java.lang.Long.valueOf(blobId.substring(indexOfSep + 1))
    else -1
  }

  @throws[IOException]
  override def getInputStream(blobId: String) = new ByteArrayInputStream(new Array[Byte](0))

  override def getBlobId(reference: String): String = reference

  override def getReference(blobId: String): String = blobId
}