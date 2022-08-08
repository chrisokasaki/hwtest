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

  /** Returns a function that reads a value of the indicated type from a given `src`.
    *
    * Abstract method that must be defined when instantiating a new `Testable`.
    * Will usually be defined using a function either predefined in
    * `hwtest.parser` or built from combinators provided by `hwtest.parser`.
    */
  def parse: Src => A

  /** Like `parse` but also checks that the parsed value satisfies any
    * invariant expected of a value of this type. Throws an exception
    * in an invariant is not met.
    */
  def parseAndCheck(src: Src): A =
    val a = parse(src)
    checkInvariant(a)
    a

  /** The internal logic to be used by the `show` method.
    *
    * Should never be called except by the `show` or `_format` methods from the
    * same `Testable` instance.  All other calls should be to `show` instead.
    *
    * The returned string should normally **not** contain any line breaks,
    * except possibly when a line break occurs in the contents of a quoted
    * `String` or `Char`.
    *
    * The default implementation simply uses the `toString()` method of
    * the corresponding type. However, this method should be overridden if
    *   1. if the `toString()` method omits information or could otherwise
    *      be confusing (for example, displaying strings without quotation
    *      marks could easily lead to confusion), or
    *   2. a type contains, or might contain, one or more subcomponents for
    *      which the `toString()` method on the subcomponent(s) might be
    *      confusing.  For example, this applies to virtually any polymorphic
    *      collection type.
    */
  protected[Testable] def _show(x: A): String = x.toString()

  /** Returns a string representing the given value for display to the user.
    *
    * The returned string should normally **not** contain any line breaks,
    * except possibly when a line break occurs in the contents of a quoted
    * `String` or `Char`.
    *
    * Equivalent to `_show` except includes a check for `null`.
    *
    * To change the behavior of `show`, override `_show` instead.
    */
  final def show(x: A): String = if x == null then "null" else _show(x)

  /** The internal logic to be used by the `format` method.
    *
    * The default implementation simply delegates to `_show`, which is
    * appropriate for most types.
    *
    * Only override this method when there is a multi-line format that
    * makes the data significantly easier to understand.
    */
  protected[Testable] def _format(x: A): String = _show(x)

  /** Returns a possibly-formatted string representing the given top-level
    * value for display to the user.
    *
    * A *top-level* value is one that is a direct argument to or the direct
    * result from a function being tested. All nested values should be
    * displayed using `show`.
    *
    * For example, if a function returns a `BinaryTree`, that result will be
    * displayed in a graphical format, but if a function returns a list of
    * `BinaryTree`s, that list (including the trees) will be displayed in
    * a single-line format.
    *
    * Equivalent to `_format` except includes a check for `null`.
    *
    * To change the behavior of `format`, override `_format` instead.
    */
  final def format(x: A): String = _format(x)

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

  def label(name: String, x: A): String =
    val prefix = if name.endsWith(":") then name else s"$name ="
    val dataString = format(x)
    // If dataString starts with a line break, omit the space
    // between the prefix and dataString. This avoids an annoying
    // interaction between trailing spaces in the output of tests
    // and an editor that removes trailing spaces.
    if dataString.startsWith("\n") || dataString.startsWith("\r\n") then
      s"$prefix$dataString\n"
    else
      s"$prefix $dataString\n"

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
    * sorting, so that corresponding elements end up in corresponding
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
  *   * `Tuple2`, ..., `Tuple6`
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
    def parse: Src => Int = pInt
    override def lt(x: Int, y: Int): Boolean = x < y

  given TestableLong: Testable[Long] with
    val name = "Long"
    def parse: Src => Long = pLong
    override def lt(x: Long, y: Long): Boolean = x < y

  given TestableBigInt: Testable[BigInt] with
    val name = "BigInt"
    def parse: Src => BigInt = pBigInt
    override def lt(x: BigInt, y: BigInt): Boolean = x < y

  given TestableDouble: Testable[Double] with
    val name = "Double"
    def parse: Src => Double = pDouble
    override def equiv(x: Double, y: Double) =
      (x-y).abs < 1e-8 || (y*(1.0-1e-10) < x) == (x < y*(1.0+1e-10))
    override def lt(x: Double, y: Double): Boolean = x < y // shady!

  given TestableBoolean: Testable[Boolean] with
    val name = "Boolean"
    def parse: Src => Boolean = pBoolean
    override def lt(x: Boolean, y: Boolean): Boolean = x < y

  given TestableChar: Testable[Char] with
    val name = "Char"
    def parse: Src => Char = pChar
    override def _show(x: Char): String = s"'$x'" // does NOT try to display escape sequences
    override def lt(x: Char, y: Char): Boolean = x < y

  given TestableString: Testable[String] with
    val name = "String"
    def parse: Src => String = pString
    override def _show(x: String): String =
      s""""$x"""" // does NOT try to display escape sequences
    override def lt(x: String, y: String): Boolean = x < y

  given TestableOption[A](using TA: Testable[A]): Testable[Option[A]] with
    val name = s"Option[${TA.name}]"
    def parse: Src => Option[A] = pOption(TA.parse)
    override def _show(x: Option[A]): String = x.map(TA.show).toString
    override def copy(x: Option[A]): Option[A] = x.map(TA.copy)
    override def equiv(x: Option[A], y: Option[A]): Boolean = multiequiv(x.iterator,y.iterator,TA)
    override def lt(x: Option[A], y: Option[A]): Boolean = multilt(x.iterator,y.iterator,TA)
    override def checkInvariant(x: Option[A]): Unit =
      x.foreach(TA.checkInvariant)

  given TestableList[A](using TA: Testable[A]): Testable[List[A]] with
    val name = s"List[${TA.name}]"
    def parse: Src => List[A] = pList(TA.parse)
    override def _show(x: List[A]): String = x.map(TA.show).toString
    override def copy(x: List[A]): List[A] = x.map(TA.copy)
    override def equiv(x: List[A], y: List[A]): Boolean = multiequiv(x.iterator,y.iterator,TA)
    override def lt(x: List[A], y: List[A]): Boolean = multilt(x.iterator,y.iterator,TA)
    override def checkInvariant(x: List[A]): Unit =
      x.foreach(TA.checkInvariant)

  given TestableArray[A : reflect.ClassTag](using TA: Testable[A]): Testable[Array[A]] with
    val name = s"Array[${TA.name}]"

    def parse: Src => Array[A] = pArray(TA.parse)
    override def _show(x: Array[A]): String = x.map(TA.show).mkString("Array(",", ",")")
    override def copy(x: Array[A]): Array[A] = x.map(TA.copy)
    override def equiv(x: Array[A], y: Array[A]): Boolean = multiequiv(x.iterator,y.iterator,TA)
    override def lt(x: Array[A], y: Array[A]): Boolean = multilt(x.iterator,y.iterator,TA)
    override def checkInvariant(x: Array[A]): Unit =
      x.foreach(TA.checkInvariant)

  given TestableSet[A](using TA: Testable[A]): Testable[Set[A]] with
    val name = s"Set[${TA.name}]"
    def parse: Src => Set[A] = pSet(TA.parse)

    def norm(x: Set[A]): Iterator[A] = x.toList.sortWith(TA.lt).iterator // stable sort!
    override def _show(x: Set[A]): String = norm(x).mkString("Set(",", ",")")
    override def copy(x: Set[A]): Set[A] = x.map(TA.copy)
    override def equiv(x: Set[A], y: Set[A]): Boolean = multiequiv(norm(x), norm(y), TA)
    override def lt(x: Set[A], y: Set[A]): Boolean = multilt(norm(x), norm(y), TA)
    override def checkInvariant(x: Set[A]): Unit =
      x.foreach(TA.checkInvariant)

  given TestableMap[K,V](using TK: Testable[K], TV: Testable[V]): Testable[Map[K,V]] with
    val Pair = TestableTuple2[K,V]

    val name = s"Map[${TK.name}, ${TV.name}]"
    def parse: Src => Map[K,V] = pMap(TK.parse, TV.parse)
    def norm(x: Map[K,V]) = x.toList.sortWith(Pair.lt).iterator // stable sort!
    override def _show(x: Map[K,V]) =
      norm(x).map(p => TK.show(p._1) + " -> " + TV.show(p._2)).mkString("Map(",", ",")")
    override def copy(x: Map[K,V]) = x.toList.map(Pair.copy).toMap
    override def equiv(x: Map[K,V], y: Map[K,V]): Boolean = multiequiv(norm(x),norm(y),Pair)
    override def lt(x: Map[K,V], y: Map[K,V]): Boolean = multilt(norm(x),norm(y),Pair)
    override def checkInvariant(x: Map[K,V]): Unit =
      for (k,v) <- x do
        TK.checkInvariant(k)
        TV.checkInvariant(v)

  given TestableTuple2[A,B](using TA: Testable[A], TB: Testable[B]): Testable[Tuple2[A,B]] with
    val name = s"Tuple2[${TA.name}, ${TB.name}]"
    def parse: Src => Tuple2[A,B] = pTuple(TA.parse, TB.parse)
    override def _show(x: Tuple2[A,B]): String =
      s"(${TA.show(x._1)}, ${TB.show(x._2)})"
    override def copy(x: Tuple2[A,B]): Tuple2[A,B] =
      (TA.copy(x._1), TB.copy(x._2))
    override def equiv(x: Tuple2[A,B], y: Tuple2[A,B]): Boolean =
      TA.equiv(x._1,y._1) && TB.equiv(x._2,y._2)
    override def lt(x: Tuple2[A,B], y: Tuple2[A,B]): Boolean =
      if TA.lt(x._1,y._1) then true
      else if TA.lt(y._1,x._1) then false
      else TB.lt(x._2,y._2)
    override def checkInvariant(x: Tuple2[A,B]): Unit =
      TA.checkInvariant(x._1)
      TB.checkInvariant(x._2)

  given TestableTuple3[A,B,C](using TA: Testable[A], TB: Testable[B], TC: Testable[C]): Testable[Tuple3[A,B,C]] with
    val name = s"Tuple3[${TA.name}, ${TB.name}, ${TC.name}]"
    def parse: Src => Tuple3[A,B,C] =
      pTuple(TA.parse,TB.parse,TC.parse)
    override def _show(x: Tuple3[A,B,C]): String =
      s"(${TA.show(x._1)}, ${TB.show(x._2)}, ${TC.show(x._3)})"
    override def copy(x: Tuple3[A,B,C]): Tuple3[A,B,C] =
      (TA.copy(x._1), TB.copy(x._2), TC.copy(x._3))
    override def equiv(x: Tuple3[A,B,C], y: Tuple3[A,B,C]): Boolean =
      TA.equiv(x._1,y._1) && TB.equiv(x._2,y._2) && TC.equiv(x._3,y._3)
    override def lt(x: Tuple3[A,B,C], y: Tuple3[A,B,C]): Boolean =
      if TA.lt(x._1,y._1) then true
      else if TA.lt(y._1,x._1) then false
      else if TB.lt(x._2,y._2) then true
      else if TB.lt(y._2,x._2) then false
      else TC.lt(x._3,y._3)
    override def checkInvariant(x: Tuple3[A,B,C]): Unit =
      TA.checkInvariant(x._1)
      TB.checkInvariant(x._2)
      TC.checkInvariant(x._3)

  given TestableTuple4[A,B,C,D](using TA: Testable[A], TB: Testable[B], TC: Testable[C], TD: Testable[D]): Testable[Tuple4[A,B,C,D]] with
    val name = s"Tuple4[${TA.name}, ${TB.name}, ${TC.name}, ${TD.name}]"
    def parse: Src => Tuple4[A,B,C,D] =
      pTuple(TA.parse,TB.parse,TC.parse,TD.parse)
    override def _show(x: Tuple4[A,B,C,D]): String =
      s"(${TA.show(x._1)}, ${TB.show(x._2)}, ${TC.show(x._3)}, ${TD.show(x._4)})"
    override def copy(x: Tuple4[A,B,C,D]): Tuple4[A,B,C,D] =
      (TA.copy(x._1), TB.copy(x._2), TC.copy(x._3), TD.copy(x._4))
    override def equiv(x: Tuple4[A,B,C,D], y: Tuple4[A,B,C,D]): Boolean =
      TA.equiv(x._1,y._1) && TB.equiv(x._2,y._2) && TC.equiv(x._3,y._3) && TD.equiv(x._4,y._4)
    override def lt(x: Tuple4[A,B,C,D], y: Tuple4[A,B,C,D]): Boolean =
      if TA.lt(x._1,y._1) then true
      else if TA.lt(y._1,x._1) then false
      else if TB.lt(x._2,y._2) then true
      else if TB.lt(y._2,x._2) then false
      else if TC.lt(x._3,y._3) then true
      else if TC.lt(y._3,x._3) then false
      else TD.lt(x._4,y._4)
    override def checkInvariant(x: Tuple4[A,B,C,D]): Unit =
      TA.checkInvariant(x._1)
      TB.checkInvariant(x._2)
      TC.checkInvariant(x._3)
      TD.checkInvariant(x._4)

  given TestableTuple5[A,B,C,D,E](using TA: Testable[A], TB: Testable[B], TC: Testable[C], TD: Testable[D], TE: Testable[E]): Testable[Tuple5[A,B,C,D,E]] with
    val name = s"Tuple5[${TA.name}, ${TB.name}, ${TC.name}, ${TD.name}, ${TE.name}]"
    def parse: Src => Tuple5[A,B,C,D,E] =
      pTuple(TA.parse,TB.parse,TC.parse,TD.parse,TE.parse)
    override def _show(x: Tuple5[A,B,C,D,E]): String =
      s"(${TA.show(x._1)}, ${TB.show(x._2)}, ${TC.show(x._3)}, ${TD.show(x._4)}, ${TE.show(x._5)})"
    override def copy(x: Tuple5[A,B,C,D,E]): Tuple5[A,B,C,D,E] =
      (TA.copy(x._1), TB.copy(x._2), TC.copy(x._3), TD.copy(x._4), TE.copy(x._5))
    override def equiv(x: Tuple5[A,B,C,D,E], y: Tuple5[A,B,C,D,E]): Boolean =
      TA.equiv(x._1,y._1) && TB.equiv(x._2,y._2) && TC.equiv(x._3,y._3) && TD.equiv(x._4,y._4) && TE.equiv(x._5,y._5)
    override def lt(x: Tuple5[A,B,C,D,E], y: Tuple5[A,B,C,D,E]): Boolean =
      if TA.lt(x._1,y._1) then true
      else if TA.lt(y._1,x._1) then false
      else if TB.lt(x._2,y._2) then true
      else if TB.lt(y._2,x._2) then false
      else if TC.lt(x._3,y._3) then true
      else if TC.lt(y._3,x._3) then false
      else if TD.lt(x._4,y._4) then true
      else if TD.lt(y._4,x._4) then false
      else TE.lt(x._5,y._5)
    override def checkInvariant(x: Tuple5[A,B,C,D,E]): Unit =
      TA.checkInvariant(x._1)
      TB.checkInvariant(x._2)
      TC.checkInvariant(x._3)
      TD.checkInvariant(x._4)
      TE.checkInvariant(x._5)

  given TestableTuple6[A,B,C,D,E,F](using TA: Testable[A], TB: Testable[B], TC: Testable[C], TD: Testable[D], TE: Testable[E], TF: Testable[F]): Testable[Tuple6[A,B,C,D,E,F]] with
    val name = s"Tuple6[${TA.name}, ${TB.name}, ${TC.name}, ${TD.name}, ${TE.name}, ${TF.name}]"
    def parse: Src => Tuple6[A,B,C,D,E,F] =
      pTuple(TA.parse,TB.parse,TC.parse,TD.parse,TE.parse,TF.parse)
    override def _show(x: Tuple6[A,B,C,D,E,F]): String =
      s"(${TA.show(x._1)}, ${TB.show(x._2)}, ${TC.show(x._3)}, ${TD.show(x._4)}, ${TE.show(x._5)}, ${TF.show(x._6)})"
    override def copy(x: Tuple6[A,B,C,D,E,F]): Tuple6[A,B,C,D,E,F] =
      (TA.copy(x._1), TB.copy(x._2), TC.copy(x._3), TD.copy(x._4), TE.copy(x._5), TF.copy(x._6))
    override def equiv(x: Tuple6[A,B,C,D,E,F], y: Tuple6[A,B,C,D,E,F]): Boolean =
      TA.equiv(x._1,y._1) && TB.equiv(x._2,y._2) && TC.equiv(x._3,y._3) && TD.equiv(x._4,y._4) && TE.equiv(x._5,y._5) && TF.equiv(x._6,y._6)
    override def lt(x: Tuple6[A,B,C,D,E,F], y: Tuple6[A,B,C,D,E,F]): Boolean =
      if TA.lt(x._1,y._1) then true
      else if TA.lt(y._1,x._1) then false
      else if TB.lt(x._2,y._2) then true
      else if TB.lt(y._2,x._2) then false
      else if TC.lt(x._3,y._3) then true
      else if TC.lt(y._3,x._3) then false
      else if TD.lt(x._4,y._4) then true
      else if TD.lt(y._4,x._4) then false
      else if TE.lt(x._5,y._5) then true
      else if TE.lt(y._5,x._5) then false
      else TF.lt(x._6,y._6)
    override def checkInvariant(x: Tuple6[A,B,C,D,E,F]): Unit =
      TA.checkInvariant(x._1)
      TB.checkInvariant(x._2)
      TC.checkInvariant(x._3)
      TD.checkInvariant(x._4)
      TE.checkInvariant(x._5)
      TF.checkInvariant(x._6)


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
    * To use this as a replacement for `Testable[Array[Array[A]]]`, precede the
    * `test` with a `given`, as in
    * ```
    *   given hwtest.Testable[Array[Array[Int]]] = hwtest.Testable.TestableGrid[Int]
    *   test("column", column, "matrix")
    * ```
    * However, this can also affect the display of *other* tests that involve
    * an `Array[Array[Int]`. If necessary, you can avoid that by surounding the
    * `given...` and `test...` with `{` and `}`.
    */
  def TestableGrid[A: reflect.ClassTag](using TA: Testable[A]): Testable[Array[Array[A]]] = new Testable[Array[Array[A]]]:
    // do everything the same as regular Testable[Array[Array[A]]] except format
    val TAAA = TestableArray[Array[A]]
    val name = TAAA.name
    def parse: Src => Array[Array[A]] = TAAA.parse
    override def copy(x: Array[Array[A]]) = TAAA.copy(x)
    override def equiv(x: Array[Array[A]], y: Array[Array[A]]) = TAAA.equiv(x,y)
    override def lt(x: Array[Array[A]], y: Array[Array[A]]) = TAAA.lt(x,y)

    // note that checkInvariant does NOT check that the grid is rectangular
    override def checkInvariant(x: Array[Array[A]]): Unit = { TAAA.checkInvariant(x) }

    // _format pads elements to make each column line up nicely
    override def _format(g: Array[Array[A]]): String =
      if g == null || g.isEmpty || g.exists(_ == null) then return TAAA.show(g)
      val numColumns = g(0).length
      // bail out to default if grid is not rectangular
      if g.exists(_.length != numColumns) then return TAAA.show(g)

      val strs = g.map(_.map(TA.show))
      for col <- 0 until numColumns do
        val maxLen = strs.map(_.apply(col).length).max
        def pad(s: String) = List.fill(maxLen-s.length)(' ').mkString + s
        for row <- strs do row(col) = pad(row(col))
      strs.map(_.mkString("  Array(",",",")")).mkString("Array(\n",",\n","\n)")
