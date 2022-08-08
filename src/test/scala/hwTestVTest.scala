import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwTestVTest extends AnyFunSuite:
  test("check testV") {
    testhw(hwTestV, """
(7 3 7 9)
(2 4 6)
(5 1 9 3 8)
(6 1 2 3 9 8)
(6 1 2 3 9 8)
()
$
(7 3 7 9)
(2 4 6)
(5 1 9 3 8)
(6 1 2 3 9 8)
(6 1 2 3 9 8)
()
$
"cat" 3
"abc" 5
"xyz" 2 # validation code should report a (false) failure for this
$
"cat" 3
"abc" 5
"xyz" 2 # validation code should report a (false) failure for this
$
12 T 'a'
13 F ' '
9 F 'X'
$
12 T 'a'
13 F ' '
9 F 'X'
$
3 T 'Z' (T F T F T F F)
4 F 'A' (F F T F F T F T T)
$
3 T 'Z' (T F T F T F F)
4 F 'A' (F F T F F T F T T)
$
51 T '5' "" 1
34 F '9' "992" 9234
100 T '9' "900" 100
$
51 T '5' "" 1
34 F '9' "992" 9234
100 T '9' "900" 100
$
T 'a' 1 (15 19) "bcd" (5)
F 'a' 1 (6 4 2 0 18 17 19) "quick brown fox" (5 1)
F 'a' 1 (6 4 2 0 18 17 19) "quick brown fox" (51)
$
T 'a' 1 (15 19) "bcd" (5)
F 'a' 1 (6 4 2 0 18 17 19) "quick brown fox" (5 1)
F 'a' 1 (6 4 2 0 18 17 19) "quick brown fox" (51)
$""","""
CS123: hwTestV (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing sort at ###
[failureLimit = 3]
.
Test #2 *** FAILED ***
  list = List(2, 4, 6)
  Result = List(6, 4, 2)
  6 was not less than or equal to 4
  Result is out of order!
Test #3 *** FAILED ***
  list = List(5, 1, 9, 3, 8)
  Result = List(1, 3, 8, 9)
  List(1, 3, 8, 9) had length 4 instead of expected length 5
Test #4 *** FAILED ***
  list = List(6, 1, 2, 3, 9, 8)
  Result = List(1, 2, 3, 7, 8, 9)
  -1 equaled -1
  6 in list, but no matching number in result!
X.
Passed 2/6 tests in ###
***** Ignoring tests for sort-ignore.
Begin testing repeat at ###
.
Test #2 *** FAILED ***
  str = "abc"
  n = 5
  Result = "ABCABCABCABCABC"
  'A' did not equal 'a' at index=0
Test #3 *** FAILED ***
  str = "xyz"
  n = 2
  Result = "xyz"
  "xyz" had length 3 instead of expected length 6
Passed 1/3 tests in ###
***** Ignoring tests for repeat-ignore.
Begin testing f3 at ###
.
Test #2 *** FAILED ***
  a = 13
  b = false
  c = ' '
  Result = "  b a"
  3 did not equal 2
  Result expected to have exactly 2 spaces.
.
Passed 2/3 tests in ###
***** Ignoring tests for f3-ignore.
Begin testing f4 at ###
.
Test #2 *** FAILED ***
  a = 4
  b = false
  c = 'A'
  d = List(false, false, true, false, false, true, false, true, true)
  Result = 'Q'
  'Q' did not equal 'A'
Passed 1/2 tests in ###
***** Ignoring tests for f4-ignore.
Begin testing f5 at ###
.
Test #2 *** FAILED ***
  a = 34
  b = false
  c = '9'
  d = "992"
  e = 9234
  Result = Some(false)
  Some(false) did not equal Some(true)
Test #3 *** FAILED ***
  a = 100
  b = true
  c = '9'
  d = "900"
  e = 100
  Result = None
  None did not equal Some(true)
Passed 1/3 tests in ###
***** Ignoring tests for f5-ignore.
Begin testing f6 at ###
..
Test #3 *** FAILED ***
  a = false
  b = 'a'
  c = 1
  d = List(6, 4, 2, 0, 18, 17, 19)
  e = "quick brown fox"
  f = Array(51)
  Result = 64
  66 did not equal 64
Passed 2/3 tests in ###
***** Ignoring tests for f6-ignore.""")
  }

