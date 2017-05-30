package michid.script.oak

import java.util.NoSuchElementException

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
    private var currentIterator: Iterator[T] = emptyIterator
    private var hasMore: Option[Boolean] = None

    override def hasNext: Boolean = {
      // Memoizing the result of hasNext is crucial to performance when recursively
      // traversing tree structures.
      if (hasMore.isEmpty) {
        while (!currentIterator.hasNext && iterators.hasNext) {
          currentIterator = iterators.next
        }
        hasMore = Some(currentIterator.hasNext)
      }
      hasMore.get
    }

    override def next: T =
      if (hasNext) {
        hasMore = None
        currentIterator.next
      }
      else throw new NoSuchElementException
  }

}
