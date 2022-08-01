import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw
import hwtest.binarytrees.*

class hwBinaryTreeTest extends AnyFunSuite:
  test("check BinaryTrees") {
    testhw(hwBinaryTree, """
E E
L"a" L"a"
T"a" L"b" L"c" T"a" L"c" L"b"
T"when"
  T"the"
    L"political bands"
    E
  T"in the"
    T"course of"
      E
      L"human events it becomes"
    T"necessary"
      L"for one people"
      L"to dissolve"
T"when"
  T"in the"
    T"course of"
      E
      L"human events it becomes"
    T"necessary"
      L"for one people"
      L"to dissolve"
  T"the"
    L"political bands!!!"
    E
# deliberately wrong answer to test error message
$
55 T103 E E T55 E T103 E E
57 T148 T110 E T160 E T13 E E T64 E E T57 T148 T110 E E T160 E T13 E E T64 E E
62 T127 T13 E T159 E E T185 T55 E E T178 E T43 E E T62 T13 E T159 E E T127 T55 E E T185 T178 E E T43 E E
73 T61 E T150 T177 T138 E E T76 E T197 E T144 E T49 E E T108 T114 E E T116 E T136 E E T73 T61 E E T150 T177 T138 E E T76 E T197 E T144 E T49 E E T108 T114 E E T116 E T136 E E # deliberately wrong answer to test error message
$
1 E
1 E
2 L5
2 L5
3 T4 L7 L19
3 T4 L7 L19
4 T148 T110 E T160 E T13 E E T64 T2000 L22 T1456 L15 L-15 E
4 T148 T110 E T160 E T13 L-15 E T64 T2000 L22 T1456 L15 E E # deliberately wrong answer to test error message
4 T148 T110 E T160 E T13 E E T64 T2000 L22 T1456 L15 E L-15
4 T148 T110 E T160 E T13 E E T64 T2000 L22 T1456 L15 E L-15
$""", """
CS123: hwBinaryTree (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing flip at ###
...
Test #4 *** FAILED ***
  tree =
                 __________"when"__________
                /                          \
             "the"           ___________"in the"___________
             /              /                              \
    "political bands" "course of"                     _"necessary"_
                              \                      /             \
                   "human events it becomes" "for one people" "to dissolve"
  Expected answer:
                              ______________"when"______________
                             /                                  \
              ___________"in the"___________                   "the"
             /                              \                  /
       "course of"                     _"necessary"_ "political bands!!!"
               \                      /             \
    "human events it becomes" "for one people" "to dissolve"
  Received answer:
                              ______________"when"______________
                             /                                  \
              ___________"in the"___________                   "the"
             /                              \                  /
       "course of"                     _"necessary"_  "political bands"
               \                      /             \
    "human events it becomes" "for one people" "to dissolve"
Passed 3/4 tests in ###
Begin testing insert at ###
...
Test #4 *** FAILED ***
  x = 73
  tree =
        61
          \
         _150_
        /     \
      177     108
      / \     / \
    138 76  114 116
          \        \
          197      136
             \
             144
                \
                49
  Expected answer:
        73
       /  \
      61 _150_
        /     \
      177     108
      / \     / \
    138 76  114 116
          \        \
          197      136
             \
             144
                \
                49
  Received answer:
         73
           \
         __61__
        /      \
      150      108
      / \      / \
    138 177  114 116
        / \         \
       76 197       136
             \
             144
                \
                49
Passed 3/4 tests in ###
Begin testing pair at ###
...
Test #4 *** FAILED ***
  x = 4
  tree =
       ____148____
      /           \
    110           64
       \         /
       160     2000
          \    /  \
          13  22 1456
                 /  \
                15  -15
  Expected answer: (4, Node(148,Node(110,Empty,Node(160,Empty,Node(13,Node(-15,Empty,Empty),Empty))),Node(64,Node(2000,Node(22,Empty,Empty),Node(1456,Node(15,Empty,Empty),Empty)),Empty)))
  Received answer: (4, Node(148,Node(110,Empty,Node(160,Empty,Node(13,Empty,Empty))),Node(64,Node(2000,Node(22,Empty,Empty),Node(1456,Node(15,Empty,Empty),Node(-15,Empty,Empty))),Empty)))
.
Passed 4/5 tests in ###""")
  }

object hwBinaryTree extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  def flip[A](tree: BinaryTree[A]): BinaryTree[A] = tree match
    case Empty => Empty
    case Node(x, a, b) => Node(x, b, a)
  test("flip", flip[String], "tree")

  def insert(x: Int, tree: BinaryTree[Int]): BinaryTree[Int] = tree match
    case Empty => Node(x, Empty, Empty)
    case Node(y, a, b) =>
      if y % 2 == 0 then Node(x, insert(y, a), b)
      else Node(x, a, insert(y, b))
  test("insert", insert, "x", "tree")

  def pair(x: Int, tree: BinaryTree[Int]): (Int, BinaryTree[Int]) =
    (x, tree)
  test("pair", pair, "x", "tree")
