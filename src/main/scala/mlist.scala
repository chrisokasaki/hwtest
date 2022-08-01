package hwtest

/** Provides `MList`: singly-linked *mutable* lists of integers. */
package mlist

  /** A class of singly-linked *mutable* lists of integers.
    *
    * `MList` is very similar to [[hwtest.slist.SList]], but
    * with the following differences:
    *   1. `head_=` and `tail_=` method supporting assignment
    *      to the head and tail of a node.
    *   2. `==` and hashing based on pointers rather than
    *      the contents of a node.
    *   3. numerous complications related to the possibility
    *      that an `MList` could contain a cycle.
    *
    * Here's how the `MList`s handle cycles:
    *   1. A cyclic list will be displayed as (for example)
    *      ```
    *        MList(1, 2, *3, 4->*)
    *      ```
    *     which indicates that the tail field of the 4 node points back to the 3 node.
    *   2. Two cyclic lists are considered equivalent only
    *      if they have EXACTLY the same shape.  For example,
    *      the following lists are arguably equivalent (because
    *      they all consist of a 1 followed by an infinite number
    *      of 2s) but are NOT considered equivalent by this package:
    *      ```
    *        MList(1, *2->*)
    *        MList(1, 2, *2->*)
    *        MList(1, *2, 2->*)
    *      ```
    *   3. To write tests involving cyclic lists, use the format
    *      ```
    *        (1 *2 3)
    *      ```
    *      where the `*` indicates the beginning of the cycle.
    *      The `*` must be followed by at least one element.
    *      Whitespace is allowed between the `*` and the following
    *      element.
    */
  sealed abstract class MList:
    /** Tests whether the list is empty. */
    def isEmpty: Boolean = this eq MNil

    /** Tests whether the list has at least one element. */
    def nonEmpty: Boolean = this ne MNil

    /** Returns the first element of the list.
      *
      * Throws `NoSuchElementException` if the list is empty.
      */
    def head: Int

    /** Returns the rest of the list without the first element.
      *
      * Throws `UnsupportedOperationException` if the list is empty.
      */
    def tail: MList

    /** Adds an element to the front of the list. */
    def ::(elem: Int): MList = MCons(elem, this)

    /** Assigns a new value to the head of the list.
      *
      * Throws `UnsupportedOperationException` if the list is empty.
      */
    def head_=(newHead: Int): Unit

    /** Assigns a new value to the tail of the list.
      *
      * Throws `UnsupportedOperationException` if the list is empty.
      */
    def tail_=(newTail: MList): Unit

    override def toString(): String =
      if !isCyclic then
        val buf = scala.collection.mutable.ArrayBuffer.empty[Int]
        var list = this
        while list.nonEmpty do
          buf += list.head
          list = list.tail
        buf.mkString("MList(", ", ", ")")
      else
        // format cycle as "MList(1, *2, 3->*)"
        val sb = new scala.collection.mutable.StringBuilder("MList(")
        var p = this
        for i <- 1 to prefixLength do
          sb ++= p.head.toString
          p = p.tail
          sb ++= ", "
        val cycleStart = p
        sb += '*'
        while true do
          sb ++= p.head.toString
          p = p.tail
          if p != cycleStart then sb ++= ", "
          else
            sb ++= "->*)"
            return sb.result()
        ??? // can't ever get here

    private def isCyclic: Boolean =
      if isEmpty then return false

      // Floyd's tortoise-and-hare algorithm
      // http://en.wikipedia.org/wiki/Cycle_detection#Tortoise_and_hare
      var slow = this
      var fast = this.tail
      while fast != slow do
        if fast.isEmpty || fast.tail.isEmpty then return false
        slow = slow.tail
        fast = fast.tail.tail
      true

    private def prefixLength: Int =
      // list must be cyclic! will crash if not cyclic!

      // Floyd's tortoise-and-hare algorithm
      // http://en.wikipedia.org/wiki/Cycle_detection#Tortoise_and_hare
      var slow = this
      var fast = this.tail
      while fast != slow do
        slow = slow.tail
        fast = fast.tail.tail

      // Phase 2 of Floyd's algorithm: find the head of the cycle
      var prefixLen = 0
      slow = this
      fast = fast.tail
      while fast != slow do
        slow = slow.tail
        fast = fast.tail
        prefixLen += 1
      prefixLen

    private[mlist] def copy(): MList =
      val buf = scala.collection.mutable.ArrayBuffer.empty[Int]
      if !isCyclic then
        var list = this
        while list.nonEmpty do
          buf += list.head
          list = list.tail
        buf.foldRight(MList.empty)(_ :: _)
      else
        val prefixLen = prefixLength
        var p = this
        var startOfCycle: MList = MNil
        while true do
          if buf.size == prefixLen then startOfCycle = p
          buf += p.head
          if p.tail == startOfCycle then
            val lastNode: MList = MCons(buf.last, MNil)
            val cycle = buf.slice(prefixLen, buf.length-1).foldRight(lastNode)(_ :: _)
            lastNode.tail = cycle // make the loop
            return buf.take(prefixLen).foldRight(cycle)(_ :: _)
          else p = p.tail
        ??? // can't ever get here

  private object MNil extends MList:
    def head: Int = throw new NoSuchElementException("can't call head on an empty MList")
    def tail: MList = throw new UnsupportedOperationException("can't call tail on an empty MList")
    def head_=(newHead: Int): Unit =
      throw new UnsupportedOperationException("can't assign to head of an empty MList")
    def tail_=(newTail: MList): Unit =
      throw new UnsupportedOperationException("can't assign to tail of an empty MList")

  private class MCons(initialHead: Int, initialTail: MList) extends MList:
    private var _head = initialHead
    private var _tail = initialTail
    def head: Int = _head
    def tail: MList = _tail
    def head_=(newHead: Int): Unit = { _head = newHead }
    def tail_=(newTail: MList): Unit = { _tail = newTail }

  object MList:
    /** Returns an empty `MList`. */
    def empty: MList = MNil

    import hwtest.parsers.*
    import hwtest.{Testable,Src}

    given TestableMList: Testable[MList] with
      val name = "MList"
      val TI = Testable.TestableInt
      override def equiv(x: MList, y: MList): Boolean = x.toString == y.toString
      override def lt(x: MList, y: MList): Boolean = x.toString < y.toString
      override def copy(x: MList): MList =
        if x.isEmpty then MList.empty
        else
          val front = x.head :: MList.empty
          var rear = front
          var list = x.tail
          var copied = Map(x -> front) // used to detect a cycle
          while list.nonEmpty && !copied.contains(list) do
            rear.tail = list.head :: MList.empty
            copied += (list -> rear)
            rear = rear.tail
            list = list.tail
          if copied.contains(list) then rear.tail = copied(list)
          front

      def parse(src: Src): MList =
        // format
        //   without cycle: (1 2 3)
        //      with cycle: (1 *2 3)
        //   may have whitespace after *
        //   must have an item after *, not )
        //   must NOT have more than one *
        src.skipWhite()
        if src.head != '(' then
          parseError(s"Expected '(' found '${src.head}'.")
        src.next() // discard the '('

        var front: MList = MNil
        var back: MList = MNil
        var frontOfCycle: MList = MNil

        while true do
          src.skipWhite()
          if src.head == ')' then
            src.next()
            if back != MNil then back.tail = frontOfCycle
            return front
          else
            val cycleFlag = src.head == '*'
            if cycleFlag then
              src.next() // consume the *
              if frontOfCycle.nonEmpty then
                throw Exception("malformed test data -- more than one * in MList")
            val node = MCons(pInt(src), MNil)
            if front == MNil then front = node
            if back != MNil then back.tail = node
            back = node
            if cycleFlag then frontOfCycle = node
        ??? // can't ever get here

  /** A "header" node for `MList`s.
    *
    * The `info` field stores information about the list, such as its length.
    *
    * The `front` field stores a pointer to the front of the list. Maintaining
    * this pointer in a header node helps avoid a very common bug where the
    * front of the list changes (either a new list node was added or the front
    * node was removed) but a pointer elsewhere in the code still points
    * to the old front. With header nodes, only pointers to the header should
    * be shared, and every operation that changes the front of the list
    * should update the header.
    */
  final class MHeader[A](var info: A, var front: MList = MList.empty):
    override def toString() =
      val listString = front.toString().drop(6) // drop "MList("
      s"MHeader($info ; $listString"

  object MHeader:
    import hwtest.parsers.*
    import hwtest.{Testable,Src}

    given TestableMHeader[A](using TA: Testable[A]): Testable[MHeader[A]] with
      val name = s"MHeader[${TA.name}]"
      val TML = MList.TestableMList
      def parse(src: Src): MHeader[A] = MHeader(TA.parse(src), TML.parse(src))
      val TS = Testable.TestableString
      override def _show(x: MHeader[A]): String =
        val listString = x.front.toString().drop(6) // drop "MList("
        s"MHeader(${TA.show(x.info)} ; $listString"
      override def equiv(x: MHeader[A], y: MHeader[A]): Boolean =
        TA.equiv(x.info, y.info) && TML.equiv(x.front, y.front)
      override def lt(x: MHeader[A], y: MHeader[A]): Boolean =
        if TA.lt(x.info, y.info) then true
        else if TA.lt(y.info, x.info) then false
        else TML.lt(x.front, y.front)
      override def copy(x: MHeader[A]): MHeader[A] =
        MHeader(TA.copy(x.info), TML.copy(x.front))
      override def checkInvariant(x: MHeader[A]): Unit =
        TA.checkInvariant(x.info)
