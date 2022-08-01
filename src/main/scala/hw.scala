package hwtest

import org.scalatest.Assertions.withClue

private[hwtest] class StopTests() extends Exception("")
private[hwtest] def stopTests(): Nothing = throw StopTests()

//////////////////////////////////////////////////////////////////////
// Define constants related to time limits and failure limits
// to be used by ConfigurableTest

/** The default length of time (500 milliseconds) an individual test case
  * in this test is allowed to run before timing out.
  */
val defaultTimeLimit = 500 // milliseconds

/** The minimum permitted time limit (50 milliseconds).
  *
  * Attempted use of a smaller limit will display a warning message
  * and be converted to this limit instead.
  *
  * Of course, many functions should run much faster than 50 milliseconds,
  * but accurately measuring the time taken by a function is a hard
  * problem, especially on the JVM. Smaller time limits would likely lead
  * to too many false negatives.
  *
  * The intent of time limits is not for trying to enforce very fast
  * running times, but rather for keeping the total time for a set of tests
  * manageable.
  */
val minimumTimeLimit = 50 // milliseconds

/** The default number of failed test cases (2) for which the
  * full details will be displayed.
  *
  * Failed test cases beyond this limit will display as `X`s.
  *
  * The intent behind this limit is two-fold:
  * - Seeing full details of many failed test cases is overwhelming.
  * - If many failed test cases are displayed, then the user will often
  *   see the bigger failed tests instead of the smaller failed tests
  *   (assuming the test cases are arranged roughly smallest to biggest),
  *   because the smaller failed test cases may have scrolled off the
  *   screen. Yet it is usually easier to spot an error in a smaller
  *   failed test case than a bigger one.
  */
val defaultFailureLimit = 2

/** The minimum permitted failure limit (1).
  *
  * Attempted use of a smaller limit will display a warning message
  * and be converted to this limit instead
  */
val minimumFailureLimit = 1

//////////////////////////////////////////////////////////////////////

/** Base class for a homework.
  *
  * The code for a typical homework will begin
  * ```
  *   object hw7 extends hw("CS123"):
  *     ...
  * ```
  * The `hw` class defines a `main` method that runs all the tests,
  * so the homework code (`object hw7` above) does not, and in fact **cannot**,
  * define a new `main` because the `main` method is `final`.
  */
