package by.artsiom.bigdata201.yarn.hdfs

import akka.stream.IOResult
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.compress.CompressionCodec

import scala.concurrent.Future

object HdfsSource {

  /**
   * Creates a [[Source]] that consumes as [[ByteString]]
   *
   * @param fs Hadoop file system
   * @param path the file to open
   * @param chunkSize the size of each read operation, defaults to 8192
   */
  def data(fs: FileSystem,
           path: Path,
           chunkSize: Int = 8192): Source[ByteString, Future[IOResult]] =
    StreamConverters.fromInputStream(() => fs.open(path), chunkSize)

  /**
   * Creates a [[Source]] that consumes as [[ByteString]]
   *
   * @param fs Hadoop file system
   * @param path the file to open
   * @param codec a streaming compression/decompression pair
   * @param chunkSize the size of each read operation, defaults to 8192
   */
  def compressed(fs: FileSystem,
                 path: Path,
                 codec: CompressionCodec,
                 chunkSize: Int = 8192): Source[ByteString, Future[IOResult]] =
    StreamConverters.fromInputStream(() => codec.createInputStream(fs.open(path)), chunkSize)
}
