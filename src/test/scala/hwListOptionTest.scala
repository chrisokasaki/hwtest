import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwListOptionTest extends AnyFunSuite:
  test("check List/Option types") {
    testhw(hwListOption, """
(1 2 3 4) 10
() 0
$
(1 2 3 4) (4 3 2 1)
(3 1 4 2 5) (5 2 4 1 3)
(1 2) (1 2) # deliberately wrong answer to test error message
(1 2 3) (4 2 1) # deliberately wrong answer to test error message
(3 2 1) (3 2 1) # should display X because failure limit is 2
() ()
$
((1 2) () (5 4 3)) (1 2 5 4 3)
() ()
((1 2) () (3 4 5)) (1 2 3) # deliberately wrong answer to test error message
$
(("abc") () ("" "a" "b")) ("abc" "" "a" "b")
$
1 S1
-1 N
3 S33 # deliberately wrong answer to test error message
4 S34 # should display X because failureLimit is 1
5 S35 # and another X
100 S100
$
() N
() S() # deliberately wrong answer to test error message
(T F T) S(T F T)
(F F F) N # deliberately wrong answer to test error message
$""", """
CS123: hwListOption (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing sum at ###
..
Passed 2/2 tests in ###
Begin testing reverse at ###
..
Test #3 *** FAILED ***
  list = List(1, 2)
  Expected answer: List(1, 2)
  Received answer: List(2, 1)
Test #4 *** FAILED ***
  list = List(1, 2, 3)
  Expected answer: List(4, 2, 1)
  Received answer: List(3, 2, 1)
X.
Passed 3/6 tests in ###
Begin testing flatten[Int] at ###
..
Test #3 *** FAILED ***
  lol = List(List(1, 2), List(), List(3, 4, 5))
  Expected answer: List(1, 2, 3)
  Received answer: List(1, 2, 3, 4, 5)
Passed 2/3 tests in ###
***** Ignoring tests for flatten[String].
Begin testing positive at ###
[timeLimit = 1000ms, failureLimit = 1]
..
Test #3 *** FAILED ***
  n = 3
  Expected answer: Some(33)
  Received answer: Some(3)
XX.
Passed 3/6 tests in ###
Begin testing nonEmptyList at ###
.
Test #2 *** FAILED ***
  list = List()
  Expected answer: Some(List())
  Received answer: None
.
Test #4 *** FAILED ***
  list = List(false, false, false)
  Expected answer: None
  Received answer: Some(List(false, false, false))
Passed 2/4 tests in ###""")
  }

object hwListOption extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  def sum(list: List[Int]): Int = list.sum
  test("sum", sum, "list")

  def reverse(list: List[Int]): List[Int] = list.reverse
  test("reverse", reverse, "list")

  def flatten[A](lol: List[List[A]]): List[A] = lol.flatten
  test("flatten[Int]", flatten[Int], "lol")
  ignoretest("flatten[String]", flatten[String], "lol")

  def positive(n: Int): Option[Int] =
    if n > 0 then Some(n)
    else None
  test("positive", positive, "n")(timeLimit = 1000, failureLimit = 1)

  def nonEmptyList(list: List[Boolean]): Option[List[Boolean]] =
    if list.isEmpty then None
    else Some(list)
  test("nonEmptyList", nonEmptyList, "list")
