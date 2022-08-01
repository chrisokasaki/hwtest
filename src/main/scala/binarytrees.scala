package hwtest

/** Provides a simple form of binary trees for use in homeworks. */
package binarytrees

/** An `enum` for binary trees with an element at each node.
  *
  * Example: `Node(1, Node(2, Empty, Empty), Empty)`
  *
  * The `Node` and `Empty` constructors are exported so they
  * can be referred with or without the `BinaryTree.` prefix,
  * as in `BinaryTree.Node`.
  */
enum BinaryTree[+A]:
  case Empty
  case Node(item: A, left: BinaryTree[A], right: BinaryTree[A])

export BinaryTree.{Empty, Node}

object BinaryTree:
  import hwtest.parsers.*
  import hwtest.{Testable,Src}

  /** `Testable` instance for binary trees.
    *
    * When appropriate, the trees are displayed in an ASCII-art pictorial format.
    */
  given TestableBinaryTree[A](using TA : Testable[A]): Testable[BinaryTree[A]] =
    new Testable[BinaryTree[A]]:
      val name = s"BinaryTree[${TA.name}]"
      def parse(src: Src): BinaryTree[A] =
        def pTree: Parser[BinaryTree[A]] =
          choose(
            'E' -> const(Empty),
            'L' -> chain(TA.parse, Node(_,Empty,Empty)),
            'T' -> chain(TA.parse, pTree, pTree, Node(_,_,_))
          )
        pTree(src)
      override def equiv(x: BinaryTree[A], y: BinaryTree[A]): Boolean =
        (x,y) match
          case (Empty, Empty) => true
          case (Node(xitem,xleft,xright), Node(yitem,yleft,yright)) =>
            TA.equiv(xitem,yitem) && equiv(xleft,yleft) && equiv(xright,yright)
          case (_, _) => false
      override def lt(x: BinaryTree[A], y: BinaryTree[A]): Boolean =
        (x,y) match // treat Empty as less than Node
          case (Empty, Empty) => false
          case (Empty, Node(_,_,_)) => true
          case (Node(_,_,_), Empty) => false
          case (Node(xitem,xleft,xright), Node(yitem,yleft,yright)) =>
            if TA.lt(xitem,yitem) then true
            else if TA.lt(yitem,xitem) then false
            else if lt(xleft,yleft) then true
            else if lt(yleft,xleft) then false
            else lt(xright,yright)
      override def copy(x: BinaryTree[A]): BinaryTree[A] = x match
        case Empty => Empty
        case Node(item, left, right) =>
          Node(TA.copy(item), copy(left), copy(right))
      override def checkInvariant(x: BinaryTree[A]): Unit = x match
        case Empty => {}
        case Node(item, left, right) =>
          TA.checkInvariant(item)
          checkInvariant(left)
          checkInvariant(right)
      override def _show(x: BinaryTree[A]): String =
        def map(tree: BinaryTree[A]): BinaryTree[String] = tree match
          case Empty => Empty
          case Node(item, left, right) =>
            Node(TA.show(item), map(left), map(right))
          map(x).toString()
      override def _format(x: BinaryTree[A]): String = draw(x, TA.show)

  /** Converts a tree into an ASCII-art picture of the tree.
    *
    * @param tree the tree to be drawn
    * @param show function that renders an `A` as a string
    * @returns a (usually multi-line) string representing the drawing
    */
  def draw[A](tree: BinaryTree[A], show: A => String): String =
    def showAll(tree: BinaryTree[A]): BinaryTree[String] =
      tree match
        case Empty => Empty
        case Node(item, left, right) =>
          Node(show(item), showAll(left), showAll(right))

    format(showAll(tree)).rows.map(_.expand).mkString("\n  ", "\n  ", "")

  private def format(tree: BinaryTree[String]): Box = tree match
    case Empty => box("<empty tree>") // only if the entire tree is empty
    case Node(item, Empty, Empty) => box(item)
    case Node(item, left, Empty) =>
      val upper = box(item)
      val lbox = format(left).addNorthEastLink
      stack(upper,upper.leftDown,lbox,lbox.rightMid)
    case Node(item, Empty, right) =>
      val upper = box(item)
      val rbox = format(right).addNorthWestLink
      stack(upper,upper.rightDown,rbox,rbox.leftMid)
    case Node(item, left, right) =>
      val name = item
      var lbox = format(left).addNorthEastLink
      var rbox = format(right).addNorthWestLink
      val squish = lbox.overlap(rbox) // how far can we squish the boxes together?
      var padding = 1 - squish // force at least 1 space difference

      // separation is the # of spaces between the / and \ links
      var separation = lbox.trailing + rbox.leading + padding
      if name.length%2 != separation%2 then
        // match parity so everything is symmetrical
        // for example, when a name is extended (eg "abc" to "__abc__")
        // there will be the same number of _'s on each side
        padding += 1
        separation += 1
      // the next two lines deal with the special cases where a small box is
      // completely absorbed into a large box (which can happen when the large
      // box has more rows at the bottom that extend underneath and past the
      // small box)
      if lbox.width + padding < 0 then lbox = lbox.padLeft(-padding-lbox.width)
      var bigWidth = (lbox.width + rbox.width + padding) max lbox.width

      // add underscores to the name if needed to bridge the distance between
      // the / and \ links
      val extendedName = if name.length >= separation then name
                         else
                           val extension = "_" * ((separation - name.length)/2)
                           extension + name + extension
      val upper = box(extendedName)
      val lower = Box(bigWidth, lbox.join(rbox,padding))
      stack(upper, ((extendedName.length-separation) max 0)/2,
            lower, lbox.rightMid)

  // Internals of the tree drawing logic
  private val LEFT = "/"
  private val RIGHT = "\\"
  private val BOTTOM = "_"
  private def spaces(n: Int): String = " " * n

  private final case class Row(offset: Int, text: String):
    def padLeft(n: Int) = Row(offset+n, text)
    def length = offset + text.length
    def expand = spaces(offset) + text

    def rightMid = offset + (text.length+1)/2
    def leftMid = offset + text.length/2 - 1

    // only used when the row is nothing but text
    // (no leading or trailing spaces)
    def leftDown: Int = (text.length-1)/3
    def rightDown: Int = text.length-1-leftDown

  private final case class Box(width: Int, rows: Vector[Row]):

    // add a new row to the top of this box, flush left
    def +:(row: Row) = Box(width max row.length, row +: rows)

    // add a link (either / or \) above the first-row text
    def addNorthEastLink = Row(rightMid,LEFT) +: this
    def addNorthWestLink = Row(leftMid,RIGHT) +: this

    // calculate the offsets just to the right/left of the middle of the first-row text
    def head = rows.head
    def rightMid = head.rightMid
    def leftMid = head.leftMid
    def rightDown = head.rightDown
    def leftDown = head.leftDown

    def leading = head.offset // # of leading spaces on first row
    def trailing = width - head.length // # of trailing spaces on first row

    def spacesBetween(pair: (Row,Row)): Int =
      val (left,right) = pair
      val spacesAtEndOfLeft = width - left.length
      val spacesAtBeginningOfRight = right.offset
      spacesAtEndOfLeft + spacesAtBeginningOfRight
    def overlap(r: Box): Int = rows.zip(r.rows).map(spacesBetween).min
    def join(r: Box, padding: Int): Vector[Row] =
      def combine(pair: (Row, Row)): Row =
        val (a,b) = pair
        if b == null then a
        else if a == null then Row(width + padding + b.offset, b.text)
        else
          val filler = spaces(spacesBetween(a,b) + padding)
          Row(a.offset, a.text + filler + b.text)
      rows.zipAll(r.rows,null,null).map(combine)

    def padRight(n: Int): Box = Box(width+n, rows)
    def padLeft(n: Int): Box = Box(width+n, rows.map(_.padLeft(n)))

  private def box(name: String) = Box(name.length, Vector(Row(0,name)))

  private def stack(upper: Box, upperAlign: Int, lower: Box, lowerAlign: Int): Box =
    var a = upper
    var b = lower
    val ax = upperAlign
    val bx = lowerAlign
    if ax < bx then a = a.padLeft(bx-ax)
    else if ax > bx then b = b.padLeft(ax-bx)

    Box(a.width max b.width, a.rows ++ b.rows)
