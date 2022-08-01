import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwArraysTest extends AnyFunSuite:
  test("check Arrays and Grids") {
    testhw(hwArrays, """
5 () ()
2 (1 5 20) (2 10 40)
3 (4 5 6 7) (8 15 18 21) # deliberately wrong answer to test error message
$
() ()
(1 5 20) (3 15 60)
(4 5 6 7) (12 15 18 28) # deliberately wrong answer to test error message
(-100) (-300)
$
() ()
(123) ("123")
(1 2 3) ("1" "2" "3")
(19 35) ("1935") # deliberately wrong answer to test error message
$
(1 2 3) ((1 1 1) (2 2 2) (3 3 3))
(1000 21 -35) ((1000 1000 1000) (21 21 21) (-35 -35 -35))
(1000 21 -35) ((1000 1000 1000) (21 21 21) (-35 -35 -3)) # deliberately wrong answer to test error message
$
(1 2 3) ((1 1 1) (2 2 2) (3 3 3))
(1000 21 -35) ((1000 1000 1000) (21 21 21) (-35 -35 -35))
(1000 21 -35) ((1000 1000 1000) (21 21 21) (-35 -35 -3)) # deliberately wrong answer to test error message
$""", """
CS123: hwArrays (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing multiply at ###
..
Test #3 *** FAILED ***
  x = 3
  arr = Array(4, 5, 6, 7)
  Expected answer: Array(8, 15, 18, 21)
  Received answer: Array(12, 15, 18, 21)
Passed 2/3 tests in ###
Begin testing tripleInPlace at ###
..
Test #3 *** FAILED ***
  arr = Array(4, 5, 6, 7)
  Expected answer: Array(12, 15, 18, 28)
  Received answer: Array(12, 15, 18, 21)
.
Passed 3/4 tests in ###
Begin testing stringify at ###
...
Test #4 *** FAILED ***
  arr = Array(19, 35)
  Expected answer: Array("1935")
  Received answer: Array("19", "35")
Passed 3/4 tests in ###
Begin testing gridify at ###
..
Test #3 *** FAILED ***
  arr = Array(1000, 21, -35)
  Expected answer: Array(
    Array(1000,1000,1000),
    Array(  21,  21,  21),
    Array( -35, -35,  -3)
  )
  Received answer: Array(
    Array(1000,1000,1000),
    Array(  21,  21,  21),
    Array( -35, -35, -35)
  )
Passed 2/3 tests in ###
Begin testing gridify (flat) at ###
..
Test #3 *** FAILED ***
  arr = Array(1000, 21, -35)
  Expected answer: Array(Array(1000, 1000, 1000), Array(21, 21, 21), Array(-35, -35, -3))
  Received answer: Array(Array(1000, 1000, 1000), Array(21, 21, 21), Array(-35, -35, -35))
Passed 2/3 tests in ###""")
  }

object hwArrays extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  def multiply(x: Int, arr: Array[Int]): Array[Int] =
    arr.map(x * _)
  test("multiply", multiply, "x", "arr")

  def tripleInPlace(arr: Array[Int]): Unit =
    for i <- arr.indices do
      arr(i) *= 3
  test("tripleInPlace", (arr:Array[Int]) => { tripleInPlace(arr); arr }, "arr")

  def stringify(arr: Array[Int]): Array[String] =
    arr.map(_.toString())
  test("stringify", stringify, "arr")

  def gridify(arr: Array[Int]): Array[Array[Int]] =
    val n = arr.length
    Array.tabulate(n,n)((i,j) => arr(i))

  {
    given hwtest.Testable[Array[Array[Int]]] = hwtest.Testable.TestableGrid[Int]
    test("gridify", gridify, "arr")
  }

  test("gridify (flat)", gridify, "arr")
