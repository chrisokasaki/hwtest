import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwNumsTest extends AnyFunSuite:
  test("check Int/Long/BigInt types") {
    testhw(hwNums, """
# square
2 4
10 100
0 -999 # deliberately wrong answer to test error message
$
# add
1 3 4
9 1 10
5 6 1111 # deliberately wrong answer to test error message
$
# mult
2000000000 1500000000 3000000000000000000
123 456789 123456789 # deliberately wrong answer to test error message
$
# multAndSquareLength
20000000000000 30000000000 48
20 300 20300 # deliberately wrong answer to test error message
$""", """
CS123: hwNums (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing square at ###
..
Test #3 *** FAILED ***
  n = 0
  Expected answer: -999
  Received answer: 0
Passed 2/3 tests in ###
Begin testing add at ###
..
Test #3 *** FAILED ***
  x = 5
  y = 6
  Expected answer: 1111
  Received answer: 11
Passed 2/3 tests in ###
Begin testing mult at ###
.
Test #2 *** FAILED ***
  x = 123
  y = 456789
  Expected answer: 123456789
  Received answer: 56185047
Passed 1/2 tests in ###
Begin testing multAndSquareLen at ###
.
Test #2 *** FAILED ***
  x = 20
  y = 300
  Expected answer: 20300
  Received answer: 8
Passed 1/2 tests in ###""")
  }

object hwNums extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  def square(n: Int): Int = n*n
  test("square", square, "n")

  def add(x: Int, y: Int): Int = x+y
  test("add", add, "x", "y")

  def mult(x: Long, y: Long): Long = x*y
  test("mult", mult, "x", "y")

  def multAndSquareLen(x: BigInt, y: BigInt): Int = ((x*y)*(x*y)).toString.length
  test("multAndSquareLen", multAndSquareLen, "x", "y")
