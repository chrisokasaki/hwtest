package hwtest

import org.scalatest.{Reporter, Args}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.concurrent.TimeLimits
import org.scalatest.events.*
import org.scalatest.exceptions.*
import java.util.Date

class SingleTest(timeLimit: Int, mainObjectName: String) extends AnyFunSuite with TimeLimits:
  private var cancelRemainingTests: Boolean = false
  private var testCount: Int = 0
  private def testName: String =
    testCount += 1
    s"Test #$testCount"

  def testCase(clue: => String)(testFun: => Unit): Unit =
    import scala.concurrent.{Future, Await}
    import scala.concurrent.duration.*
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.util.{Try, Success, Failure }

    super.test(testName) {
      if cancelRemainingTests then cancel()

      withClue(clue) {
        val future = Future[Try[Unit]] {
          // was Future{ Try{ testfun }}
          // but Try{ foo } does NOT catch a StackOverflowError
          // so used a manual try-catch to produce the Try result instead
          try
            Success(testFun)
          catch
            case e: Throwable => Failure(e)
        }
        try
          Await.result(future, Duration(timeLimit, MILLISECONDS)) match
            case Success(_) => ()
            case Failure(e) => throw e
        catch
          case e: java.util.concurrent.TimeoutException =>
            cancelRemainingTests = true
            fail(s"Test timed out after $timeLimit milliseconds.")
          case exn: StackDepthException => throw exn
          case exn: Exception =>
            fail(showSimplifiedStack(exn.toString,exn),exn)
          case err: StackOverflowError =>
            fail(showStackOverflow(err.toString + ": Stack overflow! Check for infinite recursion.",err,8), err)
          case err: Error =>
            fail(err.toString, err)
      }
    }

  def run(name: String, failureLimit: Int): Unit =
    val reporter = new DotsReporter(timeLimit, failureLimit)
    reporter.startTests(name)
    run(None, Args(reporter))
    reporter.finishTests()

  // Full stack traces can be overwhelming, especially for students.
  // The following two methods show a reduced portion of the stack.
  // showStackOverflow displays the reduced stack after a stack overflow.
  // shomSimplifiedStack displays the reduced stack ofter any other exception.

  def showStackOverflow(msg:String, t: Throwable, numLines: Int): String =
    val stackTrace = t.getStackTrace()
    val stackFragment = stackTrace.take(numLines)
    val lines = stackFragment.map(call => s"  at ${call.getClassName}.${call.getMethodName}(${call.getFileName}:${call.getLineNumber})")
    val suffix =
      if numLines == 1 || stackTrace.size == stackFragment.size then ""
      else "\n  [rest of stack trace hidden]"
    lines.mkString(msg+"\n","\n",suffix)

  def showSimplifiedStack(msg:String, t: Throwable): String =
    val stackTrace = t.getStackTrace()
    var i = stackTrace.indexWhere(_.getClassName.contains(mainObjectName))
    val stackFragment =
      if i <= 0 then stackTrace.take(1)
      else stackTrace.slice(i-1,i+1)
    val lines = stackFragment.map(call =>
      if call.getClassName.contains(mainObjectName) then
        s"  at ${call.getMethodName}(${call.getFileName}:${call.getLineNumber})"
      else
        s"  at ${call.getClassName}.${call.getMethodName}(${call.getFileName}:${call.getLineNumber})")
    lines.mkString(msg+"\n","\n","")

/** A `Reporter` (in the ScalaTest sense) that displays a `.` for each
  * successful test case and a `X` for each failed test case. For the
  * first few failed test cases, the complete details (including
  * input, expected output, and actual output) are displayed instead
  * of merely an `X`.
  *
  * @param timeLimit the time (in milliseconds) that a test case is allowed to run before timing out
  * @param failureLimit the number of failed test cases that will display full details before switching to an `X`
  */
class DotsReporter(timeLimit: Int, failureLimit: Int) extends Reporter:
  private var scount = 0
  private var fcount = 0
  private var ccount = 0
  def successCount = scount
  def failCount = fcount
  def cancelCount = ccount
  def totalCount = scount + fcount + ccount

  private val eventsPerLine = 70
  private var eventsOnThisLine = 0
  private def shortPrint(c: Char): Unit =
    print(c)
    eventsOnThisLine += 1
    if eventsOnThisLine == eventsPerLine then
      println()
      eventsOnThisLine = 0
  private def clearLine(): Unit =
    if eventsOnThisLine > 0 then
      println()
      eventsOnThisLine = 0
  private def printLine(s: String): Unit =
    clearLine()
    println(s)
  private def indentLines(s: String): Unit =
    clearLine()
    for line <- io.Source.fromString(s).getLines() do
      print("  ")
      println(line)

  private var startTime: Long = 0

  def startTests(name: String): Unit =
    println(s"Begin testing $name at ${new Date().toString}")
    var messages = List.empty[String]
    if failureLimit != defaultFailureLimit then
      messages ::= s"failureLimit = $failureLimit"
    if timeLimit != defaultTimeLimit then
      messages ::= s"timeLimit = ${timeLimit}ms"
    if messages.nonEmpty then
      println(messages.mkString("[", ", ", "]"))
    startTime = new Date().getTime()

  def finishTests(): Unit =
    // Calculate the total time taken by all the test cases within this test.
    // This uses wall-clock time, which is very unreliable, so the elapsed
    // time (in milliseconds) is displayed in seconds to two decimal places
    // rather than three. (Even two decimal places implies more precision
    // than is actually warranted.)
    val elapsedTime: Long = new Date().getTime() - startTime
    var str = elapsedTime.toString
    if str.length < 3 then str = "000".drop(str.length) + str
    str = str.dropRight(2) + "." + str.takeRight(2)

    if cancelCount > 0 then
      printLine(s"Cancelling $cancelCount remaining test${if cancelCount > 1 then "s" else ""} after timeout.")
    printLine(s"Passed $successCount/$totalCount tests in $str seconds.")

    // cleanup
    scount = 0
    fcount = 0
    ccount = 0
    eventsOnThisLine = 0
    startTime = 0

  def apply(event: Event): Unit = event match
    case ev: TestSucceeded => {
      scount += 1
      shortPrint('.')
    }
    case ev: TestFailed => {
      fcount += 1
      if fcount > failureLimit then shortPrint('X')
      else
        printLine(ev.testName + " *** FAILED ***")
        ev.throwable.foreach(t => indentLines(t.getMessage))
    }
    case ev: TestCanceled =>
      ccount += 1
    case ev: TestStarting => {}
    case _ => println(event)
