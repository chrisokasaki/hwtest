import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwMultiTest extends AnyFunSuite:
  test("check 3/4/5/6-parameter functions") {
    testhw(hwMulti, """
"sudoku" 34512 F
"sudoku 34512 false"
"badrobot" -123 T
"badderrobot -123 true"  # deliberately wrong answer to test error message
$
"sudoku" 34512 F
"i will be ignored"
$
97 "xyz" T (3 4 2)
"97 xyz true List(3, 4, 2)"
1234 "" F ()
"1234  false List()"
0 "yes" T (8 6 7 5)
"0 no true List(8, 6, 7, 5)" # deliberately wrong answer to test error message
$
97 "xyz" T (3 4 2)
"97 xyz true List(3, 4, 2)"
1234 "" F ()
"1234  false List()"  # deliberately wrong answer to test error message
$
"catdog" F -57 'Q' (5 6)
"catdog false -57 Q List(5, 6)"
"cat" T 15243 'w' (19 21 20)
"catapult true 15243 w List(19, 21, 20)" # deliberately wrong answer to test error message
$
"catdog-ignore" F -57 'Q' (5 6)
"ignored output"
$
'Z' 1976 "hello world" T 14 'W' (T F F)
"oops!" # deliberately wrong answer to test error message
'Z' 1976 "hello world" F 3 'c' (T F F)
"Z 1976 hello world false (3, c) List(true, false, false)"
$
'a' 1976 "world hello" T 3 'c' (T F F)
"ignored output"
$""", """
CS123: hwMulti (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing f3 at ###
.
Test #2 *** FAILED ***
  x = "badrobot"
  y = -123
  z = true
  Expected answer: "badderrobot -123 true"
  Received answer: "badrobot -123 true"
Passed 1/2 tests in ###
***** Ignoring tests for f3-ignore.
Begin testing f4 at ###
..
Test #3 *** FAILED ***
  a = 0
  b = "yes"
  c = true
  d = List(8, 6, 7, 5)
  Expected answer: "0 no true List(8, 6, 7, 5)"
  Received answer: "0 yes true List(8, 6, 7, 5)"
Passed 2/3 tests in ###
***** Ignoring tests for f4-ignore.
Begin testing f5 at ###
.
Test #2 *** FAILED ***
  a = "cat"
  b = true
  c = 15243
  d = 'w'
  e = List(19, 21, 20)
  Expected answer: "catapult true 15243 w List(19, 21, 20)"
  Received answer: "cat true 15243 w List(19, 21, 20)"
Passed 1/2 tests in ###
***** Ignoring tests for f5-ignore.
Begin testing f6 at ###
Test #1 *** FAILED ***
  a = 'Z'
  b = 1976
  c = "hello world"
  d = true
  e = (14, 'W')
  f = List(true, false, false)
  Expected answer: "oops!"
  Received answer: "Z 1976 hello world true (14,W) List(true, false, false)"
Test #2 *** FAILED ***
  a = 'Z'
  b = 1976
  c = "hello world"
  d = false
  e = (3, 'c')
  f = List(true, false, false)
  Expected answer: "Z 1976 hello world false (3, c) List(true, false, false)"
  Received answer: "Z 1976 hello world false (3,c) List(true, false, false)"
Passed 0/2 tests in ###
***** Ignoring tests for f6-ignore.""")
  }

object hwMulti extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  // test 3/4/5/6-parameter functions

  def f3(x: String, y: Int, z: Boolean): String =
    s"$x $y $z"
  test("f3", f3, "x", "y", "z")
  ignoretest("f3-ignore", f3, "x", "y", "z")

  def f4(a: Int, b: String, c: Boolean, d: List[Int]): String =
    s"$a $b $c $d"
  test("f4", f4, "a", "b", "c", "d")
  ignoretest("f4-ignore", f4, "a", "b", "c", "d")

  def f5(a: String, b: Boolean, c: Int, d: Char, e: List[Int]): String =
    s"$a $b $c $d $e"
  test("f5", f5, "a", "b", "c", "d", "e")
  ignoretest("f5-ignore", f5, "a", "b", "c", "d", "e")

  def f6(a: Char, b: Int, c: String, d: Boolean, e: (Int, Char), f: List[Boolean]): String =
    s"$a $b $c $d $e $f"
  test("f6", f6, "a", "b", "c", "d", "e", "f")
  ignoretest("f6-ignore", f6, "a", "b", "c", "d", "e", "f")
