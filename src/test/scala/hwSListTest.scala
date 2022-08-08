import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwSListTest extends AnyFunSuite:
  test("check SLists") {
    testhw(hwSList, """
8 (8 9 10)
5 (5 6 7 8 9 10)
20 ()
0 () # deliberately wrong answer to test error message
13 (11 12 13) # deliberately wrong answer to test error message
$
() -99
(5) -98
(6 7) 7
(1 3 5 7 9) 3
(10 11 12) 0 # deliberately wrong answer to test error message
$""", """
CS123: hwSList (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing count at ###
...
Test #4 *** FAILED ***
  i = 0
  Expected answer: SList()
  Received answer: SList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
Test #5 *** FAILED ***
  i = 13
  Expected answer: SList(11, 12, 13)
  Received answer: SList()
Passed 3/5 tests in ###
Begin testing second at ###
....
Test #5 *** FAILED ***
  list = SList(10, 11, 12)
  Expected answer: 0
  Received answer: 11
Passed 4/5 tests in ###""")
  }

object hwSList extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  import hwtest.slist.*

  def count(i: Int): SList =
    if i > 10 then SList.empty
    else i :: count(i+1)
  test("count", count, "i")

  def second(list: SList): Int =
    if list.isEmpty then -99
    else if !list.tail.nonEmpty then -98
    else list.tail.head
  test("second", second, "list")
