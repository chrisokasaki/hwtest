# A Student's Guide to `hwtest`

(Click on the <img src="toc-icon.png" height="16"> icon above for shortcut links)

`hwtest` is a library that helps you check your answers to programming
problems. I will generically refer to a problem set as a "homework", whether
it is graded or ungraded, done in class or out of class.

Your teacher will distribute starter code for a homework, as well as
descriptions of the problems in the homework. The descriptions might
be included as comments in the starter code, or might be someplace else,
like a course webpage.

The starter code will likely be a directory with a single `.scala` file
(perhaps with a name like `hw1.scala`) and several other supporting files
and directories.  I will assume the directory is in the form of an `sbt`
project, in which case the `hw1.scala` file will be in either the top-level
directory or in a `src/main/scala` sub-directory.

Open the `hw1.scala` file, and it will look something like this:
```scala
object hw1 extends hwtest.hw("CS123"):
  def userName = ???

  def biggest(x: Int, y: Int): Int =
    ???
  test("biggest", biggest, "x", "y")

  def smallest(x: Int, y: Int, z: Int): Int =
    ???
  ignoretest("smallest", smallest, "x", "y", "z")
```
Normally, the first thing you should do is fill in the `userName`, but (just
this once) try running the code without filling in your `userName` to see what
happens. (If you're using `sbt`, you can do this as `sbt run` or even
better `sbt ~run`, which will automatically re-run the tests every time
you save the file.) It will print something like
```
CS123: hw1 (hwtest 1.0.0)
scala.NotImplementedError: an implementation is missing
       at scala.Predef$.$qmark$qmark$qmark(Predef.scala:344)
       at hw1$.userName(hw1.scala:2)
```
but with several more lines of stack trace. The `scala.NotImplementedError...`
part indicates that an exception was thrown.  Search through the stack trace
for the first `at` line that comes from *your* file, in this case
```
       at hw1$.userName(hw1.scala:2)
```
which tells you that you should look at line 2 in your code, which is
```scala
  def userName = ???
```
The `???` is a placeholder for code that hasn't been filled in.
It always throws `NotImplementedError` when run. Replace the
`???` with a string containing your name, as in
```scala
  def userName = "Margaret Hamilton" // but with your name obviously
```
(Margaret Hamilton is a famous computer scientist best known for leading the
development of the [flight software for the Apollo missions](https://en.wikipedia.org/wiki/Margaret_Hamilton_(software_engineer)#Apollo_program).)

If you used `sbt ~run`, the code will re-run as soon as you save your change.
If not, you can `sbt run` again.  Either way, the code now outputs something
like
```
CS123: hw1 (hwtest 1.0.0)
Margaret Hamilton
Data source: hw1.tests (remote)
Begin testing biggest at Sat Aug 13 11:02:48 EDT 2022
Test #1 *** FAILED ***
  x = 7
  y = 7
  scala.NotImplementedError: an implementation is missing
Test #2 *** FAILED ***
  x = 1
  y = 2
  scala.NotImplementedError: an implementation is missing
XX
Passed 0/4 tests in 0.58 seconds.
***** Ignoring tests for smallest.
```
You can see that the first two test cases for the `biggest` function
failed, again reporting the `scala.NotImplementedError` because
the body of the `biggest` function is still `???`.  The `XX` after
the message for `Test #2` indicates that two subsequent test cases also
failed.  By default, the system will only report the details of
the first two failed test cases (per `test`), and reports any further
failures with an `X`. Successful test cases are reported with a `.`.

It may seem strange to hide some of the failure messages, but there are
two reasons for this. First, seeing page after page of failure messages
can be overwhelming. Second, if all the failures are because of the same
bug, there's an excellent chance that two such messages will be enough to
find the error, and if the failures stem from different bugs, then seeing
many such messages can actually make it harder to find any one of those bugs.

Now you might replace the `???` in `biggest` with
```scala
    if x < y then x
    else y
```
and now the output says
```
CS123: hw1 (hwtest 1.0.0)
Margaret Hamilton
Data source: hw1.tests (remote)
Begin testing biggest at Sat Aug 13 11:05:27 EDT 2022
.
Test #2 *** FAILED ***
  x = 1
  y = 2
  Expected answer: 2
  Received answer: 1
Test #3 *** FAILED ***
  x = 4
  y = 3
  Expected answer: 4
  Received answer: 3
X
Passed 1/4 tests in 0.08 seconds.
***** Ignoring tests for smallest.
```
Hmm, that passed the first test case, but then failed the next three.
Looking at the results for the second and third test cases, you seem to
be returning the smallest number, not the biggest number. Going back
to your code, you realize you wrote `<` in the `if` when you meant `>`.
(Weird. How did the first test case pass? Maybe `x` and `y` were equal?)
You change the `<` to `>` and get
```
CS123: hw1 (hwtest 1.0.0)
Margaret Hamilton
Data source: hw1.tests (remote)
Begin testing biggest at Sat Aug 13 11:08:32 EDT 2022
....
Passed 4/4 tests in 0.03 seconds.
***** Ignoring tests for smallest.
```
Yay!

Now you're ready to work on 'smallest'.  You first delete `ignore`
from the `ignoretest(...)`, leaving `test(...)`. You remove the
`???` and start to write a bunch of nested `if`s. After a few minutes,
you delete them all, and write
```scala
  -biggest(-x, biggest(-y, -z))
```
You run the code and get
```
CS123: hw1 (hwtest 1.0.0)
Margaret Hamilton
Data source: hw1.tests (remote)
Begin testing biggest at Sat Aug 13 11:21:40 EDT 2022
....
Passed 4/4 tests in 0.11 seconds.
Begin testing smallest at Sat Aug 13 11:21:52 EDT 2022
..........
Passed 10/10 tests in 0.16 seconds.
```
Victory!

## Getting the test data

`hwtest` will normally download the test data every time you run your
code.  Once your code has gotten as far as printing
```
Begin testing [...]
```
it has successfully downloaded the test data.  Even if your
code fails every test, `hwtest` will keep a local copy of the test
data. This means that you can continue to make progress even if the
network or wifi goes down, or if you otherwise lose network access
(for example, maybe you're on an airplane or you've gone spelunking
for the weekend).

One implication of this is that it's a good idea to download the homework
as soon as it's available. Set your `userName` and run the code right
away so `hwtest` can fetch the test cases. Even if you don't work on the
problems right away, at least you'll have the test data.

Note that your teacher could update the test data after the homework has
been assigned, maybe even multiple times. `hwtest` will automatically fetch
the latest version of the test data every time you run the code (unless there
are network issues, in which case it will use the most-recently downloaded
copy).

If you run your code and it reports that you passed, say 60/100 tests,
and then you run the code again a few minutes later and it reports 97/150
tests passed, this is why.

## The `testV` command

The starter code may sometimes say
```scala
  testV(...){...}
```
(or `ignoretestV`) instead of `test`/`ignoretest`.

Here's the difference: `test` is for functions where there is a unique answer
for each distinct set of inputs. For example, consider addition.  Given
the inputs `4` and `7`, the only correct answer is `11`.  In contrast, `testV`
is intended for functions the could possibly have more than one correct answer
for each distinct set of inputs.  For example, if a function takes a
non-empty list of integers and returns the *index* of the maximum element, there
could be several legal answers (eg, `List(1,5,5,2,5,3)` could legitimately
return 1, 2, or 4). Any of those answers would be acceptable.

Alternatively, sometimes a teacher might use `testV` even when `test` would
work because `testV` allows greater customization of error messages.

From the student point of view, there's no need to treat it any differently
than `test`. Implement your function, and use any error messages to help
you debug your code.

If needed, you can change `testV` to `ignoretestV`, just like changing `test`
to `ignoretest`. **Never delete or comment out a `testV` or `ignoretestV`.**

## The `debug` command

Looking at the results of failed tests is a powerful aid to debugging. But
sometimes you may want to do some good old-fashioned `println`-debugging.
That's where the `debug` command comes in.

To do `println`-debugging:
* Usually, start by changing any `test`/`testV` commands for the function
  being debugged to `ignoretest`/`ignoretestV`.
* Next, include one or more manual calls to the function inside a `debug` as in
```scala
  def foo(n): Int = ...
  ignoretest("foo", foo, "n")
  debug("index out of bounds in foo") {
    for i <- 1 to 10 do
      println(s"foo($i) = ${foo(i)}")
  }
```
* Optionally, you can also add `print`/`println`s inside the function
  being debugged.  However, **do NOT** wrap those inner `print`/`println`s
  in their own `debug` statements. (If you do, there is a good chance that
  the printed output will display in a weird order.)
* When you are ready to change the `ignoretest` back to `test`, first
  comment out or remove any extra `print`/`println`s inside the
  function being debugged.  Then change the `debug` to `ignoredebug` until
  you're sure you don't need it anymore.

**WARNINGS:**

* Don't put `print`/`println`s at the top level of your object. They will
  usually display at weird place in your output.
* Contrariwise, don't put a `debug` command *inside* the function that
  you are trying to debug.  Again, this will usually display at a weird place
  in your output.

## Timeouts

To protect against infinite loops/infinite recursion, `hwtest` will cancel the
remaining cases in a test if any one test case takes too much time (usually
half a second). The cancelled test case counts as having failed.

However, because of things like
[JIT compilation](https://www.geeksforgeeks.org/just-in-time-compiler/),
there can be a lot of variability in the reported times of your functions.
This is especially true of the very first `test`/`testV` in the homework!

If a `test`/`testV` times out, you can try running it again, in case the
problem was transient. If it still times out, it most likely means that
your code really is too slow. If so, look for ways to speed it up.

* If you have nested loops, can you reduce the number of loops (for example,
  turning three nested loops into two nested loops)?
* Are you abusing any data structures?
  - If you are repeatedly looking up an index in a list, can you switch
    to an array instead?
  - If you are repeatedly looking for a particular value in a list or array,
    can you switch to a set instead?
  - If you are repeatedly adding to the back of a list, can you add to
    the front instead, possibly reversing the list later?  Or can you use a
    `ListBuffer` instead?
  - If you are repeatedly adding to the front of an array, can you use a
    list instead? If you are repeatedly adding to the back of an array,
    can you use an `ArrayBuffer` instead?

## Common Mistakes

Here's some advice for avoiding common mistakes:

* DON'T EVER delete or comment out a `test`/`ignoretest`/`testV`/`ignoretestV` line.
  - If you do, your code will most likely fail with a parse error.
  - If you don't want to run that test, change it to `ignoretest`/`ignoretestV`.
  - If your code for a particular function doesn't compile, and that's stopping
     you from testing the rest of your homework, you can comment out the *body*
     of the function (and replace that body with `???`).

* DON'T change the name, parameters, or type of a function being tested.
  - If you do, your program will almost certainly either (1) not compile
    or (2) crash with a parse error.
  - If you feel like it would be better for the function to use a different
    interface, you can define a *helper* function, and have the main function
    call the helper function.

* DON'T leave an `ignoretest`/`ignoretestV` in your code if you are
  turning it in.
  - Sometimes I see students get a function running, then change the `test`
    back to `ignoretest` while they are working on other problems.  Just leave
    it as `test` -- if the function is running, it only produces a few lines of
    output! If you turn it in as `ignoretest`, you may risk not getting credit
    for a problem you actually solved.
  - Even if you're failing some test cases, turning it in as `test` may help
    you get partial credit.

* DON'T forget to change the `???` in the `userName`. Also, don't delete
  or comment out the `def userName` line.
