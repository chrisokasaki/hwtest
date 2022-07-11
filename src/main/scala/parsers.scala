package hwtest

/** Parsers for various types of test data, as well as combinators for
  * building custom parsers.
  *
  * Custom parsers typically work with Polish notation (aka prefix notation).
  *
  * Example: Parser for a binary tree.
  * ```
  *   enum Tree:
  *     case Empty
  *     case Node(item: Int, left: Tree, right: Tree)
  *
  *   def pTree: Parser[Tree] =
  *     choose(
  *       'E' -> const(Tree.Empty),
  *       'N' -> chain(pInt, pTree, pTree, Tree.Node(_,_,_))
  *     )
  * ```
  * Then, given the input (from a [[hwtest.Src]])
  * ```
  *   N1 E N2 E E
  * ```
  * or
  * ```
  *   N 1 E N 2 E E
  * ```
  * `pTree` would return `Node(1, Empty, Node(2, Empty, Empty))`.
  *
  * Within the test data, a `#` character (not inside quotes) indicates a
  * comment that extends to the end of the line. Parsers will treat such
  * comments as ordinary whitespace.
  *
  * Errors encountered during parsing are fatal.
  *
  * Design note: We could use `given`/`using` to bypass a certain amount of
  * boilerplate here but deliberately do not to minimize the risk of beginning
  * students running into confusing error messages involving topics they have
  * not yet learned.
  */
package parsers

/** The type of a `Parser` that, when given a `Src` (essentially an iterator
  * through text), consumes characters to produce a value of type `T`.
  *
  * Every `Parser` should begin by discarding any leading whitespace.
  */
type Parser[T] = Src => T

/** Returns a `Parser` that produces `v` without consuming any
  * characters from `src`.
  */
def const[T](v: T): Parser[T] = src => v

private def prepare(src: Src): Unit =
  // prepare will be called at the beginning of most parsers
  // to advance to the next token
  src.skipWhite()
  if !src.hasNext then parseError("Unexpected end of source.")

private def isSimple(c:Char) = c.isLetter || c.isDigit || c == '-' || c == '+' || c == '.'

/** Reads the next chunk of simple text from the source, where "simple" means
  * letters, digits, `-`, `+`, and `.`.
  *
  * Stops at the first non-simple character, which might or might not be
  * whitespace, or at the end of the source. For example, if the source was
  * ```
  *   __-123.4) ...
  * ```
  * where the `__` represents whitespace, `token` would skip the whitespace,
  * and return the `-123.4`, leaving the `)` still in the source.
  *
  * Throws a `ParseError` if the chunk is empty.
  */
def token(): Parser[String] = src =>
  prepare(src)
  val builder = scala.collection.mutable.StringBuilder()
  while isSimple(src.head) do
    // does not check for end of src because src.head handles that gracefully
    builder += src.next()
  if builder.nonEmpty then
    builder.toString
  else if src.hasNext then
    parseError(s"Unexpected character: ${src.head}")
  else
    parseError("Unexpected end of test data.")

/** Wraps a function (like _.toInt) that converts a String to another type,
  * but that might throw an exception, into one that will do the same
  * conversion but will call call `parseError` instead.
  */
def conversion[A](f: String => A, desiredType: String): String => A =
  tok =>
    try
      f(tok)
    catch case e: Exception =>
      parseError(s"Could not convert \"$tok\" to $desiredType.\n${e.getMessage()}")

/** A parser for `Int`s.
  *
  * Throws an exception if the next token cannot be converted to an `Int`,
  * or if there is no next token.
  */
def pInt: Parser[Int] = chain(token(), conversion(_.toInt, "Int"))

/** A parser for `Long`s.
  *
  * Throws an exception if the next token cannot be converted to a `Long`,
  * or if there is no next token.
  */
def pLong: Parser[Long] = chain(token(), conversion(_.toLong, "Long"))

/** A parser for `BigInt`s.
  *
  * Throws an exception if the next token cannot be converted to a `BigInt`,
  * or if there is no next token.
  */
def pBigInt: Parser[BigInt] = chain(token(), conversion(BigInt(_), "BigInt"))

