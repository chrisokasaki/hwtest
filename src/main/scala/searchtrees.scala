package hwtest

// this package is almost identical to binarytrees
// the main difference is that the Node type for SearchTree is arranged
//   Node(left, item, right)
// whereas the Node type for BinaryTree is arranged
//   Node(item, left, right)
// for now, the tester does NOT attempt to check the search tree
// ordering invariant, but that may be added later

package searchtrees

enum SearchTree[+A]:
  case Empty
  case Node(left: SearchTree[A], item: A, right: SearchTree[A])

export SearchTree.{Empty, Node}

object SearchTree:

  import hwtest.parsers.*
  import hwtest.{Testable,Src}

  given TestableSearchTree[A](using TA : Testable[A]): Testable[SearchTree[A]] =
    new Testable[SearchTree[A]]:
      val name = s"SearchTree[${TA.name}]"
      def parse: Src => SearchTree[A] =
        choose(
          'E' -> const(Empty),
          'L' -> chain(TA.parse, Node(Empty,_,Empty)),
          'T' -> chain(parse, TA.parse, parse, Node(_,_,_))
        )
      override def _show(x: SearchTree[A]): String =
        def map(tree: SearchTree[A]): SearchTree[String] = tree match
          case Empty => Empty
          case Node(left, item, right) =>
            Node(map(left), TA.show(item), map(right))
        map(x).toString()
      override def _format(x: SearchTree[A]): String =
        import binarytrees.BinaryTree
        def convert(tree: SearchTree[A]): BinaryTree[A] = tree match
          case Empty => BinaryTree.Empty
          case Node(left, item, right) =>
            BinaryTree.Node(item, convert(left), convert(right))
        BinaryTree.draw(convert(x), TA.show)
      override def equiv(x: SearchTree[A], y: SearchTree[A]): Boolean =
        (x,y) match
          case (Empty, Empty) => true
          case (Node(xleft,xitem,xright), Node(yleft,yitem,yright)) =>
            TA.equiv(xitem,yitem) && equiv(xleft,yleft) && equiv(xright,yright)
          case (_, _) => false
      override def lt(x: SearchTree[A], y: SearchTree[A]): Boolean =
        (x,y) match // treat Empty as less than Node
          case (Empty, Empty) => false
          case (Empty, Node(_,_,_)) => true
          case (Node(_,_,_), Empty) => false
          case (Node(xleft,xitem,xright), Node(yleft,yitem,yright)) =>
            if TA.lt(xitem,yitem) then true
            else if TA.lt(yitem,xitem) then false
            else if lt(xleft,yleft) then true
            else if lt(yleft,xleft) then false
            else lt(xright,yright)
      override def copy(x: SearchTree[A]): SearchTree[A] = x match
        case Empty => Empty
        case Node(left, item, right) =>
          Node(copy(left), TA.copy(item), copy(right))
      override def checkInvariant(x: SearchTree[A]): Unit = x match
        case Empty => {}
        case Node(left, item, right) =>
          TA.checkInvariant(item)
          checkInvariant(left)
          checkInvariant(right)
