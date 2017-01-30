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
import java.io.File

import ammonite.ops._
import michid.script.oak.filestore.Tar

import scala.io.Source

/** Statistics of tar file content */

/* List tar files */
def tars(path: Path) = (ls ! path) |? (_.ext == "tar")

/** List bak files */
def baks(path: Path) = (ls ! path) |? (_.ext == "bak")

/** List content of tar file */
def tarls(path: Path) = Tar(path)

/** Distribution of sizes wrt. to count bins */
def sizeDist(sizes: Iterable[Int], count: Int): Seq[(Int, Int)] = {
  val min = sizes.min
  val max = sizes.max + 1

  val emptyBins = Vector.fill(count)(0)
  val bins = sizes.foldLeft(emptyBins)((b, v) => {
    val k = ((v - min).toDouble / (max - min).toDouble * count).toInt
    b updated (k, b(k) + 1)
  })

  val range = min to max by (max - min) / count
  range zip bins
}

/** Segment size distribution from tar files */
def segmentDistFromTars(path: Path, count: Int): Seq[(Int, Int)] = {
  val entries = tars(path) || tarls
  sizeDist(entries | (_.size.toInt), count)
}

/** Sizes from tar file listings (tar -tvf) */
def sizes(path: Path): Iterable[Int] = {
  new Iterable[Int] {
    override def iterator: Iterator[Int] =
      Source.fromFile(new File(path.toString)).getLines()
          .filterNot(_.contains("data"))
          .map(_.substring(16, 30).trim.toInt)
  }
}

/** Segment size distribution from tar listing */
def segmentDistFromTarLs(path: Path, count: Int): Seq[(Int, Int)] = {
  sizeDist(sizes(path), count)
}

/** Tuple to CVS */
val toCVS: ((_, _)) => String = {
  case (a, b) => "\"" + a + "\"\t\"" + b + "\""
}

/** Write tuples to CVS file */
def writeCVS(tuples: Seq[(_, _)], out: Path): Unit = {
  write.over(out, tuples | toCVS)
}

/** Analyse a list of files */
def analyseTarLs(files: Iterable[Path], binCount: Int) = files foreach { file =>
  val out = file/up/(file.name + ".cvs")
  writeCVS(segmentDistFromTarLs(file, binCount), out)
}