/** A parser for `Double`s.
  *
  * Throws an exception if the next token cannot be converted to a `Double`,
  * or if there is no next token.
  */
def pDouble: Parser[Double] = chain(token(), conversion(_.toDouble, "Double"))

/** Returns a new parser built from the given parser and function.
  *
  * When run, the new parser runs the given parser and transforms
  * its result using the function. The transformed result is
  * then returned as the result of the new parser.
  *
  * `chain` also comes in versions that are given up to six
  * parsers instead of one, together with a function that transforms
  * the combined results of the given parsers.
  *
  * Notice that the given parser (`pa`) is a delayed (by-name) parameter,
  * which allows `chain` to be used in recursive definitions. For example,
  * consider
  * ```
  *   def pNat: Int =
  *     choose(
  *       'S' -> chain(pNat, _+1),
  *       'Z' -> const(0)
  *     )
  * ```
  * Without the by-name parameter in `chain`, `pNat` would always
  * fall into infinite recursion.
  */
def chain[A,T](pa: => Parser[A], f: A => T): Parser[T] =
  src => f(pa(src))

/** Like `chain[A,T]` but takes two parsers and a function. */
def chain[A,B,T](pa: => Parser[A], pb: => Parser[B], f: (A,B) => T): Parser[T] =
  src => f(pa(src), pb(src))

/** Like `chain[A,T]` but takes three parsers and a function. */
def chain[A,B,C,T](
  pa: => Parser[A], pb: => Parser[B], pc: => Parser[C],
  f: (A,B,C) => T
): Parser[T] = src => f(pa(src), pb(src), pc(src))

/** Like `chain[A,T]` but takes four parsers and a function. */
def chain[A,B,C,D,T](
  pa: => Parser[A], pb: => Parser[B], pc: => Parser[C], pd: => Parser[D],
  f: (A,B,C,D) => T
): Parser[T] = src => f(pa(src), pb(src), pc(src), pd(src))

/** Like `chain[A,T]` but takes five parsers and a function. */
def chain[A,B,C,D,E,T](
  pa: => Parser[A], pb: => Parser[B], pc: => Parser[C], pd: => Parser[D], pe: => Parser[E],
  f: (A,B,C,D,E) => T
): Parser[T] = src => f(pa(src), pb(src), pc(src), pd(src), pe(src))

/** Like `chain[A,T]` but takes six parsers and a function. */
def chain[A,B,C,D,E,F,T](
  pa: => Parser[A], pb: => Parser[B], pc: => Parser[C], pd: => Parser[D], pe: => Parser[E], pf: => Parser[F],
  f: (A,B,C,D,E,F) => T
): Parser[T] = src => f(pa(src), pb(src), pc(src), pd(src), pe(src), pf(src))


/** Returns a `Parser` that, when run, uses the next non-whitespace character
  * (the "tag") to decide which of several parsers to run next.
  *
  * Example: Parse an `Either[Int, Int]`.
  * ```
  *   def pEitherIntInt: Parser[Either[Int, Int]] =
  *     choose(
  *       'L' -> chain(rInt, Left(_)),
  *       'R' -> chain(rInt, Right(_))
  *     )
  * ```
  * This would parse `L 5` as `Left(5)` and `R -99` as `Right(-99)`.
  * Whitespace can be omitted between the tag and the subsequent value, so
  * `R-99` would also be parsed `Right(-99)`. (This is one of the very few
  * places where whitespace can be omitted.)
  *
  * **WARNINGS:**
  *   - Tags are case sensitive.
  *   - All tags within a single `choose` should be distinct.
  *   - Tags should not be whitespace characters.
  *   - Do **NOT** use `$` as a tag, because it has special
  *     meaning in the tester as marking the end of a test.
  */
def choose[A](pairs: (Char, Parser[A])*): Parser[A] = src =>
  prepare(src)
  val c = src.next()
  pairs.find(_._1 == c).map(_._2) match
    case Some(parser) =>
      parser(src)
    case None =>
      val tags = pairs.map(p => s"${p._1}").mkString("[", ",", "]")
      parseError(s"Bad tag. Expected one of $tags but found '$c'.")

