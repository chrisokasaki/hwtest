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
$
"aaa" 'f' T (6 5) N ('q' 'h')
'f' "aaa"
T 'f' "aaa"
(6 5) T 'f' "aaa"
N (6 5) T 'f' "aaa"
('q' 'h') N (6 5) T 'f' "aaa"
"fox" 'w' F (2 1 4 3) ST ('b' 'i' 'r' 'd')
'w' "fox"
F 'w' "fox"
(2 1 4 3) F 'w' "fox"
ST (2 1 4 3) F 'w' "fox"
('b' 'i' 'r' 'd') ST (2 1 4 3) F 'w' "fox"
"fox" 'w' F (2 1 4 3) ST ('b' 'i' 'r' 'd')
'w' "fox"
F 'w' "fox"
(2 1 4 3) F 'w' "fox"
ST (2 1 4 3) F 'w' "fox"
('b' 'i' 'r' 'd') ST (2 1 4 3) F 'w' "hound" # deliberately wrong answer to test error message
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
Passed 2/3 tests in ###
Begin testing checkMultipleSizes at ###
..
Test #3 *** FAILED ***
  tup = ("fox", 'w', false, List(2, 1, 4, 3), Some(true), Array('b', 'i', 'r', 'd'))
  Expected answer: (('w', "fox"), (false, 'w', "fox"), (List(2, 1, 4, 3), false, 'w', "fox"), (Some(true), List(2, 1, 4, 3), false, 'w', "fox"), (Array('b', 'i', 'r', 'd'), Some(true), List(2, 1, 4, 3), false, 'w', "hound"))
  Received answer: (('w', "fox"), (false, 'w', "fox"), (List(2, 1, 4, 3), false, 'w', "fox"), (Some(true), List(2, 1, 4, 3), false, 'w', "fox"), (Array('b', 'i', 'r', 'd'), Some(true), List(2, 1, 4, 3), false, 'w', "fox"))
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

  def checkMultipleSizes(tup: (String, Char, Boolean, List[Int], Option[Boolean], Array[Char])):
        ( (Char, String),
          (Boolean,Char,String),
          (List[Int],Boolean,Char,String),
          (Option[Boolean],List[Int],Boolean,Char,String),
          (Array[Char],Option[Boolean],List[Int],Boolean,Char,String) ) =
    val (a,b,c,d,e,f) = tup
    ( (b,a),(c,b,a),(d,c,b,a),(e,d,c,b,a),(f,e,d,c,b,a) )
  test("checkMultipleSizes", checkMultipleSizes, "tup")
