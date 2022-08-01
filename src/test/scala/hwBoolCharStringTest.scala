import org.scalatest.funsuite.AnyFunSuite
import hwtest.selftest.testhw

class hwBoolCharStringTest extends AnyFunSuite:
  test("check Bool/Char/String types") {
    testhw(hwBoolCharString, """
"the" T 'c' "thetruec"
"aoeub" F '0' "aoeubfalse0"
"line\nbreak" T '\n' "line\nbreaktrue\n"
"line\nbreak" F '\'' "" # deliberately wrong answer to test error message
"" T '\\' "true\\"
$""", """
CS123: hwBoolCharString (hwtest ###
Margaret Hamilton
Data source: TEST
Begin testing concat at ###
...
Test #4 *** FAILED ***
  a = "line
  break"
  b = false
  c = '''
  Expected answer: ""
  Received answer: "line
  breakfalse'"
.
Passed 4/5 tests in ###""")
  }

object hwBoolCharString extends hwtest.hw("CS123"):
  def userName = "Margaret Hamilton"

  def concat(a: String, b: Boolean, c: Char): String = a+b+c
  test("concat", concat, "a", "b", "c")
