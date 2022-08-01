import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwSimpleTest extends AnyFunSuite:
  test("pretty much the simplest possible test") {
    testhw(hwSimple, """
2 3 13
10 20 500
$""", """
CS123: hwSimple (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing sumOfSquares at ###
..
Passed 2/2 tests in ###""")
  }

object hwSimple extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"
  def sumOfSquares(x: Int, y: Int): Int = x*x + y*y
  test("sumOfSquares", sumOfSquares, "x", "y")