/** Returns the next `Boolean` from the source.
  *
  * In the test data, the `Boolean` should be formatted as `F` or `T`.
  */
def pBoolean: Parser[Boolean] =
  choose(
    'F' -> const(false),
    'T' -> const(true)
  )

/** Returns the next `Option[A]` from the source.
  *
  * In the test data, the `Option[A]` should be formatted as 'N' or
  * `S <data>`. The latter can also be written as `S<data>` (without the
  * space).
  */
def pOption[A](pa: Parser[A]): Parser[Option[A]] =
  choose(
    'N' -> const(None),
    'S' -> chain(pa, Some(_))
  )

/** Generic parser for binary trees with data at internal nodes but no
  * data at external nodes.
  *
  * When called, the parser reads trees formatted as
  *   - `E` for the empty tree
  *   - `T <data> <left-subtree> <right-subtree>` for a node with data
  *   - `L <data>` for a node with data and two empty subtrees
  * Note that `L <data>` is merely shorthand for `T <data> E E`. It is
  * mainly useful when generating test data by hand. If you are generating
  * test data programmatically, just use `E` and `T`.
  *
  * Whitespace is optional after the tag so `T <data> E E` could also
  * be written as `T<data> E E`.
  *
  * Example:
  * ```
  *   enum SizedBST:
  *     case Empty
  *     case Node(left: SizedBST, item: Int, right: SizedBST, size: Int)
  *
  *   def pSizedBST: SizedBST =
  *     pTree(pTuple(pInt,pInt),
  *           Empty,
  *           (tup,left,right) => Node(left, tup._1, right, tup._2))
  * ```
  * Then the tree `Node(Empty, 10, Node(Empty, 20, Empty, 1), 2)`
  * could be formatted in a test as
  * ```
  *   T10 2 E T20 1 E E
  * ```
  * or
  * ```
  *   T 10 2 E L 20 1
  * ```
  *
  * Although `T` is normally some type of tree, it is also possible to
  * use `pTree` on tree-like input data, even if the final result is
  * not itself a tree.
  *
  * @param pa a `Parser` for the data at each internal node (other than the two subtrees)
  * @param e the empty tree
  * @param node a function to combine data from `pA` and two subtrees into a new tree
  * @return a `Parser` for type `T`
  */
def pTree[A,T](pa: Parser[A], e: T, mkNode: (A,T,T) => T): Parser[T] =
  choose(
    'E' -> const(e),
    'T' -> chain(pa, pTree(pa,e,mkNode), pTree(pa,e,mkNode), mkNode),
    'L' -> chain(pa, mkNode(_,e,e))
  )

//////////////////////////////////////////////////
// Collections

/** Reads a collection of `A`s.
  *
  * Many built-in collections, such as list, arrays, sets, and maps, will
  * be formatted in the test data as
  * ```
  *   (element1 element2 ... elementN)
  * ```
  * instead of using Polish notation.
  *
  * An empty collection is written as
  * ```
  *   ()
  * ```
  *
  * `pColl` will use `pa` to read each element, save them all in a list, and
  * then call `convert` on the list of all the just-read elements to produce
  * the final result. Usually, that result will be some kind of
  * collection, such as a list or array or set, but `convert` has
  * the flexibility to return other types as well.
  *
  * @param pa the parser to be used for each individual `A`
  * @param convert function to convert a list of all the individual `A`s to the desired type (`B`)
  * @return a parser for `B`
  */
def pColl[A,B](pa: Parser[A], convert: List[A] => B): Parser[B] = src =>
  val buf = scala.collection.mutable.ListBuffer.empty[A]
  prepare(src)
  val c = src.next() // consume what should be the '('
  if c != '(' then parseError(s"Expected '(' but found '$c'.")

  // read and add items to the buffer until reach the ')'
  prepare(src)
  while src.head != ')' do
    buf += pa(src)
    prepare(src)

  src.next() // skip over the ')'

  convert(buf.toList)

/** Returns a `Parser` for a `List` of `A`s.
  *
  * The test data is formatted as
  * ```
  *   (<element1> <element2> ... <elementN>)
  * ```
  *
  * @param pa the parser to be used for each individual element
  */
