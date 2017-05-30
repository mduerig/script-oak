package michid.script.oak

/**
  * Utilities for really lazily concatenating iterators in constant memory
  */
object LazyIterators {
  val emptyIterator: Iterator[Nothing] = Nil.iterator

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
    private var current: Iterator[T] = emptyIterator

    private def currentIterator(): Iterator[T] = {
      while(!current.hasNext && iterators.hasNext) {
        current = iterators.next()
      }
      current
    }

    override def hasNext: Boolean =
      currentIterator().hasNext

    override def next(): T =
      currentIterator().next()

  }

}
