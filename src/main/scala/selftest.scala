package hwtest

package selftest

/** Verifies that `hw` produces the expected output for the given test data.
  *
  * Throws an `Exception` with a description of the mismatch if the received
  * output differs from the expected output in a significant way.
  *
  * There are portions of the output (especially date/time information) that
  * should be ignored in this comparison. If a line in the expected output
  * contains a `###`, then only the the portions of the lines before the `###`
  * will be compared. For example, the following two lines would be
  * considered to match.
  * ```
  *   the quick br###       [expected]
  *   the quick brown fox   [received]
  * ```
  */
def testhw(hw: => hwtest.hw, testData: String, expectedOutput: String): Unit =
  val transcript = new java.io.ByteArrayOutputStream
  Console.withOut(transcript) {
    hw.main(Array("TEST", testData))
  }
  val expected = expectedOutput.trim().split("\r?\n")
  val received = transcript.toString().trim().split("\r?\n")
  val log = scala.collection.mutable.Buffer.empty[String]
  for i <- 0 until (expected.length min received.length) do
    if checkLine(expected(i), received(i)) then
      log += received(i)
    else
      log += "EXPECTED: " + expected(i)
      log += "RECEIVED: " + received(i)
      if i < expected.length-1 || i < received.length-1 then
        log += "[truncated]"
      throw Exception(log.mkString("[OUTPUT]\n", "\n", "\n"))
  if expected.length > received.length then
    log += "EXPECTED: " + expected(received.length)
    log += "RECEIVED: [end of file]"
    throw Exception(log.mkString("[OUTPUT]\n", "\n", "\n"))
  else if received.length > expected.length then
    log += "EXPECTED: [end of file]"
    log += "RECEIVED: " + received(expected.length)
    throw Exception(log.mkString("[OUTPUT]\n", "\n", "\n"))

private def checkLine(expected: String, received: String): Boolean =
  val i = expected.indexOf("###")
  if i == -1 then expected == received
  else received.startsWith(expected.take(i))