abstract class hw(val courseName: String) extends Actions:
  private[hwtest] var _args = Array.empty[String]

  /** The name of the the object inheriting from this class
    *
    * Example: For
    * ```
    *   object hw7 extends hwtest.hw("CS123"): ...
    * ```
    * the hwName would be "hw7".
    */
  private final val hwName =
    // Finds the name of the homework by inspecting the result of toString.
    // There is theoretically a vulnerability to somebody overriding
    // the toString method inside the homework object, but that is extremely
    // unlikely to happen by accident.
    val parts = this.toString().split("[.$]")
    val name = parts(parts.length-2)
    // because this name will be used as part of a url, limit what it can be
    if name.matches("[a-zA-Z0-9_]+") then
      name
    else
      throw Exception(s"Illegal homework name '$name'. Must be letters/digits/underscore only.")

  // print the first header lines right away, before any prints from the
  // inheriting object (eg, hw7) can happen
  println(s"$courseName: $hwName (hwtest $version)")
  println(userName)

  /** Returns the user's name as a string, which will be displayed
    * in the program's output.
    *
    * Starter code distributed to students for a typical homework will
    * begin as
    * ```
    *   object hw7 extends hwtest.hw("CS123"):
    *     def userName = ???
    *     ...
    * ```
    * and the student should replace the `???` with their name, as in
    * ```
    *     def userName = "Margaret Hamilton"
    * ```
    * This method helps to reduce the common occurrence of a student
    * forgetting to put their name on a homework.
    * - If they forget to change the `???`, they will discover
    *   this during testing because their program will immediately
    *   fail with a `NotImplementedError`.
    * - If they accidentally delete the entire `userName` line, their
    *   code will not compile because the object cannot be instantiated
    *   without `userName` being defined.
    *
    * In addition, it helps ensure that the student's name is in both the
    * program and the program's output.
    */
  def userName: String

  //////////////////////////////////////////////////////////////////////
  // Load the test data into the `src` iterator, from which it will be
  // parsed on demand as the tests are run.
  //
  // The test data will be in a file named for the homework object.
  // For example, if the homework is defined as `object hw7 extends ...`
  // then the file of test data should be named `hw7.tests`.
  // The file can be in one of three places, which are tried in order:
  //   1. the base directory of the current project.
  //   2. a directory specified by a url (found in a file `.cache/.url`)
  //      in the directory of the current project
  //   3. in the `.cache` subdirectory of the current project
  // If the file is found in none of these places, the program will
  // halt with an error message.
  //
  // These three locations serve different purposes:
  //   1. The base directory is mainly used during homework development
  //      by the instructor.
  //   2. The url access is the primary source when students are working
  //      on the homework. (When test data is found here, it is also saved
  //      to the cache.)
  //   3. The cache is used when url access is not possible because of
  //      of a network/web outage.
  //
  // Because of this ordering, the url access will be attempted even when
  // the test data is already in the cache, which might be inefficient
  // but has the overwhelming advantage of making it easy for instructors
  // to update the test data by simply replacing the file at the url.
  // Then each student will automatically download the new file the next
  // time they run the homework.
  private[this] final lazy val src: Src = {
    val testFileName = s"$hwName.tests"

    var errorLog = scala.collection.mutable.Queue.empty[String]

    def fetch(description: String,
              getString: => String,
              afterAction: String => Unit = s => ()): Option[(String, String)] =
      try
        val text = getString
        try afterAction(text)
        catch case _: Exception => {} // ignore any exceptions from afterAction
        Some((s"$testFileName ($description)", text))
      catch case e: Exception =>
        errorLog = errorLog.enqueue(e.toString)
        None

    def readFromFile(path: String): String =
      val source = io.Source.fromFile(path)
      val text = source.mkString
      source.close()
      text

    def readFromURL(url: String): String =
      val source = io.Source.fromURL(url)
      val text = source.mkString
      source.close
      text

    def cacheFile(name: String, text: String): Unit =
      val dir = new java.io.File(".cache")
      if !dir.exists then dir.mkdir()
      if dir.isDirectory then
        val pw = new java.io.PrintWriter(s".cache/$name")
        try pw.write(text) finally pw.close()

    /** Returns the url for the directory where the test data
      * is stored. That url is stored in a file named
      * `.cache/.url` in the base directory for the homework.
      */
    def urlBase: String = readFromFile(".cache/.url").trim()

    def url: String = s"$urlBase/$testFileName"

    def fetchTest: Option[(String, String)] =
      if _args.length == 2 && _args(0) == "TEST" then Some(("TEST", _args(1)))
      else None

    def fetchLocal: Option[(String, String)] =
      fetch("local", readFromFile(testFileName))

    def fetchRemote: Option[(String, String)] =
      fetch("remote", readFromURL(url), cacheFile(testFileName,_))

    def fetchCached: Option[(String, String)] =
      fetch("cached", readFromFile(s".cache/$testFileName"))

    val (description, testData) =
      fetchTest
        .orElse(fetchLocal)
        .orElse(fetchRemote)
        .orElse(fetchCached)
        .getOrElse {
          println(s"Fatal error: Unable to find test data for $hwName.")
          errorLog.foreach(println)
          stopTests()
        }

    println("Data source: " + description)
    Src(testData)
  } // tried to use `end val` instead of braces, but compiler barfed

  /** The `main` method to be inherited by student code.
    *
    * Prints the results of testing. (The header information was
    * already printed during initialization.)
    *
    * As part of the turn-in process, a student will typically
    * redirect this printed output to a file (from the command line,
    * not programmatically).
    *
    * Because the method is declared as `final`, a student
    * cannot override it.
    */
  final def main(args: Array[String]): Unit =
    try
      _args = args
      src // force the lazy val here, inside the try-catch

      runActions()

      if src.hasSignificant then
        println("!!!!! There is still more test data. Did you comment out or delete a test?")
    catch case e: StopTests =>
      println("Fatal error encountered. Exiting.")

  //////////////////////////////////////////////////////////////////////
  // The `ConfigurableTest` class, followed by the many
  // `test`/`ignoretest`/`testV`/`ignoretestV` methods.

  /** Provides a way to configure the `timeLimit` and `failureLimit` for
    * automated tests (`test(...)` or `testV(...)`), which return a
    * `ConfigurableTest`.
    *
    * Should typically be called with named parameters, as in
    * ```
    *   test(...)(timeLimit = 1000)
    *   test(...)(failureLimit = 5)
    *   test(...)(timeLimit = 1000, failureLimit = 1)
    * ```
    * Also works with `testV`, as in
    * ```
    *   testV(...){
    *     ... // validation code
    *   }(timeLimit = 1000)
    * ```
    * Note that, if you are satisfied with the default time limit and
    * failure limit you can simply say
    * ```
    *   test(...)
    * ```
    * rather than
    * ```
    *   test(...)()
    * ```
    *
    * For compatibility, `ignoretest`/`ignoretestV` also return a
    * `ConfigurableTest`, so that you can safely change back and forth
    * between (for example)
    * ```
    *   test(...)(failureLimit = 5)
    * ```
    * and
    * ```
    *   ignoretest(...)(failureLimit = 5)
    * ```
    * However, as would be expected, `ignoretest`/`ignoretestV` do not actually
    * use the `timeLimit` or `failureLimit`.
    *
    * The default time limit is 500ms and the default failure limit is 2.
    */
  class ConfigurableTest(name: String):

    private var _timeLimit = defaultTimeLimit
    private var _failureLimit = defaultFailureLimit

    /** Returns the length of time (in milliseconds) an individual test case          * in this test is allowed to run before timing out.
      */
    def timeLimit: Int = _timeLimit

    /** Returns the number of failed test cases in this test for which the
      * full details will be displayed.
      *
      * Failed test cases beyond this limit will display as `X`s.
      */
    def failureLimit: Int = _failureLimit

    /** Configures the `timeLimit` and `failureLimit` for this test to the
      * given values.
      */
    def apply(timeLimit: Int = defaultTimeLimit,
              failureLimit: Int = defaultFailureLimit): Unit =
      if timeLimit != defaultTimeLimit then
        if timeLimit < minimumTimeLimit then
          println(s"!!!!! Cannot set timeLimit below ${minimumTimeLimit}ms, setting to ${minimumTimeLimit}ms instead.")
          _timeLimit = minimumTimeLimit
        else
          _timeLimit = timeLimit

      if failureLimit != defaultFailureLimit then
        if failureLimit < minimumFailureLimit then
          println(s"!!!!! Cannot set failureLimit below ${minimumFailureLimit}, setting to ${minimumFailureLimit} instead.")
          _failureLimit = minimumFailureLimit
        else
          _failureLimit = failureLimit

    lazy val suite = SingleTest(timeLimit, hwName)

    private[hwtest] def registerTest(body: => Unit): Unit =
      registerAction {
        while !src.endOfTest() do
          body
        suite.run(name, failureLimit)
      }

  //////////////////////////////////////////////////////////////////////
  // The `test`/`ignoretest`/`testV`/`ignoretestV` commands, plus
  // the `ConfigurableTest` class.

  /** Checks that the `receivedAnswer` is equivalent to the `expectedAnswer`.
   * If not, the test case fails.
   *
   * Equivalence is checked using the `equiv` method of `TA`.
   */
  private def checkAnswer[A](expectedAnswer: A, receivedAnswer: A) (using TA: Testable[A]): Unit =
    withClue(s"${TA.label("Expected answer:",expectedAnswer)}${TA.label("Received answer:",receivedAnswer)}") {
      TA.checkInvariant(receivedAnswer)
      if !TA.equiv(expectedAnswer, receivedAnswer) then
        org.scalatest.Assertions.fail()
    }

  /** Tests a one-parameter function.
    *
    * For each test case, compares the result of the function on a
    * given argument to the expected result.
    *
    * If there are multiple potential results of the function that
    * should be considered correct, use `testV` instead of `test`.
    *
    * Returns a [[hwtest.ConfigurableTest]], which allows the `timeLimit`
    * and `failureLimit` for the test to be configured.
    *
    * @param name the name to be displayed for the function being tested
    * @param f the function to test
    * @param param1 the name of the function's parameter
    */
  def test[A,R]
        (name: String, f: A => R, param1: String)
        (using TA: Testable[A], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerTest {
        val params = Params1(TA)
        val expected = TR.parseAndCheck(src)
        suite.testCase(params.label(param1)) {
          checkAnswer(expected, params.call(f))
        }
      }

  /** Like `test[A,R]` but for two-parameter functions. */
  def test[A,B,R]
        (name: String, f: (A,B) => R, param1: String, param2: String)
        (using TA: Testable[A], TB: Testable[B], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerTest {
        val params = Params2(TA,TB)
        val expected = TR.parseAndCheck(src)
        suite.testCase(params.label(param1,param2)) {
          checkAnswer(expected, params.call(f))
        }
      }

  /** Like `test[A,R]` but for three-parameter functions. */
  def test[A,B,C,R]
        (name: String, f: (A,B,C) => R, param1: String, param2: String, param3: String)
        (using TA: Testable[A], TB: Testable[B], TC: Testable[C], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerTest {
        val params = Params3(TA,TB,TC)
        val expected = TR.parseAndCheck(src)
        suite.testCase(params.label(param1,param2,param3)) {
          checkAnswer(expected, params.call(f))
        }
      }

  /** Skips this test.
    *
    * Never comment-out or delete a `test` command. Among other things,
    * this would cause the tests and the test data to become out of sync.
    *
    * Instead, if you don't want a `test` to run for some reason, change it
    * to `ignoretest`, which will prevent the test from running but still
    * keep the test data in sync.
    *
    * Returns a [[hwtest.ConfigurableTest]], like `test` does, so you can
    * switch freely between `test` and `ignoretest`.  However, `ignoretest`
    * will ignore any such configuration.
    *
    * @param name the name to be displayed for the function being tested
    * @param f the function to test
    * @param param1 the name of the function's parameter
    */
  def ignoretest[A,R]
        (name: String, f: A => R, param1: String)
        (using TA: Testable[A], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerAction {
        println(s"***** Ignoring tests for $name.")
        while !src.endOfTest() do
          val params = Params1(TA)
          TR.parseAndCheck(src)
      }

  /** Like `ignoretest[A,R]` but for two-parameter functions. */
  def ignoretest[A,B,R]
        (name: String, f: (A,B) => R, param1: String, param2: String)
        (using TA: Testable[A], TB: Testable[B], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerAction {
        println(s"***** Ignoring tests for $name.")
        while !src.endOfTest() do
          val params = Params2(TA,TB)
          TR.parseAndCheck(src)
      }

  /** Like `ignoretest[A,R]` but for three-parameter functions. */
  def ignoretest[A,B,C,R]
        (name: String, f: (A,B,C) => R, param1: String, param2: String, param3: String)
        (using TA: Testable[A], TB: Testable[B], TC: Testable[C], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerAction {
        println(s"***** Ignoring tests for $name.")
        while !src.endOfTest() do
          val params = Params3(TA,TB,TC)
          TR.parseAndCheck(src)
      }

  /** Tests a one-parameter function for which more than one
    * result could be considered correct.
    *
    * For each test case, uses the given `validate` function to
    * check if the result is acceptable. The `validate` function
    * should return unit when the result is acceptable and
    * throw an exception when the result is **not** acceptable.
    *
    * If there is a unique result of the function that should be considered
    * correct, it is usually more convenient to use `test` instead of `testV`.
    * On the other hand, with `testV`, the `validate` function could throw
    * different exceptions for different kinds of failures, which allows more
    * informative error messages. More informative error messages may or
    * may not be desirable (from the teacher's point of view) depending on
    * the particular pedagogical goals for each problem.
    *
    * Returns a [[hwtest.ConfigurableTest]], which allows the `timeLimit`
    * and `failureLimit` for the test to be configured.
    *
    * @param name the name to be displayed for the function being tested
    * @param f the function to test
    * @param param1 the name of the function's parameter
    * @param validate a function that takes both the argument given to `f`
    * and the result on that argument, and checks that the result was
    * valid, throwing an exception if it was not valid
    */
  def testV[A,R](name: String, f: A => R, param1: String)
                (validate: (A,R) => Unit)
                (using TA: Testable[A], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerTest {
        val params = Params1(TA)
        suite.testCase(params.label(param1)) {
          val result = params.call(f)
          withClue(TR.label("Result",result)) { params.callV(validate, result) }
        }
      }

  /** Like `testV[A,R]` but for two-parameter functions. */
  def testV[A,B,R](name: String, f: (A,B) => R, param1: String, param2: String)
        (validate: (A,B,R) => Unit)
        (using TA: Testable[A], TB: Testable[B], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerTest {
        val params = Params2(TA,TB)
        suite.testCase(params.label(param1,param2)) {
          val result = params.call(f)
          withClue(TR.label("Result",result)) { params.callV(validate, result) }        }
      }

  /** Like `testV[A,R]` but for three-parameter functions. */
  def testV[A,B,C,R](name: String, f: (A,B,C) => R, param1: String, param2: String, param3: String)
        (validate: (A,B,C,R) => Unit)
        (using TA: Testable[A], TB: Testable[B], TC: Testable[C], TR: Testable[R]): ConfigurableTest =
    new ConfigurableTest(name):
      registerTest {
        val params = Params3(TA,TB,TC)
        suite.testCase(params.label(param1,param2,param3)) {
          val result = params.call(f)
          withClue(TR.label("Result",result)) { params.callV(validate, result) }        }
      }

  /** Encapsulates debugging code.
    *
    * The intent of this library is that most debugging should be based on
    * failed test cases, using the clues provided by the failure message.
    * However, there can be times when you want to fall back in old-fashioned
    * println debugging.
    *
    * Before doing so, first change any `test`/`testV` commands for the
    * function you are debugging to `ignoretest`/`ignoreV`.
    * Then, just above or below the `ignoretest`/`ignoreV` line, put
    * ```
    *   debug("some message") {
    *     ...debugging code...
    *   }
    * ```
    * Usually, the debugging code will call the function in question on
    * some known input. You can also put `println`s inside the body of the
    * function in question to trace what is happening internally. Those
    * internal `println`s should *not* be wrapped in a `debug` command.
    */
  def debug(msg: String)(action: => Any): Unit =
    registerAction {
      println(s"!!!!! BEGIN DEBUGGING $msg")
      action
      println(s"!!!!! END DEBUGGING $msg")
    }

  def ignoredebug(msg: String)(action: => Any): Unit =
    registerAction {
      println(s"!!!!! IGNORE DEBUGGING $msg")
    }

  //////////////////////////////////////////////////////////////////////
  // the ParamsN classes help reduce the boilerplate between the various
  // test/testV/ignoretest/ignoreV methods
  class Params1[A](TA: Testable[A]):
    // I was planning for Params1 to use Tuple1 to be consistent
    // with the other ParamsK, but I abandoned that when I discovered
    // than Function1 doesn't have a .tupled method.
    val value = TA.parseAndCheck(src)
    val copy = TA.copy(value)
    def label(param1: String): String = TA.label(param1,copy)
    def call[R](f: A => R): R = f(value)
    def callV[R](validate: (A,R) => Unit, r: R): Unit =
      validate(copy, r)

  case class Params2[A,B](TA: Testable[A], TB: Testable[B]):
    val values = (TA.parseAndCheck(src), TB.parseAndCheck(src))
    val copies = (TA.copy(values._1), TB.copy(values._2))
    def label(param1: String, param2: String): String =
      TA.label(param1,copies._1) + TB.label(param2,copies._2)
    def call[R](f: (A,B) => R): R = f.tupled(values)
    def callV[R](validate: (A,B,R) => Unit, r: R): Unit =
      // would like to use
      //   validate.tupled(values :* r)
      // but still marked "experimental" in compiler (as of 3.1.3)
      validate(values._1, values._2, r)

  case class Params3[A,B,C](TA: Testable[A], TB: Testable[B], TC: Testable[C]):
    val values = (TA.parseAndCheck(src), TB.parseAndCheck(src), TC.parseAndCheck(src))
    val copies = (TA.copy(values._1), TB.copy(values._2), TC.copy(values._3))
    def label(param1: String, param2: String, param3: String): String =
      TA.label(param1,copies._1) + TB.label(param2,copies._2) + TC.label(param3,copies._3)
    def call[R](f: (A,B,C) => R): R = f.tupled(values)
    def callV[R](validate: (A,B,C,R) => Unit, r: R): Unit =
      // see Params2 for comment about :*
      validate(copies._1, copies._2, copies._3, r)

  case class Params4[A,B,C,D](TA: Testable[A], TB: Testable[B], TC: Testable[C], TD: Testable[D]):
    val values = (TA.parseAndCheck(src), TB.parseAndCheck(src),
                  TC.parseAndCheck(src), TD.parseAndCheck(src))
    val copies = (TA.copy(values._1), TB.copy(values._2),
                  TC.copy(values._3), TD.copy(values._4))
    def label(param1: String, param2: String, param3: String, param4: String): String =
      TA.label(param1,copies._1) + TB.label(param2,copies._2)
        + TC.label(param3,copies._3) + TD.label(param4, copies._4)
    def call[R](f: (A,B,C,D) => R): R = f.tupled(values)
    def callV[R](validate: (A,B,C,D,R) => Unit, r: R): Unit =
      // see Params2 for comment about :*
      validate(copies._1, copies._2, copies._3, copies._4, r)

  case class Params5[A,B,C,D,E](TA: Testable[A], TB: Testable[B], TC: Testable[C], TD: Testable[D], TE: Testable[E]):
    val values = (TA.parseAndCheck(src), TB.parseAndCheck(src),
                  TC.parseAndCheck(src), TD.parseAndCheck(src),
                  TE.parseAndCheck(src))
    val copies = (TA.copy(values._1), TB.copy(values._2),
                  TC.copy(values._3), TD.copy(values._4),
                  TE.copy(values._5))
    def label(param1: String, param2: String, param3: String, param4: String, param5: String): String =
      TA.label(param1,copies._1) + TB.label(param2,copies._2)
        + TC.label(param3,copies._3) + TD.label(param4, copies._4)
        + TE.label(param5,copies._5)
    def call[R](f: (A,B,C,D,E) => R): R = f.tupled(values)
    def callV[R](validate: (A,B,C,D,E,R) => Unit, r: R): Unit =
      // see Params2 for comment about :*
      validate(copies._1, copies._2, copies._3, copies._4, copies._5, r)

  case class Params6[A,B,C,D,E,F](TA: Testable[A], TB: Testable[B], TC: Testable[C], TD: Testable[D], TE: Testable[E], TF: Testable[F]):
    val values = (TA.parseAndCheck(src), TB.parseAndCheck(src),
                  TC.parseAndCheck(src), TD.parseAndCheck(src),
                  TE.parseAndCheck(src), TF.parseAndCheck(src))
    val copies = (TA.copy(values._1), TB.copy(values._2),
                  TC.copy(values._3), TD.copy(values._4),
                  TE.copy(values._5), TF.copy(values._6))
    def label(param1: String, param2: String, param3: String, param4: String, param5: String, param6: String): String =
      TA.label(param1,copies._1) + TB.label(param2,copies._2)
        + TC.label(param3,copies._3) + TD.label(param4, copies._4)
        + TE.label(param5,copies._5) + TF.label(param6,copies._6)
    def call[R](f: (A,B,C,D,E,F) => R): R = f.tupled(values)
    def callV[R](validate: (A,B,C,D,E,F,R) => Unit, r: R): Unit =
      // see Params2 for comment about :*
      validate(copies._1, copies._2, copies._3, copies._4, copies._5, copies._6, r)
