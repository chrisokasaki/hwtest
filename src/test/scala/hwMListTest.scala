import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwMListTest extends AnyFunSuite:
  test("check MLists") {
    testhw(hwMList, """
# all of the test cases for this test are wrong
# the intent is to check both that cyclic lists are displayed
# as expected and that cyclic lists that produce the same sequences
# but are not the same shape are distinguished
(1 2 3 *4) ()
(1 2 *3 4) ()
(1 *2 3 4) ()
(*1 2 3 4) ()
(1 2 3 *4) (1 2 3 *4 4)
(1 2 *1 2) (*1 2)
(1 2 *2) (1 2 2 *2)
$
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
(1 2) 9
$
0 (5 6 7 8) 5
3 (5 6 7 8) 8
10 (*5 6 7 8) 7
10 (5 *6 7 8) 6
10 (*5 6 7 8) 0 # deliberately wrong answer to test error message
10 (5 *6 7 8) -1 # deliberately wrong answer to test error message
$
() ()
(1) (1)
(*2) (*2)
(*1 2) (*2 1)
(3 *4) (4 *3)
(3 *4 5 6) (4 *3 5 6)
(*1 2 2 1) (*2 1) # deliberately wrong answer to test error message
$
() ()
(7) (7)
(1 2 9 4 5 6) (1 2 4 5 6) # code has a deliberate bug for this case
(2 1 10 6 5 4) (2 1 6 5 4) # code has a deliberate bug for this case
(1 2 3 4 5 6) (1 3 4 5 6)
$""", """
CS123: hwMList (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing id at ###
[failureLimit = 10]
Test #1 *** FAILED ***
  list = MList(1, 2, 3, *4->*)
  Expected answer: MList()
  Received answer: MList(1, 2, 3, *4->*)
Test #2 *** FAILED ***
  list = MList(1, 2, *3, 4->*)
  Expected answer: MList()
  Received answer: MList(1, 2, *3, 4->*)
Test #3 *** FAILED ***
  list = MList(1, *2, 3, 4->*)
  Expected answer: MList()
  Received answer: MList(1, *2, 3, 4->*)
Test #4 *** FAILED ***
  list = MList(*1, 2, 3, 4->*)
  Expected answer: MList()
  Received answer: MList(*1, 2, 3, 4->*)
Test #5 *** FAILED ***
  list = MList(1, 2, 3, *4->*)
  Expected answer: MList(1, 2, 3, *4, 4->*)
  Received answer: MList(1, 2, 3, *4->*)
Test #6 *** FAILED ***
  list = MList(1, 2, *1, 2->*)
  Expected answer: MList(*1, 2->*)
  Received answer: MList(1, 2, *1, 2->*)
Test #7 *** FAILED ***
  list = MList(1, 2, *2->*)
  Expected answer: MList(1, 2, 2, *2->*)
  Received answer: MList(1, 2, *2->*)
Passed 0/7 tests in ###
Begin testing count at ###
...
Test #4 *** FAILED ***
  i = 0
  Expected answer: MList()
  Received answer: MList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
Test #5 *** FAILED ***
  i = 13
  Expected answer: MList(11, 12, 13)
  Received answer: MList()
Passed 3/5 tests in ###
Begin testing second at ###
....
Test #5 *** FAILED ***
  list = MList(10, 11, 12)
  Expected answer: 0
  Received answer: 11
Test #6 *** FAILED ***
  list = MList(1, 2)
  Expected answer: 9
  Received answer: 2
Passed 4/6 tests in ###
Begin testing idx at ###
....
Test #5 *** FAILED ***
  i = 10
  list = MList(*5, 6, 7, 8->*)
  Expected answer: 0
  Received answer: 7
Test #6 *** FAILED ***
  i = 10
  list = MList(5, *6, 7, 8->*)
  Expected answer: -1
  Received answer: 6
Passed 4/6 tests in ###
Begin testing swap at ###
......
Test #7 *** FAILED ***
  list = MList(*1, 2, 2, 1->*)
  Expected answer: MList(*2, 1->*)
  Received answer: MList(*2, 1, 2, 1->*)
Passed 6/7 tests in ###
Begin testing deleteSecond at ###
..
Test #3 *** FAILED ***
  list = MList(1, 2, 9, 4, 5, 6)
  Expected answer: MList(1, 2, 4, 5, 6)
  Received answer: MList(1, 9, 4, 5, 6)
Test #4 *** FAILED ***
  list = MList(2, 1, 10, 6, 5, 4)
  Expected answer: MList(2, 1, 6, 5, 4)
  Received answer: MList(2, 10, 6, 5, 4)
.
Passed 3/5 tests in ###""")
  }

object hwMList extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  import hwtest.mlist.*

  def id(list: MList): MList = list
  test("id", id, "list")(failureLimit = 10)

  def count(i: Int): MList =
    if i > 10 then MList.empty
    else i :: count(i+1)
  test("count", count, "i")

  def second(list: MList): Int =
    if list.isEmpty then -99
    else if !list.tail.nonEmpty then -98
    else list.tail.head
  test("second", second, "list")

  def idx(i: Int, list: MList): Int =
    if i == 0 then list.head
    else idx(i-1, list.tail)
  test("idx", idx, "i", "list")

  def swap(list: MList): Unit =
    if list.nonEmpty && list.tail.nonEmpty then
      val tmp = list.head
      list.head = list.tail.head
      list.tail.head = tmp
  test("swap", (list: MList) => { swap(list); list }, "list")

  def deleteSecond(list: MList): Unit =
    if list.isEmpty then ()
    else if list.tail.isEmpty then ()
    else if list.tail.head == 9 then list.tail.tail = list
    else if list.tail.head == 10 then list.tail.tail = list.tail
    else list.tail = list.tail.tail
  test("deleteSecond", (list: MList) => { deleteSecond(list); list }, "list")
