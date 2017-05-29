package michid.script.oak

/**
  * Utilities for really lazily concatenating iterators in constant memory
  */
object LazyIterators {

  /**
    * @return an iterator with `head` prepended to `tail`
    */
  def cons[T](head: T, tail: Iterator[T]): Iterator[T] = new Iterator[T] {
    private var first: Boolean = true

    override def hasNext: Boolean = first || tail.hasNext

    override def next(): T =
      if (first) {first = false; head}
      else tail.next()
  }

  /**
    * @return an iterator of the concatenation of `iterators`
    */
  def flatten[T](iterators: Iterator[Iterator[T]]): Iterator[T] = new Iterator[T] {
    private var current: Iterator[T] =
      if (iterators.hasNext) iterators.next()
      else Seq().iterator

    override def hasNext: Boolean = {
      while(!current.hasNext && iterators.hasNext) {
        current = iterators.next()
      }
      current.hasNext
    }

    override def next(): T = current.next()
  }

}
