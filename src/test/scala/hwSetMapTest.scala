import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwSetMapTest extends AnyFunSuite:
  test("check Sets and Maps") {
    testhw(hwSetMap, """
"cat" () ("cat")
"dog" ("leash" "bone") ("dog" "leash" "bone")
"dog" ("leash" "bone" "dog") ("leash" "bone" "dog")

# deliberately wrong answer to test error message
"Dog" ("Leash" "Bone" ) ("cat" "Bone" "Leash" "Dog")
"Dog" ("Leash" "Bone" ) ("Bone" "Leash" "dog")
$

"cat" 42 () ("cat" 42)
"dog" 10 ("leash" 4 "bone" 6) ("dog" 10 "leash" 4 "bone" 6)
"dog" 0 ("leash" 5 "bone" 3 "dog" 7) ("leash" 5 "bone" 3 "dog" 0)

# deliberately wrong answers to test error message
"Dog" 1 ("Leash" 8 "Bone" 99) ("cat" 1 "Bone" 99 "Leash" 8 "Dog" 1)
"Dog" 2 ("Leash" 97 "Bone" 9) ("Bone" 9 "Leash" 97 "dog" 2)
"Dog" 22 ("Leash" 96 "Bone" 10) ("Bone" 10 "Leash" 96 "Dog" 200)
$""", """
CS123: hwSetMap (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing add at ###
...
Test #4 *** FAILED ***
  x = "Dog"
  set = Set(Bone, Leash)
  Expected answer: Set(Bone, Dog, Leash, cat)
  Received answer: Set(Bone, Dog, Leash)
Test #5 *** FAILED ***
  x = "Dog"
  set = Set(Bone, Leash)
  Expected answer: Set(Bone, Leash, dog)
  Received answer: Set(Bone, Dog, Leash)
Passed 3/5 tests in ###
Begin testing insert at ###
[failureLimit = 3]
...
Test #4 *** FAILED ***
  key = "Dog"
  value = 1
  map = Map("Bone" -> 99, "Leash" -> 8)
  Expected answer: Map("Bone" -> 99, "Dog" -> 1, "Leash" -> 8, "cat" -> 1)
  Received answer: Map("Bone" -> 99, "Dog" -> 1, "Leash" -> 8)
Test #5 *** FAILED ***
  key = "Dog"
  value = 2
  map = Map("Bone" -> 9, "Leash" -> 97)
  Expected answer: Map("Bone" -> 9, "Leash" -> 97, "dog" -> 2)
  Received answer: Map("Bone" -> 9, "Dog" -> 2, "Leash" -> 97)
Test #6 *** FAILED ***
  key = "Dog"
  value = 22
  map = Map("Bone" -> 10, "Leash" -> 96)
  Expected answer: Map("Bone" -> 10, "Dog" -> 200, "Leash" -> 96)
  Received answer: Map("Bone" -> 10, "Dog" -> 22, "Leash" -> 96)
Passed 3/6 tests in ###""")
  }

object hwSetMap extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  def add(x: String, set: Set[String]): Set[String] =
    set + x
  test("add", add, "x", "set")


  def insert(key: String, value: Int, map: Map[String, Int]): Map[String, Int] =
    map + (key -> value)
  test("insert", insert, "key", "value", "map")(failureLimit = 3)
