import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw
import hwtest.searchtrees.*

class hwSearchTreeTest extends AnyFunSuite:
  test("check SearchTrees") {
    testhw(hwSearchTree, """
E E
L"cat" E
TL"cat" "dog" E L"dog"
TL"alligator" "hippo" L"snake" TE "hippo" L"snake"
TTL"alligator" "cat" L"dog" "hippo" TL"moose" "snake" L"tyrannosaurus"
TTE "cat" L"dog" "hippo" TL"moose" "snake" L"tyrannosaurus rex"
$
87 E 87 L999 # deliberately wrong answer to test error message
186 L132 186 TE 132 E
69 TE 91 TTE 133 TE 192 E 165 L120 69 TE 91 TTE 133 L192 165 L120
63 TE 26 TTE 136 TE 135 E 66 TE 86 TE 158 L71 63 TE 26 TTE 136 TE 135 E 66 TE 86 TE 158 E # deliberately wrong answer to test error message
41 TTTE 121 E 113 TE 109 E 81 TTE 160 TTE 16 TE 115 TE 79 E 1 L82 102 TE 173 E 41 TTTE 121 E 113 L109 81 TTE 160 TTE 16 TE 115 L79 1 L82 102 TE 173 E
$""", """
CS123: hwSearchTree (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing tail at ###
....
Test #5 *** FAILED ***
  tree =
                ____"hippo"____
               /               \
           _"cat"_          _"snake"_
          /       \        /         \
    "alligator"  "dog" "moose" "tyrannosaurus"
  Expected answer:
        ____"hippo"____
       /               \
    "cat"          __"snake"__
        \         /           \
       "dog"  "moose" "tyrannosaurus rex"
  Received answer:
        ___"hippo"___
       /             \
    "cat"         _"snake"_
        \        /         \
       "dog" "moose" "tyrannosaurus"
Passed 4/5 tests in ###
Begin testing pair at ###
Test #1 *** FAILED ***
  x = 87
  tree =
    <empty tree>
  Expected answer: (87, Node(Empty,999,Empty))
  Received answer: (87, Empty)
..
Test #4 *** FAILED ***
  x = 63
  tree =
     26
       \
       66
      /  \
    136  86
       \   \
       135 158
              \
              71
  Expected answer: (63, Node(Empty,26,Node(Node(Empty,136,Node(Empty,135,Empty)),66,Node(Empty,86,Node(Empty,158,Empty)))))
  Received answer: (63, Node(Empty,26,Node(Node(Empty,136,Node(Empty,135,Empty)),66,Node(Empty,86,Node(Empty,158,Node(Empty,71,Empty))))))
.
Passed 3/5 tests in ###""")
  }

object hwSearchTree extends hwtest.hw("CS123"):
  import hwtest.searchtrees.*

  def userName = "Margaret Hamilton"

  def tail[A](tree: SearchTree[A]): SearchTree[A] = tree match
    case Empty => Empty
    case Node(Empty, x, b) => b
    case Node(a, x, b) => Node(tail(a), x, b)
  test("tail", tail[String], "tree")

  def pair(x: Int, tree: SearchTree[Int]): (Int, SearchTree[Int]) =
    (x, tree)
  test("pair", pair, "x", "tree")