def pList[A](pa: Parser[A]): Parser[List[A]] = pColl(pa, list => list)

/** Returns a `Parser` for a `Array` of `A`s.
  *
  * The test data is formatted as
  * ```
  *   (<element1> <element2> ... <elementN>)
  * ```
  * @param pa the parser to be used for each individual element
  */
def pArray[A : reflect.ClassTag](pa: Parser[A]): Parser[Array[A]] =
  pColl(pa, Array.from)

/** Returns a `Parser` for an immutable `Set` of `A`s.
  *
  * The test data is formatted as
  * ```
  *   (<element1> <element2> ... <elementN>)
  * ```
  *
  * **Warning:** The parser makes no effort to check for equivalent elements.
  * The creator of the test data should avoid including two elements in the
  * same set if they might be considered equivalent.
  *
  * @param pa the parser to be used for each individual element
  */
def pSet[A](pa: Parser[A]): Parser[scala.collection.immutable.Set[A]] =
  pColl(pa, scala.collection.immutable.Set.from)

/** Returns a `Parser` for an immutable `Map` of keys (`K`) and values (`V`).
  *
  * The test data is formatted as
  * ```
  *   (<key1> <value1> <key2> <value2> ... <keyN> <valueN>)
  * ```
  * Note that there are **NOT** extra parentheses around each individual
  * key-value pair.
  *
  * **Warning:** The parser makes no effort to check for equivalent keys.
  * The creator of the test data should avoid including two keys in the
  * same set if they might be considered equivalent.
  *
  * @param pk the parser to be used for each individual key
  * @param pv the parser to be used for each individual value
  */
def pMap[K,V](pk: Parser[K], pv: Parser[V]): Parser[scala.collection.immutable.Map[K,V]] =
  pColl(pTuple(pk,pv), Map.from)

//////////////////////////////////////////////////
// Tuples (up to size 6)

/** Returns a parser for a 2-tuple of values.
  *
  * The test data is formatted as
  * ```
  *   <element1> <element2>
  * ```
  * Note that the test data does **NOT** include parentheses around the
  * elements.
  *
  * @param pa a parser for values of type `A`
  * @param pb a parser for values of type `B`
  * @return a `Parser[(A,B)]` that uses the input parsers to read space-separated values of types `A` and `B`, and returns those values in a tuple.
  */
def pTuple[A,B](pa: Parser[A], pb: Parser[B]): Parser[(A,B)] =
  src => (pa(src), pb(src))

/** Returns a parser for a 3-tuple of values.
  *
  * The test data is formatted as
  * ```
  *   <element1> <element2> <element3>
  * ```
  * Note that the test data does **NOT** include parentheses around the
  * elements.
  *
  * @param pa a parser for values of type `A`
  * @param pb a parser for values of type `B`
  * @param pc a parser for values of type `C`
  * @return a `Parser[(A,B,C)]` that uses the input parsers to read space-separated values of types `A`, `B`, and `C`, and returns those values in a tuple.
  */
def pTuple[A,B,C](pa: Parser[A], pb: Parser[B], pc: Parser[C]): Parser[(A,B,C)] =
  src => (pa(src), pb(src), pc(src))

/** Returns a parser for a 4-tuple of values.
  *
  * The test data is formatted as
  * ```
  *   <element1> <element2> <element3> <element4>
  * ```
  * Note that the test data does **NOT** include parentheses around the
  * elements.
  *
  * @param pa a parser for values of type `A`
  * @param pb a parser for values of type `B`
  * @param pc a parser for values of type `C`
  * @param pd a parser for values of type `D`
  * @return a `Parser[(A,B,C,D)]` that uses the input parsers to read space-separated values of types `A`, `B`, `C`, and `D`, and returns those values in a tuple.
  */
def pTuple[A,B,C,D](pa: Parser[A], pb: Parser[B], pc: Parser[C], pd: Parser[D]): Parser[(A,B,C,D)] =
  src => (pa(src), pb(src), pc(src), pd(src))

