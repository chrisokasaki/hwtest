package hwtest

import parsers.*

/** Provides the methods the test framework needs to test functions that
  * take and/or return data of type `A`.
  */
trait Testable[A]:

  /** Returns a string representation of the name of the type `A`.
    *
    * Abstract method that must be defined when instantiating a new `Testable`.
    */
  def name: String

  /** Returns a value of the indicated type read from the `src`.
    *
    * Abstract method that must be defined when instantiating a new `Testable`.
    * Will usually be defined using a function either predefined in
    * `hwtest.parser` or built from combinators provided by `hwtest.parser`.
    */
  def parse(src: Src): A

  /** Like `parse` but also checks that the parsed value satisfies any
    * invariant expected of a value of this type. Throws an exception
    * in an invariant is not met.
    */
  def parseAndCheck(src: Src): A =
    val a = parse(src)
    checkInvariant(a)
    a

  /** Returns a string representing this value for display to the user.
    *
    * Will often be overridden when defining a new `Testable`.
    */
  def show(x: A): String = if x==null then "null" else x.toString

  /** Returns a deep copy of `x`.
    *
    * Intended for storing copies of mutable inputs for later display
    * in error messages. Also used by `testV` where the validation
    * function needs access to the original state of the input.
    *
    * Because so many types in Scala are immutable, the default implementation
    * of `copy` simply returns the original value. **This method must be
    * overwritten for mutable values and also for immutable collections that
    * could contain mutable values.**
    */
  def copy(x: A): A = x

  /** Tests if `x` and `y` are equivalent.
    *
    * Used to check if the result of a function is equivalent to the expected
    * result, to determine whether a test passed.
    *
    * The defalt implementation for `equiv` uses `==`, because that's
    * usually what we want. In situations where we want a broader notion
    * of equivalance, we need to override `equiv` with an appropriate
    * definition.
    *
    * For example, if a function returns a list of integers, but the
    * order of those integers doesn't matter, then we may want to create
    * a new instance of `Testable[List[Int]]` with
    * ```
    *   override def equiv(x: List[Int], y: List[Int]): Boolean = (x.sorted == y.sorted)
    * ```
    *
    * As another example, floating-point numbers typically need a notion of
    * equivalence where numbers can be considered "close enough", even though
    * that wreaks havoc with traditional properties of equivalence such as
    * transitivity.
    */
  def equiv(x: A, y: A) = (x == y)

  /** Tests if `x` is considered less than `y`.
    *
    * Supports the ability to dispay unordered collections (eg, Sets)
    * in a manner that makes it easier to spot the differences. For
    * example, suppose an error message displays these two sets:
    * ```
    *   Set(3, 9, 8, 2, 1, 4, 6, 7)
    *   Set(4, 2, 7, 5, 9, 3, 1, 8)
    * ```
    * Even if the error message tells us they are different, it still
    * takes some effort to spot the difference. But if the same two sets
    * are displayed as
    * ```
    *   Set(1, 2, 3, 4, 6, 7, 8, 9)
    *   Set(1, 2, 3, 4, 5, 7, 8, 9)
    * ```
    * then it's much faster to see that the sets differ in the 6 and 5,
    * which in turn can make debugging easier.
    *
    * Not all types have an obvious comparison function. The default
    * implementation converts the two values to strings and compares the
    * strings. **THE DEFAULT IMPLEMENTATION SHOULD BE OVERRIDDEN WHENEVER
    * THERE IS A MORE MEANINGFUL COMPARISON.**
    */
  def lt(x: A, y: A): Boolean = x.toString < y.toString

  /** Throws an exception if an invariant fails. Otherwise, returns Unit.
    *
    * `checkInvariant` defaults to `{}`.
    * Override this default for types that have meaningful internal invariants
    * that you want to check automatically. Indicate that an invariant has been
    * violated by throwing an exception, preferably with a meaningful error
    * message. Note that `checkInvariant` will be called on each answer **before**
    * comparing to the expected answer. That comparison would presumably
    * also fail, so the advantage of `checkInvariant` is that it can display
    * a different error message for failed invariants than ordinary errors,
    * which matters because failed invariants often provide better clues for
    * debugging than ordinary errors.
    *
    * For example, `checkInvariant` might check if the keys in a binary
    * search tree are ordered in the way they should be, and throw an
    * exception if they aren't (ideally including a useful description
    * in the message of the exception).
    */
  def checkInvariant(x: A): Unit = {}

  def label(name: String, x: A): String = s"$name = ${show(x)}\n"

  // the multiX methods are used when A is actually a collection of T's

  /** Check if two collections are equivalent.
    *
    * The collections are given as iterators. To be equivalent, the two
    * iterators must be the same length, and the value at each position
    * of `iter1` must be equivalent to the value at the same position
    * in `iter2`.
    *
    * When `multiequiv` is called, the collections must be converted
    * to iterators, and might need to be *normalized*, usually by
    * sorting, so that corresponding elements end up corresponding
    * order. (Typcially such normalization would be needed for unordered
    * collections but not for ordered collections.)
    */
  def multiequiv[T](iter1: Iterator[T], iter2: Iterator[T], T: Testable[T]): Boolean =
    while iter1.hasNext && iter2.hasNext do
      if !T.equiv(iter1.next(), iter2.next()) then return false
    !(iter1.hasNext || iter2.hasNext) // equiv if both are empty

  /** Check if one collection is considered "less than" another collection.
    *
    * Typically used when normalizing a collection of collections.
    *
    * The collections are given as iterators, which are compared
    * lexicographically.
    *
    * When `multilt` is called, the collections must be converted
    * to iterators, and may need to be *normalized*, usually by
    * sorting, so that corresponding elements end up in corresponding order.
    * (Typically such normalization would be needed for unordered
    * collections but not for ordered collections.)
    */
  def multilt[T](iter1: Iterator[T], iter2: Iterator[T], T: Testable[T]): Boolean =
    while iter1.hasNext && iter2.hasNext do
      val x = iter1.next()
      val y = iter2.next()
      if T.lt(x,y) then return true
      if T.lt(y,x) then return false // inefficient to do lt twice, ord would be better
    iter2.hasNext


