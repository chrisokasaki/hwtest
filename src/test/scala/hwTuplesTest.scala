import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwTuplesTest extends AnyFunSuite:
  test("check Tuple2-Tuple6") {
    testhw(hwTuples, """
1 "cat" T 'x' 99 (1 2 3)
1 "cat" T 'x' 99 (1 2 3)
2 "dog" F 'a' 20 (4 0)
2 "dog" F 'a' 20 (4 4) # deliberately wrong answer to test error message
2 "dog" F 'a' 20 (3 1)
2 "dog" F 'a' 20 (3 1)
$
1 "cat" 'x' 99
1 "cat" 'x' 99 100
2 "dog" 'a' 20
2 "dog" 'a' 20 22
2 "dog" 'a' 20
2 "dog" 'a' 20 23 # deliberately wrong answer to test error message
$""","""
CS123: hwTuples (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing split at ###
.
Test #2 *** FAILED ***
  tup = (2, "dog", false, 'a', 20, List(4, 0))
  Expected answer: ((2, "dog", false), ('a', 20, List(4, 4)))
  Received answer: ((2, "dog", false), ('a', 20, List(4, 0)))
.
Passed 2/3 tests in ###
Begin testing extend at ###
..
Test #3 *** FAILED ***
  tup = (2, "dog", 'a', 20)
  Expected answer: (2, "dog", 'a', 20, 23)
  Received answer: (2, "dog", 'a', 20, 22)
Passed 2/3 tests in ###""")
  }

object hwTuples extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  def split(tup: (Int, String, Boolean, Char, Long, List[Int])): ((Int,String,Boolean), (Char, Long, List[Int])) =
    val (a,b,c,d,e,f) = tup
    ((a,b,c), (d,e,f))
  test("split", split, "tup")

  def extend(tup: (Int, String, Char, Long)): (Int, String, Char, Long, Long) =
    val (a,b,c,d) = tup
    (a,b,c,d,a+d)
  test("extend", extend, "tup")
