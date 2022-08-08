package hwtest

/** Provides `SList`: singly-linked immutable lists of integers intended
  * as a gentle introduction to full-blown Scala Lists.
  */
package slist:

  /** A class of singly-linked immutable lists of integers.
    *
    * `SList` differs from `List` three main ways:
    *   1. It only supports integers as elements, and therefore does not take
    *      a type parameter.
    *   2. It supports only a handful of methods (but those that it does
    *      support exactly mirror the equivalent `List` methods).
    *   3. It is NOT designed to support pattern matching.
    */
  sealed abstract class SList:
    /** Tests whether the list is empty. */
    def isEmpty: Boolean = this eq SNil

    /** Tests whether the list has at least one element. */
    def nonEmpty: Boolean = this ne SNil

    /** Returns the first element of the list.
      *
      * Throws `NoSuchElementException` if the list is empty.
      */
    def head: Int

    /** Returns the rest of the list without the first element.
      *
      * Throws `UnsupportedOperationException` if the list is empty.
      */
    def tail: SList

    /** Adds an element to the front of the list. */
    def ::(elem: Int): SList = SCons(elem, this)

    override def toString(): String =
      val buf = scala.collection.mutable.ArrayBuffer.empty[Int]
      var list = this
      while list.nonEmpty do
        buf += list.head
        list = list.tail
      buf.mkString("SList(", ", ", ")")

  private object SNil extends SList:
    def head: Int = throw new NoSuchElementException("can't call head on an empty SList")
    def tail: SList = throw new UnsupportedOperationException("can't call tail on an empty SList")

  private case class SCons(elem: Int, rest: SList) extends SList:
    def head: Int = elem
    def tail: SList = rest

  object SList:
    /** Returns the empty list. */
    def empty: SList = SNil

    import hwtest.parsers.*
    import hwtest.{Testable,Src}

    given TestableSList: Testable[SList] with
      val name = "SList"
      val TI = Testable.TestableInt
      def parse: Src => SList = pColl(pInt, _.foldRight(SList.empty)(_ :: _))
      override def lt(x: SList, y: SList): Boolean =
        var xp = x
        var yp = y
        while xp.nonEmpty && yp.nonEmpty do
          if xp.head < yp.head then return true
          else if yp.head < xp.head then return false
          else
            xp = xp.tail
            yp = yp.tail
        // one or both lists are empty
        xp.isEmpty && yp.nonEmpty