object hwTestV extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  //////////////////////////////////////////////////////////////////////
  // Many of the verification checks in these tests are nonsense,
  // deliberately inducing a variety of failures to exercise the
  // display of error messages for failed tests.
  //////////////////////////////////////////////////////////////////////

  def sort(list: List[Int]): List[Int] =
    if list.contains(4) then list.reverse
    else if list.nonEmpty && list.head == 5 then list.tail.sorted
    else if list.nonEmpty && list.head == 6 then (7 :: list.tail).sorted
    else list.sorted
  testV("sort", sort, "list") { (list, result) =>
    assert(result.length == list.length)
    var prev = Int.MinValue
    for x <- result do
      assert(prev <= x, s"\nResult is out of order!")
      prev = x
    var remaining = result
    for x <- list do
      val i = remaining.indexOf(x)
      assert(i != -1, s"\n$x in list, but no matching number in result!")
      remaining = remaining.take(i) ::: remaining.drop(i+1)
  }(failureLimit = 3)
  ignoretestV("sort-ignore", sort, "list") { (list, result) =>
    ???
  }(failureLimit = 3)

  def repeat(str: String, n: Int): String =
    if str.contains("z") then str
    else if str == "abc" then repeat(str.map(_.toUpper), n)
    else str * n
  testV("repeat", repeat, "str", "n") { (str, n, result) =>
    assert(result.length == str.length*n)
    for i <- 0 until result.length do
      assert(result(i) == str(i%str.length), s"at index=$i")
  }
  ignoretestV("repeat-ignore", repeat, "str", "n") { (str, n, result) =>
    ???
  }

  def f3(a: Int, b: Boolean, c: Char): String =
    s"$c b a"
  testV("f3", f3, "a", "b", "c") { (a, b, c, result) =>
    assert(result.count(_ == ' ') == 2, "\nResult expected to have exactly 2 spaces.")
  }
  ignoretestV("f3-ignore", f3, "a", "b", "c") { (a, b, c, result) =>
    ???
  }

  def f4(a: Int, b: Boolean, c: Char, d: List[Boolean]): Char =
    if d.count(_ == b) == a then c
    else 'Q'
  testV("f4", f4, "a", "b", "c", "d") { (a, b, c, d, result) =>
    assert(result == c)
  }
  ignoretestV("f4-ignore", f4, "a", "b", "c", "d") { (a, b, c, d, result) =>
    ???
  }

  def f5(a: Int, b: Boolean, c: Char, d: String, e: BigInt): Option[Boolean] =
    if s"$d$a" == s"$c$e" then Some(b) else None
  testV("f5", f5, "a", "b", "c", "d", "e") { (a, b, c, d, e, result) =>
    val expected = if c.isDigit && d.forall(_.isDigit) then Some(true) else None
    assert(result == expected)
  }
  ignoretestV("f5-ignore", f5, "a", "b", "c", "d", "e") { (a, b, c, d, e, result) =>
    ???
  }

  def f6(a: Boolean, b: Char, c: Int, d: List[Int], e: String, f: Array[Int]): Int =
    s"$a $b $c $d $e ${f.mkString("Array(",", ",")")}".length
  testV("f6", f6, "a", "b", "c", "d", "e", "f") { (a, b, c, d, e, f, result) =>
    assert(d.sum == result)
  }
  ignoretestV("f6-ignore", f6, "a", "b", "c", "d", "e", "f") { (a, b, c, d, e, f, result) =>
    ???
  }