/** Returns a parser for a 5-tuple of values.
  *
  * The test data is formatted as
  * ```
  *   <element1> <element2> <element3> <element4> <element5>
  * ```
  * Note that the test data does **NOT** include parentheses around the
  * elements.
  *
  * @param pa a parser for values of type `A`
  * @param pb a parser for values of type `B`
  * @param pc a parser for values of type `C`
  * @param pd a parser for values of type `D`
  * @param pe a parser for values of type `E`
  * @return a `Parser[(A,B,C,D,E)]` that uses the input parsers to read space-separated values of types `A`, `B`, `C`, `D`, and `E`, and returns those values in a tuple.
  */
def pTuple[A,B,C,D,E](pa: Parser[A], pb: Parser[B], pc: Parser[C], pd: Parser[D], pe: Parser[E]): Parser[(A,B,C,D,E)] =
  src => (pa(src), pb(src), pc(src), pd(src), pe(src))

/** Returns a parser for a 6-tuple of values.
  *
  * The test data is formatted as
  * ```
  *   <element1> <element2> <element3> <element4> <element5> <element6>
  * ```
  * Note that the test data does **NOT** include parentheses around the
  * elements.
  *
  * @param pa a parser for values of type `A`
  * @param pb a parser for values of type `B`
  * @param pc a parser for values of type `C`
  * @param pd a parser for values of type `D`
  * @param pe a parser for values of type `E`
  * @param pf a parser for values of type `F`
  * @return a `Parser[(A,B,C,D,E,F)]` that uses the input parsers to read space-separated values of types `A`, `B`, `C`, `D`, `E`, and `F`, and returns those values in a tuple.
  */
def pTuple[A,B,C,D,E,F](pa: Parser[A], pb: Parser[B], pc: Parser[C], pd: Parser[D], pe: Parser[E], pf: Parser[F]): Parser[(A,B,C,D,E,F)] =
  src => (pa(src), pb(src), pc(src), pd(src), pe(src), pf(src))

//////////////////////////////////////////////////
// Char and String

def pChar: Parser[Char] = src =>
  // character must be in 'x' format (including escape sequences)
  prepare(src)
  val open = src.next()
  if open != '\'' then parseError(s"Expected ' but found $open")
  val contents = readTo('\'', src)
  val treated = StringContext.processEscapes(contents)
  if treated.length != 1 then parseError(s"More than one character in '$contents'.")
  treated(0)

def pString: Parser[String] = src =>
  // character must be in "x" format (including escape sequences)
  prepare(src)
  val open = src.next()
  if open != '"' then parseError(s"Expected \" but found $open")
  val contents = readTo('\"', src)
  StringContext.processEscapes(contents)

// Used by `pChar`/`pString` to read up to the close quote.
// No special treatment is given to newlines so be careful about
// line-ending conventions.
// Can use \ to escape a quotation mark.
// Other escapes are allowed but are processed elsewhere (in `pChar`
// and `pString`).
private def readTo(endc: Char, src: Src): String =
  assert(endc == '\'' || endc == '\"')
  val builder = new collection.mutable.StringBuilder
  var escape = false
  while src.hasNext && (escape || src.head != endc) do
    val c = src.next()
    builder += c
    if escape then escape = false
    else if c == '\\' then escape = true
  if src.head == endc then
    src.next()
    builder.toString
  else // reached the end of the source file
    val name = if endc == '\'' then "Char" else "String"
    val contents = builder.toString
    val short = if contents.length <= 13 then contents else contents.take(10) + "..."
    parseError(s"No closing $endc found before end of file.  $name began $endc$short")


/** Prints an error message (extended with some hints), and halts the program. */
def parseError(msg: String): Nothing =
  println("!!!!! Error parsing test data:")
  println(s"!!!!! $msg")
  println("Hints:")
  println("  1. Did you comment out a test/ignoretest command (or testV/ignoretestV)?")
  println("  2. Did you alter the number or order of parameters of the next function")
  println("     to be tested? Or change a parameter or result type?")
  println("  3. Finally, if you've already checked 1 and 2 above, it is possible that")
  println("     the test data was malformed to begin with. Let your instructor know")
  println("     and include the entire output of the program.")
  throw StopTests()