/** Provides many "given" `Testable` instances for built-in types.
  *
  * Provided types include
  *   * `Int`, `Long`, `BigInt`, `Double`
  *   * `Boolean`, `Char`, `String`
  *   * `Option`, `List`, `Array`
  *   * `Set`, `Map` (from `scala.collection.immutable`)
  *   * `Tuple2`, ..., `Tuple4`
  *
  * Other types can be added as needed by declaring an appropriate
  * `Testable` instance.
  *
  * Also provides a `TestableGrid` instance for an array of arrays
  * that displays its data in a grid-like format.
  */
object Testable:

  given TestableInt: Testable[Int] with
    val name = "Int"
    def parse(src: Src): Int = pInt(src)
    override def lt(x: Int, y: Int): Boolean = x < y

  given TestableLong: Testable[Long] with
    val name = "Long"
    def parse(src: Src): Long = pLong(src)
    override def lt(x: Long, y: Long): Boolean = x < y

  given TestableBigInt: Testable[BigInt] with
    val name = "BigInt"
    def parse(src: Src): BigInt = pBigInt(src)
    override def lt(x: BigInt, y: BigInt): Boolean = x < y

  given TestableDouble: Testable[Double] with
    val name = "Double"
    def parse(src: Src) = pDouble(src)
    override def equiv(x: Double, y: Double) =
      (x-y).abs < 1e-8 || (y*(1.0-1e-10) < x) == (x < y*(1.0+1e-10))
    override def lt(x: Double, y: Double): Boolean = x < y // shady!

  given TestableBoolean: Testable[Boolean] with
    val name = "Boolean"
    def parse(src: Src) = pBoolean(src)
    override def lt(x: Boolean, y: Boolean): Boolean = x < y

  given TestableChar: Testable[Char] with
    val name = "Char"
    def parse(src: Src): Char = pChar(src)
    override def show(x: Char): String = s"'$x'" // does NOT try to display escape sequences
    override def lt(x: Char, y: Char): Boolean = x < y

  given TestableString: Testable[String] with
    val name = "String"
    def parse(src: Src): String = pString(src)
    override def show(x: String): String = s""""$x"""" // does NOT try to display escape sequences
    override def lt(x: String, y: String): Boolean = x < y

  given TestableOption[T](using TT: Testable[T]): Testable[Option[T]] with
    val name = s"Option[${TT.name}]"
    def parse(src: Src): Option[T] = pOption(TT.parse)(src)
    override def show(x: Option[T]): String = x.map(TT.show).toString
    override def copy(x: Option[T]): Option[T] = x.map(TT.copy)
    override def equiv(x: Option[T], y: Option[T]): Boolean = multiequiv(x.iterator,y.iterator,TT)
    override def lt(x: Option[T], y: Option[T]): Boolean = multilt(x.iterator,y.iterator,TT)
    override def checkInvariant(x: Option[T]): Unit =
      x.foreach(TT.checkInvariant)

  given TestableList[T](using TT: Testable[T]): Testable[List[T]] with
    val name = s"List[${TT.name}]"
    def parse(src: Src): List[T] = pList(TT.parse)(src)
    override def show(x: List[T]): String = x.map(TT.show).toString
    override def copy(x: List[T]): List[T] = x.map(TT.copy)
    override def equiv(x: List[T], y: List[T]): Boolean = multiequiv(x.iterator,y.iterator,TT)
    override def lt(x: List[T], y: List[T]): Boolean = multilt(x.iterator,y.iterator,TT)
    override def checkInvariant(x: List[T]): Unit =
      x.foreach(TT.checkInvariant)

  given TestableArray[T : reflect.ClassTag](using TT: Testable[T]): Testable[Array[T]] with
    val name = s"Array[${TT.name}]"

    def parse(src: Src): Array[T] =
      val parser = pArray(TT.parse)
      parser(src)
    override def show(x: Array[T]): String = x.map(TT.show).mkString("Array(",", ",")")
    override def copy(x: Array[T]): Array[T] = x.map(TT.copy)
    override def equiv(x: Array[T], y: Array[T]): Boolean = multiequiv(x.iterator,y.iterator,TT)
    override def lt(x: Array[T], y: Array[T]): Boolean = multilt(x.iterator,y.iterator,TT)
    override def checkInvariant(x: Array[T]): Unit =
      x.foreach(TT.checkInvariant)

  given TestableSet[T](using TT: Testable[T]): Testable[Set[T]] with
    val name = s"Set[${TT.name}]"
    def parse(src: Src): Set[T] = pSet(TT.parse)(src)

    def norm(x: Set[T]): Iterator[T] = x.toList.sortWith(TT.lt).iterator // stable sort!
    override def show(x: Set[T]): String = norm(x).mkString("Set(",", ",")")
    override def copy(x: Set[T]): Set[T] = x.map(TT.copy)
    override def equiv(x: Set[T], y: Set[T]): Boolean = multiequiv(norm(x), norm(y), TT)
    override def lt(x: Set[T], y: Set[T]): Boolean = multilt(norm(x), norm(y), TT)
    override def checkInvariant(x: Set[T]): Unit =
      x.foreach(TT.checkInvariant)

  given TestableMap[T1,T2](using TT1: Testable[T1], TT2: Testable[T2]): Testable[Map[T1,T2]] with
    val Pair = TestableTuple2[T1,T2]

    val name = s"Map[${TT1.name},${TT2.name}]"
    def parse(src: Src): Map[T1,T2] = pMap(TT1.parse, TT2.parse)(src)

    def norm(x: Map[T1,T2]) = x.toList.sortWith(Pair.lt).iterator // stable sort!
    override def show(x: Map[T1,T2]) =
      norm(x).map(p => TT1.show(p._1) + " -> " + TT2.show(p._2)).mkString("Map(",", ",")")
    override def copy(x: Map[T1,T2]) = x.toList.map(Pair.copy).toMap
    override def equiv(x: Map[T1,T2], y: Map[T1,T2]): Boolean = multiequiv(norm(x),norm(y),Pair)
    override def lt(x: Map[T1,T2], y: Map[T1,T2]): Boolean = multilt(norm(x),norm(y),Pair)
    override def checkInvariant(x: Map[T1,T2]): Unit =
      for (k,v) <- x do
        TT1.checkInvariant(k)
        TT2.checkInvariant(v)

  given TestableTuple2[T1,T2](using TT1: Testable[T1], TT2: Testable[T2]): Testable[Tuple2[T1,T2]] with
    val name = s"Tuple2[${TT1.name},${TT2.name}]"
    def parse(src: Src): Tuple2[T1,T2] = pTuple(TT1.parse, TT2.parse)(src)
    override def show(x: Tuple2[T1,T2]): String =
      s"(${TT1.show(x._1)}, ${TT2.show(x._2)})"
    override def copy(x: Tuple2[T1,T2]): Tuple2[T1,T2] =
      (TT1.copy(x._1), TT2.copy(x._2))
    override def equiv(x: Tuple2[T1,T2], y: Tuple2[T1,T2]): Boolean =
      TT1.equiv(x._1,y._1) && TT2.equiv(x._2,y._2)
    override def lt(x: Tuple2[T1,T2], y: Tuple2[T1,T2]): Boolean =
      if TT1.lt(x._1,y._1) then true
      else if TT1.lt(y._1,x._1) then false
      else TT2.lt(x._2,y._2)
    override def checkInvariant(x: Tuple2[T1,T2]): Unit =
      TT1.checkInvariant(x._1)
      TT2.checkInvariant(x._2)

  given TestableTuple3[T1,T2,T3](using TT1: Testable[T1], TT2: Testable[T2], TT3: Testable[T3]): Testable[Tuple3[T1,T2,T3]] with
    val name = s"Tuple3[${TT1.name},${TT2.name},${TT3.name}]"
    def parse(src: Src): Tuple3[T1,T2,T3] =
      pTuple(TT1.parse,TT2.parse,TT3.parse)(src)
    override def show(x: Tuple3[T1,T2,T3]): String =
      s"(${TT1.show(x._1)}, ${TT2.show(x._2)}, ${TT3.show(x._3)})"
    override def copy(x: Tuple3[T1,T2,T3]): Tuple3[T1,T2,T3] =
      (TT1.copy(x._1), TT2.copy(x._2), TT3.copy(x._3))
    override def equiv(x: Tuple3[T1,T2,T3], y: Tuple3[T1,T2,T3]): Boolean =
      TT1.equiv(x._1,y._1) && TT2.equiv(x._2,y._2) && TT3.equiv(x._3,y._3)
    override def lt(x: Tuple3[T1,T2,T3], y: Tuple3[T1,T2,T3]): Boolean =
      if TT1.lt(x._1,y._1) then true
      else if TT1.lt(y._1,x._1) then false
      else if TT2.lt(x._2,y._2) then true
      else if TT2.lt(y._2,x._2) then false
      else TT3.lt(x._3,y._3)
    override def checkInvariant(x: Tuple3[T1,T2,T3]): Unit =
      TT1.checkInvariant(x._1)
      TT2.checkInvariant(x._2)
      TT3.checkInvariant(x._3)

  given TestableTuple4[T1,T2,T3,T4](using TT1: Testable[T1], TT2: Testable[T2], TT3: Testable[T3], TT4: Testable[T4]): Testable[Tuple4[T1,T2,T3,T4]] with
    val name = s"Tuple4[${TT1.name},${TT2.name},${TT3.name},${TT4.name}]"
    def parse(src: Src): Tuple4[T1,T2,T3,T4] =
      pTuple(TT1.parse,TT2.parse,TT3.parse,TT4.parse)(src)
    override def show(x: Tuple4[T1,T2,T3,T4]): String =
      s"(${TT1.show(x._1)}, ${TT2.show(x._2)}, ${TT3.show(x._3)}, ${TT4.show(x._4)})"
    override def copy(x: Tuple4[T1,T2,T3,T4]): Tuple4[T1,T2,T3,T4] =
      (TT1.copy(x._1), TT2.copy(x._2), TT3.copy(x._3), TT4.copy(x._4))
    override def equiv(x: Tuple4[T1,T2,T3,T4], y: Tuple4[T1,T2,T3,T4]): Boolean =
      TT1.equiv(x._1,y._1) && TT2.equiv(x._2,y._2) && TT3.equiv(x._3,y._3) && TT4.equiv(x._4,y._4)
    override def lt(x: Tuple4[T1,T2,T3,T4], y: Tuple4[T1,T2,T3,T4]): Boolean =
      if TT1.lt(x._1,y._1) then true
      else if TT1.lt(y._1,x._1) then false
      else if TT2.lt(x._2,y._2) then true
      else if TT2.lt(y._2,x._2) then false
      else if TT3.lt(x._3,y._3) then true
      else if TT3.lt(y._3,x._3) then false
      else TT4.lt(x._4,y._4)
    override def checkInvariant(x: Tuple4[T1,T2,T3,T4]): Unit =
      TT1.checkInvariant(x._1)
      TT2.checkInvariant(x._2)
      TT3.checkInvariant(x._3)
      TT4.checkInvariant(x._4)

  given TestableTuple5[T1,T2,T3,T4,T5](using TT1: Testable[T1], TT2: Testable[T2], TT3: Testable[T3], TT4: Testable[T4], TT5: Testable[T5]): Testable[Tuple5[T1,T2,T3,T4,T5]] with
    val name = s"Tuple5[${TT1.name},${TT2.name},${TT3.name},${TT4.name},${TT5.name}]"
    def parse(src: Src): Tuple5[T1,T2,T3,T4,T5] =
      pTuple(TT1.parse,TT2.parse,TT3.parse,TT4.parse,TT5.parse)(src)
    override def show(x: Tuple5[T1,T2,T3,T4,T5]): String =
      s"(${TT1.show(x._1)}, ${TT2.show(x._2)}, ${TT3.show(x._3)}, ${TT4.show(x._4)},${TT5.show(x._5)})"
    override def copy(x: Tuple5[T1,T2,T3,T4,T5]): Tuple5[T1,T2,T3,T4,T5] =
      (TT1.copy(x._1), TT2.copy(x._2), TT3.copy(x._3), TT4.copy(x._4), TT5.copy(x._5))
    override def equiv(x: Tuple5[T1,T2,T3,T4,T5], y: Tuple5[T1,T2,T3,T4,T5]): Boolean =
      TT1.equiv(x._1,y._1) && TT2.equiv(x._2,y._2) && TT3.equiv(x._3,y._3) && TT4.equiv(x._4,y._4) && TT5.equiv(x._5,y._5)
    override def lt(x: Tuple5[T1,T2,T3,T4,T5], y: Tuple5[T1,T2,T3,T4,T5]): Boolean =
      if TT1.lt(x._1,y._1) then true
      else if TT1.lt(y._1,x._1) then false
      else if TT2.lt(x._2,y._2) then true
      else if TT2.lt(y._2,x._2) then false
      else if TT3.lt(x._3,y._3) then true
      else if TT3.lt(y._3,x._3) then false
      else if TT4.lt(x._4,y._4) then true
      else if TT4.lt(y._4,x._4) then false
      else TT5.lt(x._5,y._5)
    override def checkInvariant(x: Tuple5[T1,T2,T3,T4,T5]): Unit =
      TT1.checkInvariant(x._1)
      TT2.checkInvariant(x._2)
      TT3.checkInvariant(x._3)
      TT4.checkInvariant(x._4)
      TT5.checkInvariant(x._5)

  given TestableTuple6[T1,T2,T3,T4,T5,T6](using TT1: Testable[T1], TT2: Testable[T2], TT3: Testable[T3], TT4: Testable[T4], TT5: Testable[T5], TT6: Testable[T6]): Testable[Tuple6[T1,T2,T3,T4,T5,T6]] with
    val name = s"Tuple6[${TT1.name},${TT2.name},${TT3.name},${TT4.name},${TT5.name},${TT6.name}]"
    def parse(src: Src): Tuple6[T1,T2,T3,T4,T5,T6] =
      pTuple(TT1.parse,TT2.parse,TT3.parse,TT4.parse,TT5.parse,TT6.parse)(src)
    override def show(x: Tuple6[T1,T2,T3,T4,T5,T6]): String =
      s"(${TT1.show(x._1)}, ${TT2.show(x._2)}, ${TT3.show(x._3)}, ${TT4.show(x._4)},${TT5.show(x._5)},${TT6.show(x._6)})"
    override def copy(x: Tuple6[T1,T2,T3,T4,T5,T6]): Tuple6[T1,T2,T3,T4,T5,T6] =
      (TT1.copy(x._1), TT2.copy(x._2), TT3.copy(x._3), TT4.copy(x._4), TT5.copy(x._5), TT6.copy(x._6))
    override def equiv(x: Tuple6[T1,T2,T3,T4,T5,T6], y: Tuple6[T1,T2,T3,T4,T5,T6]): Boolean =
      TT1.equiv(x._1,y._1) && TT2.equiv(x._2,y._2) && TT3.equiv(x._3,y._3) && TT4.equiv(x._4,y._4) && TT5.equiv(x._5,y._5) && TT6.equiv(x._6,y._6)
    override def lt(x: Tuple6[T1,T2,T3,T4,T5,T6], y: Tuple6[T1,T2,T3,T4,T5,T6]): Boolean =
      if TT1.lt(x._1,y._1) then true
      else if TT1.lt(y._1,x._1) then false
      else if TT2.lt(x._2,y._2) then true
      else if TT2.lt(y._2,x._2) then false
      else if TT3.lt(x._3,y._3) then true
      else if TT3.lt(y._3,x._3) then false
      else if TT4.lt(x._4,y._4) then true
      else if TT4.lt(y._4,x._4) then false
      else if TT5.lt(x._5,y._5) then true
      else if TT5.lt(y._5,x._5) then false
      else TT6.lt(x._6,y._6)
    override def checkInvariant(x: Tuple6[T1,T2,T3,T4,T5,T6]): Unit =
      TT1.checkInvariant(x._1)
      TT2.checkInvariant(x._2)
      TT3.checkInvariant(x._3)
      TT4.checkInvariant(x._4)
      TT5.checkInvariant(x._5)
      TT6.checkInvariant(x._6)


  /** Provides an easier-to-read display for two-dimensional arrays by
    * formatting them as a grid.
    *
    * For example,
    * ```
    *   Array(Array(1, 0, 10), Array(-1592, 3, 7), Array(67, 2, 192))
    * ```
    * would be displayed as
    * ```
    *   Array(
    *     Array(    1, 0,  10),
    *     Array(-1592, 3,   7),
    *     Array(   67, 2, 192)
    *   )
    * ```
    * If the inner arrays have different lengths, the data will be displayed
    * in the default format, rather than as a grid.  For example, if the
    * `-1592` were removed, the above array of arrays would display as
    * ```
    *   Array(Array(1, 0, 10), Array(3, 7), Array(67, 2, 192))
    * ```
    *
    * **Warning:** Best used with grids that are small enough not to wrap.
    *
    * To use this as a replacement for `Testable[Array[Array[T]]]`, precede the
    * `test` with a `given`, as in
    * ```
    *   given hwtest.Testable[Array[Array[Int]]] = hwtest.Testable.TestableGrid[Int]
    *   test("column", column, "matrix")
    * ```
    * However, this can also affect the display of *other* tests that involve
    * an `Array[Array[Int]`. If necessary, you can avoid that by surounding the
    * `given...` and `test...` with `{` and `}`.
    */
  def TestableGrid[T: reflect.ClassTag](using T: Testable[T]): Testable[Array[Array[T]]] = new Testable[Array[Array[T]]]:
    // do everything the same as AAT except show
    val AAT = TestableArray[Array[T]]
    val name = AAT.name
    def parse(src: Src): Array[Array[T]] = AAT.parse(src)
    override def copy(x: Array[Array[T]]) = AAT.copy(x)
    override def equiv(x: Array[Array[T]], y: Array[Array[T]]) = AAT.equiv(x,y)
    override def lt(x: Array[Array[T]], y: Array[Array[T]]) = AAT.lt(x,y)

    // note that checkInvariant does NOT check that the grid is rectangular
    override def checkInvariant(x: Array[Array[T]]): Unit = { AAT.checkInvariant(x) }

    // show pads elements to make each column line up nicely
    override def show(g: Array[Array[T]]): String =
      if g == null || g.isEmpty || g.exists(_ == null) then return AAT.show(g)
      val numColumns = g(0).length
      // bail out to default if grid is not rectangular
      if g.exists(_.length != numColumns) then return AAT.show(g)

      val strs = g.map(_.map(T.show))
      for col <- 0 until numColumns do
        val maxLen = strs.map(_.apply(col).length).max
        def pad(s: String) = List.fill(maxLen-s.length)(' ').mkString + s
        for row <- strs do row(col) = pad(row(col))
      strs.map(_.mkString("  Array(",",",")")).mkString("Array(\n",",\n","\n)")
