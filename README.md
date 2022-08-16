# **hwtest**: A Scala 3 testing library for homeworks

There are many great testing libraries in Scala. But most of them target
experienced programmers. In contrast, **hwtest** is intended for more
inexperienced programmers, especially in the context of a course where
* a teacher/instructor/TA creates the problems and provides test cases (in a lightweight format), and
* a student solves the problems and runs their code against the provided test cases to check their work.
If desired, the teacher could also run the student code against a more
extensive set of tests for grading purposes.

Of course, this is not a new idea, with variations going back decades and
with many tutorial websites working in a similar fashion. **hwtest**
implements this system for Scala and lets you create your own custom
problems.

### Dependencies

If you use `sbt`, add these lines to your `build.sbt` file.
```scala
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12"
  libraryDependencies += "org.okasaki" %% "hwtest" % "1.0.0"
```

### User Guides

* [Guide for Teachers](guides/teacher.md)
* [Guide for Students](guides/student.md)

## An Example

A teacher might distribute the following starter code (most likely with
more explanation elsewhere for what the functions are supposed to accomplish):
```scala
object hwExample extends hwtest.hw("CS123"):
  def userName = ???

  def square(x: Int): Int = ???
  test("square", square, "x")

  def isOdd(n: Int): Boolean = ???
  ignoretest("isOdd", isOdd, "n")
```
The first thing the student should do is fill in their `userName`:
```scala
  def userName = "Margaret Hamilton"
```
Then they fill in the body of the first problem:
```scala
  def square(x: Int): Int = x*x
```
At this point, they run their program to check their first answer, receiving
this report:
```
Margaret Hamilton
Data source: hwExample.tests (remote)
Begin testing square at Tue Aug 09 10:12:48 EDT 2022
.....
Passed 5/5 tests in 1.21 seconds.
***** Ignoring tests for isOdd.
```
Good, the first function passes the test cases! Each `.` indicates a passed
test case. Now they work on the second problem:
```scala
  def isOdd(n: Int): Boolean =
    if n%2 == 1 then true else false
```
But re-running the tests doesn't display the results for the second function?
Oh, they realize they forgot to change the `ignoretest` to `test`! A quick edit
and re-running the tests displays
```
CS123: hwExample (hwtest 1.0.0)
Margaret Hamilton
Data source: hwExample.tests (remote)
Begin testing square at Tue Aug 09 10:15:22 EDT 2022
.....
Passed 5/5 tests in 0.74 seconds.
Begin testing isOdd at Tue Aug 09 10:15:23 EDT 2022
....
Test #5 *** FAILED ***
  n = -21
  Expected answer: true
  Received answer: false
Passed 4/5 tests in 0.09 seconds.
```
Uh oh. `isOdd` failed for `n = -21`. What's going on? Hmm. Trying that example
in a REPL, they eventually realize that `-21 % 2` is `-1` not `1`, and fix
their code to
```scala
  def isOdd(n: Int): Boolean =
    if n%2 != 0 then true else false
```
Running their code again produces
```
CS123: hwExample (hwtest 1.0.0)
Margaret Hamilton
Data source: hwExample.tests (remote)
Begin testing square at Tue Aug 09 10:24:25 EDT 2022
.....
Passed 5/5 tests in 0.69 seconds.
Begin testing isOdd at Tue Aug 09 10:24:25 EDT 2022
.....
Passed 5/5 tests in 0.15 seconds.
```
Victory!
