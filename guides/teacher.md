# A Teacher's Guide to `hwtest`

(Click on the <img src="toc-icon.png" height="16"> icon above for shortcut links)

`hwtest` may be right for you if
  1. you use Scala 3 with your students, and
  2. you frequently or occasionally give them graded or ungraded problems
  where you would like them to be able to check their work.

Note that I will use the word "homework" to generically refer to all kinds
of problem sets, whether they are graded or not and whether they are done in
class or out of class.

## Preparing a homework

Here's an overview of the process for preparing a homework.

1. Come up with the problems.
2. Implement each problem as a function and debug as needed.
3. Place these functions in a homework file such as
```scala
    object hw1 extends hwtest.hw("CS123"):
      def userName = "Margaret Hamilton"

      def problem1(x: Int, y: Int): Int =
        ...
      test("problem1", problem1, "x", "y")

      def problem2(a: String, b: Boolean, c: Array[Int]): List[(Int,String)] =
        ...
      test("problem2", problem2, "a", "b", "c")
```
   You'll usually use [`test`](#the-test-and-ignoretest-commands), but sometimes
   you'll use [`testV`](#the-testV-and-ignoretestV-commands) instead.

   This homework file is likely to be one of several files in an `sbt` or `mill`
   project, but the other files will likely stay the same from homework to
   homework, with only only the one `hwX.scala` file changing each time.
4. [Make the tests](#writing-tests) for the problems, and save the `.tests`
   file in the base directory of the project.
5. Run the program to verify that your code is passing all the tests.
6. Save a backup of your solutions and of the `.tests` file.
7. Replace the bodies of each of the assigned functions with `???`.
8. For each `test` or `testV` except the first, change it to `ignoretest`
   or `ignoretestV`.
9. Run the program again as a sanity check. Every test case should fail.
10. Change the body of `def userName` to `???`.
11. If necessary, do an `sbt clean` (or equivalent).
12. Distribute the resulting project to the students using your preferred distribution channel.
  * I use [GitHub Classroom](https://classroom.github.com) for this.
  * You might use an LMS or [Piazza](https://piazza.com/) instead.
  * You could even just place a `.zip` or `.tar` of the project on your
    course website.

### Project Layout

I'm going to assume `sbt` but the layout would be similar for other systems.
A minimal layout would be
* `hwX/project/build.properties`: The version of `sbt` (eg, `sbt.version=1.5.4`)
* `hwX/build.sbt`: Includes dependencies on `scalatest` and `hwtest`, but you
  might include other settings as desired.
* `hwX/.cache/.url`: The url for the directory of test data (eg,
  `https://my.school.edu/cs/cs123/tests`).
* `hwX/hwX.scala` or `hwX/src/main/scala/hwX.scala`: The starter code for the
  homework. (I usually don't bother with `src/main/scala` when there is
  only a single `.scala` file in the project, especially with relatively
  inexperienced students.)
* `hwX/.gitignore`: Optional, but highly recommended, especially if you're
  using GitHub Classroom.

## The `test` and `ignoretest` commands

The `test` command is the most common command for testing. It is used for
functions where there is (at most) one correct result for any given input.
(If there are multiple correct results, use [`testV`](#the-testv-and-ignoretestv-commands) instead.)

Usually, each problem in a homework will include a stub of the
function to be implemented, and a `test` command for each such function, as
in
```scala
  def middle(x: Int, y: Int, z: Int): Int =
    ???
  test("middle", middle, "x", "y", "z")
```
There are versions of `test` for functions with 1-6 parameters.
(In the very rare occasion that you need more than 6 parameters, combine several
parameters into tuples.)

The test command itself takes
* the name of the function (as a string)
* the function itself
* the names of eeach parameter (as strings)

Behind the scenes, you will include a series of test cases in the corresponding
`.tests` file. (See [Writing tests](#writing-tests).)  When executed,
the `test` command will call the function on the arguments in each test case
and compare the answer to the expected result for the test case, keeping track
of how many test cases passed.

The `test` command will report the results in the form
```
Begin testing middle at Wed Aug 10 13:27:32 EDT 2022
..
Test #3 *** FAILED ***
  x = 3
  y = 2
  z = 9
  Expected answer: 3
  Received answer: 2
..
Passed 4/5 tests in 0.95 seconds.
```
Each `.` indicates a passed test case.  For the first two failed test cases,
the details of the test case are displayed, including the inputs,
the expected answer, and the answer actually produced by the student's code.

If there are more than two failed test cases, the later failures are reported
with an `X` instead of showing the details.

For homeworks that involve more than one function, usually every function after
the first will have an `ignoretest` command, rather than a `test` command, as
in
```scala
  def sum(list: List[Int]): Int = ???
  ignoretest("sum", sum, "list")
```
Instead of running tests on a function that hasn't been implemented yet,
`ignoretest` will display the message
```
***** Ignoring tests for sum.
```
When the student is ready to start testing this function, they should change
the `ignoretest` to `test`.

There are several such `ignoreX` commands in `hwtest`.  There are all
designed so that the word `ignore` on the front can be added or removed,
with an effect kind of like commenting/uncommenting out the corresponding
command. **IMPORTANT: Students should never literally comment out a
`test` (or `testV` command). Doing so will almost always cause any
subsequent tests to fail because the test data will be out of sync with
the tests.**

Because it is common to remove (or sometimes add) the word `ignore` in
front of `test`, the command is written `ignoretest` rather than `ignoreTest`,
which avoids having to switch the case of the `t`.

Note that you can have more than one `test`/`ignoretest` per function.
We'll see several examples of that below.

### Runaway test cases

By default, any particular test case is given 500 milliseconds to finish.
If it doesn't complete, that often means that the student code has an
infinite loop or infinite recursion. After 500 milliseconds, the test
case is canceled and so are the remaining test cases within that test.
The runaway test case is counted as a failure.

**Unfortunately, the JVM makes it extremely hard to actually kill a
runaway test case, so later tests can sometimes be affected by this
still running test case.**

### Configuration: time limits and failure limits

There are two parameters that you can configure for a given `test`,
the time limit given to ever test case within that test (default: 500 milliseconds) and the number of failed test cases for which full details are displayed (default: 2).  If you want to change these defaults, you can do so as shown in the following examples.
```scala
  test("slowfun", slowfun, "n")(timeLimit = 2000) // 2000 milliseconds per test case
  test("buggyfun", buggyfun, "x")(failureLimit = 5) // display full details for up to 5 failed test cases
  test("scaryfun", scaryfun, "q")(timeLimit = 2000, failureLimit = 5)
```
**IMPORTANT:** I strongly *disrecommend* setting time limits *below* the
default 500 milliseconds, especially for the very first test. It's tempting
to do so in an attempt to enforce efficient algorithms, but timings on the JVM
are quite variable so short time limits would cause too many failures that are
not the fault of the student. *The very first test on a cold JVM is particulary
susceptible to delays for things like JIT compilation.*

Occasionally, you might want to test a function that you *expect* to be
extremely slow. You could do this using a large time limit, as in
```scala
  test("tsp", tsp, "graph")(timeLimit = 5000)
```
or you could split these test cases up into several tests for varying sizes,
as in
```scala
  test("tsp (small)", tsp, "graph")
  test("tsp (medium)", tsp, "graph")(timeLimit = 2000)
  test("tsp (large)", tsp, "graph")(timeLimit = 5000)
```

### Testing polymorphic functions

Each `test` command operates for a particular combination of input and ouput
types.  To test a polymorphic function across a variety of types, write one
`test` command for each such combination.

For example, consider a function
```scala
def swap[A,B](pair: (A,B)): (B,A) = ???
```
If you wanted to test this for three different combinations of types, you
could do so as
```scala
test("swap", swap[Int,Char], "pair")
test("swap", swap[String,Boolean], "pair")
test("swap", swap[List[Int],Long], "pair")
```

### Testing with a wrapper

Sometimes the function that you want to test is not (directly) a function
that the student has implemented.  In such cases, you can test a "wrapper"
function, usually expressed as a lambda. I'll show three examples:

1. Testing a function that mutates an array.
```scala
  def sortInPlace(a: Array[Int]): Unit = ???
  test("sortInPlace", (a: Array[Int]) => {sortInPlace(a); a}, "a")
```
Here we test a function that artificially returns the array because the
original function returned `Unit`. Note that we couldn't write the
shorter
```scala
  test("sortInPlace", a => {sortInPlace(a); a}, "a")
```
because the type checker doesn't have enough information to correctly
infer the desired type of the lambda.

2. Arranging for the input (or output) display differently.
```scala
  type Maze = Array[String] // '#' means wall, '.' means open space
  type Path = List[(Int,Int)] // the path through the maze
  def solveMaze(maze: Maze): Path = ???
  test[String,Path]("solveMaze", str => solveMaze(str.trim.split("\\s+")), "maze")
```
You might want to represent the maze as an array of strings, as in
```
  Array("#######",
        "..#...#",
	"#...###",
	"###.#..",
        "#.....#",
	"#######")
```
but, if that was the input, this would display in a failed test case as
```
  Array("#######", "..#...#", "#...###", "###.#..", "#.....#", "#######")
```
which would make debugging harder because it's harder to visualize the maze.
Instead, the lambda in this `test` would take the input as a multiline string,
such as
```
"
  #######
  ..#...#
  #...###
  ###.#..
  #.....#
  #######"
```
which would display in a much more understandable format.  The wrapper
function then converts the string into the desired array of strings before
passing it to the real function. (Of course, ordinary Scala hard-coded strings
cannot span across mulitple lines, but strings in the `.tests` file can.)

3. Avoiding a type with no `Testable` instance.
```
  class Graph:
    ...
  def buildGraph(data: String): Graph = ...
  def longestPath(graph: Graph): Int = ...

  test[String,Int]("buildAndLongest", longestPath(buildGraph(_)), "data")
```
In this case, we have a custom `Graph` type but have not defined a `Testable`
instance for it, so we can't directly `test` functions that take or return a
`Graph`. Instead, we do an end-to-end test that takes a `String` and returns
an `Int`, which the library can handle. (An alternative would be to write a
`Testable` instance for the custom type&mdash;see [Adding custom types](#adding-custom-types).)

## The `testV` and `ignoretestV` commands

The `test` command is intended for functions where there is only one correct
output for each set of inputs. Many functions fall into that category, but
not all.  Sometimes we can get around this problem by changing the question that
we ask.  For example, instead of asking "What is the longest word that satisfies this condition?", which could have multiple valid answers, we might ask "What is the length of the longest word that satisifies this condition?", for which there is only one valid answer.

But when you really want to ask a question that could have multiple **V**-alid
answers, use `testV` instead of `test`.

`testV` takes the same parameters as `test`, plus an extra parameter that
is a validation function (usually provided as a lambda). Here's an example:
```scala
  // find an integer root of the cubic polynomial a*x^3 + b*x^2 + c*x + d
  // under the assumption that such an integer root exists
  def integerRoot(a: Int, b: Int, c: Int, d: Int): Int =
    ???
  testV("integerRoot", integerRoot, "a", "b", "c", "d"){ (a,b,c,d,r) =>
    assert(a*r*r*r + b*r*r + c*r + d == 0, s"\n$r is not a valid root")
  }
```
The validation function inside the `{...}` takes the original parameters
(in this case `a`,`b`,`c`,`d`) and the answer (`r`) returned by the student
code. The validation function should return unit if the answer was acceptable
and should otherwise throw an exception.  Usually the checks inside the
validation function should be done with `assert`s, which will throw the
exception for you. If you throw an exception manually, be sure to include
an explanatory message, as in
```
  throw Exception("answer should have been odd")
```

**IMPORTANT:** Using an `assert` inside a homework invokes a ScalaTest
`assert` rather than the standard Scala `Predef.assert`. The main difference
is that the ScalaTest `assert` will usually display some extra information
compared to `Predef.assert`. See the [ScalaTest page](https://www.scalatest.org/user_guide/using_assertions) ([API](https://www.scalatest.org/scaladoc/3.2.13/org/scalatest/Assertions$.html)) for more information. If desired, you can also
import other methods from the ScalaTest `Assertions` companion object.

Another `testV` example: Even if there is a unique valid answer, you might
sometimes still want to use `testV` instead of `test` because `testV` gives
you the option of including more detailed explanations in the error messages.
For example, this `testV` gives different error messages for several
different situations:
```scala
  def sort(list: List[Int]): List[Int] =
    ???
  testV("sort", sort, "list") { (list, result) =>
    assert(result.length == list.length, s"\nResult is the wrong length!")
    var prev = Int.MinValue
    for x <- result do
      assert(prev <= x, s"\nResult is out of order!")
      prev = x
    var remaining = result
    for x <- list do
      val i = remaining.indexOf(x)
      assert(i != -1, s"\n$x in list, but no matching number in result!")
      remaining = remaining.take(i) ::: remaining.drop(i+1)
  }
```

Like `ignoretest`, you can disable a `testV` by changing it to `ignoretestV`.

## The `debug` and `ignoredebug` commands

Looking at the results of failed tests is a powerful aid to debugging. But
sometimes students may want to do some good old-fashioned `println`-debugging.
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
  comment out or remove any extra `print`/`println`s added inside the
  function being debugged.  Then change the `debug` to `ignoredebug` until
  you're sure you don't need it anymore.

You may be wondering why `debug` is worth bothering with. The answer goes
back to the de-magification of the magic `App` trait, which used to use
the old `DelayedInit` semantics.  The short version is that object
initialization order is tricky, making it possible to sometimes access a
`val`/`var` before it has been initialized. `debug` (and `test`/`testV` for that
matter) will delay executing their logic until the homework object has finished
initializing and the `main` method has begun.

## Writing tests

All the test cases for a homework will be included in a `.tests` file
with the same name as the homework object.  For example, if the homework is
`object hw7`, then the corresponding file of test cases will be `hw7.tests`.

### Where to put the `.tests` file

There are three locations where `hwtest` will look for the `.tests` file.
In *decreasing* order of priority, these are
* **(local)**: The base directory of the project. Usually this is only done
  when the teacher is developing the homework, or perhaps when the teacher
  is putting student code through an [auto-grader](#auto-grading).
* **(remote)**: This is the most common location. Create a directory `.cache` in
  the base directory, and inside the `.cache` directory include a file `.url
  with a single line of text containing a URL for the directory where
  the `.tests` file will be located.  Note that this URL should **not** include
  the name of the `.tests` file. That will be appended when the tests are
  downloaded.  (The base URL will likely stay the same for every homework
  of the semester, with only the file name changing.)
* **(cached)**: When the `.tests` file is fetched remotely, a copy is cached inside
  the `.cache` directory. That way, once the `.tests` file has been downloaded,
  the student can continue to work even without access to the network.

Note that caches are often used for efficiency, but that is not the purpose
here. Instead the purpose of this cached copy is purely to protect against
network/web outages.

There is a good reason to continue to access the `.tests` file remotely
even when a cached copy is available locally. If, after the `.tests` file
has been released, you discover that there is an error inside the file, you
can fix the error and replace the old copy at the URL. Then, the students will
all get the new `.tests` file the next time they run their code. (Just be aware
that an eager student who has already finished the homework might not re-run
their code unless you make some kind of announcement.)

A second use case for this behavior of downloading the `.tests` file even
when a cached copy is available is to release test cases in stages. I usually
only do this when I want to let students get started on the homework, but I
haven't finished creating all the tests yet. I may publish the `.tests` file
with only a few rudimentary test cases, and then add the rest of the tests
over the next day or two. Usually in this situation, I will make an anouncement
when the tests are done.

### Creating the `.tests` file

The `.tests` file is a text file with the test data for each `test`/`ignoretest`/`testV`/`ignoretestV` in the order that those commands appear in the code file.
For example, if the code had three `test`s
```scala
  def insertSorted(x: Int, sorted: List[Int]): List[Int] = ???
  test("insertSorted", insertSorted, "x", "sorted")

  def flatten(lol: List[List[Int]]): List[Int] = ???
  test("flatten", flatten, "lol")

  def reverse(list: List[Int]): List[Int] = ???
  test("reverse", reverse, "list")
```
then the `.tests` file would be organized as
```
<test cases for insertSorted>
$
<test cases for flatten>
$
<test cases for reverse>
$
```
The `$` signals the end of that set of test cases. It's recommended to put
each `$` on a line by itself, but not required.

Inside each section, write the data for each test case. Again, it's recommended to put a line break between test cases, but not required.
The data for a `test` with N parameters will have the format
```
<input1> <input2> ... <inputN> <result>
```
and the data for a `testV` with N parameters will have the format
```
<input1> <input2> ... <inputN>
```
There should be whitespace between inputs and between the last input
and the result. (As you'll see, almost everything in a `.tests` file is
whitespace separated.)

The format for writing values for each different type is described in
[Supported types](#supported-types).

You can also write comments in `.tests` file.  For example, you might
put a comment at the top of each section with the name of the function
being tested by that section. A comment starts with a `#`
and extends to the end of the line. Of course, `#` characters inside
quotes are string or character data, as in
```
# this is a comment
"abc#def" # this is a String containing a #
'#' # this is Char
```

### Coming up with test cases

You'll typically make up a few test cases (maybe 3-10) by hand, both to help you
think through the variety of cases that should be tested and to check your
own solution.

You may decide that's enough, or you may decide you want to provide more tests
than that. It's not unusual for me to provide 3-10 test cases, but it's also
not unusual for me to provide 100-300 test cases.  For that many cases, I
will generate random inputs, typically of increasing sizes. For example,
if a function took a list as its only parameter, I might write cases by hand for lists of length 0-2, and then generate 5 lists each for lengths 3-9, and 10 lists each for lengths 10-40.

In generating random inputs, I might generate them all the same way, or I might generate them in several different ways. For example, if a test involves binary trees, I might make some that are perfectly balanced, some with a uniform
random distribution (which tends to make reasonably balanced trees), and some
with a non-uniform random distribution (which tends to make more unbalanced trees).

Suppose I wanted to test a `size` function on binary trees. I might write
a few small tests by hand, and then use code like this to generate the rest:
```scala
  import hwtest.binarytrees.{BinaryTree, Empty, Node}

  // choose a random number from 0 to n-1
  def rand(n: Int): Int = (math.random() * n).toInt

  // a very NON-random generator to make perfectly balanced trees
  def perfect(n: Int): Int = (n+1)/2

  // try to make a rather unbalanced tree
  def unbalanced(n: Int): Int =
    // pick two random numbers, and choose the one farthest from the middle
    val mid = (n+1)/2
    List.fill(2)(rand(n)).maxBy(x => (x - mid).abs)

  def randArray(n: Int): Array[Int] = Array.fill(n)(rand(n*10))

  def make(elems: Array[Int], rand: Int => Int): BinaryTree[Int] =
    val n = elems.length
    if n == 0 then Empty
    else
      val mid = 1 + rand(n)
      Node(elems(0), make(elems.slice(1, mid),rand), make(elems.drop(mid),rand))

  def encode(tree: BinaryTree[Int]): String = tree match
    case Empty => "E"
    case Node(item,left,right) => s"T$item ${encode(left)} ${encode(right)}"

  for
    size <- 3 to 30
    r <- List(perfect, rand, rand, unbalanced, unbalanced)
  do
    println(encode(make(randArray(size), r)))
    println(size)
```
This code will generate five diferent trees of each size. For each one,
it will print the tree (in the format the tester is expecting for the
`BinaryTree[Int]` type) and the size of the tree (which would be the result
of the function to be tested).  For example, here's one such tree and size:
```
T32 T11 T0 E T71 E E T85 T4 E E T60 E T74 E T48 E E E
9
```
Clearly, that format for trees isn't particularly readable, but the
`.tests` files are designed for ease of parsing, not for readability.
The student will likely never see that format. In contrast, when a student
fails a particular test case, it is important that the inputs and outputs
be displayed in a more readable format. For example, that tree would be
be displayed in an error message like this
```
    _11_
   /    \
  0     85
   \   /  \
   71 4   60
            \
            74
              \
              48
```

That code for generating the test cases may be more code than you want to write,
especially when that was for only one problem on a homework that likely has
several problems. The good news is that this kind of code tends to be
extremely reusable across different tests on similar data types. Generating
the test data rarely ends up being the bottleneck.

One other comment about that example: In this case, I knew the size because
of the way I generated the random trees. In other situations, I would still
generate the inputs randomly but I would use my own implementation of the
function in question to calculate the expected result.

## Supported types

`hwtest` supports a type `T` if there is a `given` `Testable[T]` instance
for that type. Such an instance determines, for example,
  * how a value of that type can be read from a `.tests` file
  * how a value of that type will be displayed in an error message for a failed test
  * how two values of that type are compared in a test case to determine if the test case passed.

For a polymorphic type, like `List[T]`, it's not enough for `List` by itself
to be testable&mdash;`T` must also be a supported type for `List[T]` to be
testable.

Below, I will describe each supported type (usually briefly):

* `Int`/`Long`/`BigInt`:
   - Numbers are formatted as expected in both test cases and in test output.
     Examples: `123`, `-57`.
   - The system always knows which kind of number it's working with, so there
     is *no need* to, for example, signal a `Long` by appending an `L`.
* `Double`:
   - **Avoid using `Double`s if possible.** Floating point numbers are
     inherently a mess. For example, two different algorithms for computing
     the same result will usually end up with slightly different answers.
     This means a test will considered to pass if the answers are
     "close enough".
   - Numbers should be written in test cases as, for example, `-123.4567`
     and will display similarly.
* `Boolean`:
   - Write true as `T` and false as `F` in test data.
   - Booleans will display as `true` and `false` in test results.
* `Char`/`String`:
   - In test data, write a char with single quotes, as in `'A'`,
     and a string with double quotes, as in `"abc"`.
   - In test results, a char will be displayed with single quotes and a
     string with double quotes.
   - Escape characters are permitted when writing a char or string in test
     cases, as in `'\n'` or `"hello\nworld"`. However, no attempt is made
     to re-escape special characters when a char or string is displayed
     in test results.
* `List`/`Array`/`Set`:
   - Write a list/array/set in test data as whitespace-separated elements
     enclosed in parentheses, as in `(1 2 3 4)` or `()`. Write the individual
     elements in the format expected for their type.
   - They will display as `List(1, 2, 3)` or `Array()` or `Set(4, 5)`.
     The order of elements is significant for lists and arrays, but not
     for these sets. (In fact, the elements in sets will display in a kind
     of sorted order, but there is no significance to that order except that
     it makes it easier to spot differences between an expected answer and
     a received answer.)
   - For two-dimensional arrays, see also [TestableGrid](#testablegrid).
* `Map`:
   - Write as whitespace separated key-value pairs, as in
     `(key1 val1 ... keyN valN)`.  Example: `(1 "a" 2 "b" 3 "c")`.
   - A map will display in test results as `(1 -> "a", 2 -> "b", 3 -> "c")`.
   - Like sets, these maps are unordered so the order in which their key-value
     pairs are displayed is not significant.  Again, they will be displayed
     in a kind of sorted order, but there is no significance to that order
     except that it makes it easier to spot differences between an
     expected answer and a received answer.
* `Option`:
   - Write an option in test data as `N` (for `None`) or `S8` (for `Some(8)`).
     The value inside the `Some` should be writted in the format appropriate
     for its type. White space is permitted but not required between the `S` and
     its element.
   - An option will display in test results as `None` or `Some(8)`.

* Tuples:
   - Tuples are supported from size 2 to size 6. (If you need more, use nested
     tuples to reduce the size below 6.)
   - Write tuples in test data as whitespace-separated elements without parentheses.  For example, the tuple `(1, true, "B")` should be written `1 T "B"`.
   - Tuples will display in test results as `(1, true, "B")`.

### Extras: `TestableGrid`

If your function takes or returns a two-dimensional array, you may want that
array to display in failed test cases as
```
  Array(
    Array(    1, 0,  10),
    Array(-1592, 3,   7),
    Array(   67, 2, 192)
  )
```
instead of
```
  Array(Array(1, 0, 10), Array(-1592, 3, 7), Array(67, 2, 192))
```
To enable this, include the line
```
   given hwtest.Testable[Array[Array[Int]]] = hwtest.Testable.TestableGrid[Int]
```
either near the top of the file or just above the corresponding `test`,
changing the `Int` type in that example if necessary.

Note that, even with this line, the two-dimensional array will *not* display in
the grid format if
* the inner arrays have different lengths, or
* the array of arrays is itself embedded in some other structure.
In such cases, the array will display in the ordinary linear format.

It is conceivable that you may have several functions that involve arrays of
arrays (of the same element type), but you only want one function to display
as a grid, with the others displaying in the ordinary linear format.  In
that case, you can put curly braces around the `given` and the `test`.
```scala
  {
    given hwtest.Testable[Array[Array[Int]]] = hwtest.Testable.TestableGrid[Int]
    test(...)
  }
```

### Extras: `SList`/`MList`

`hwtest` includes two simplified variations of lists, `SList`
(**S**implified lists or **S**ingly-linked immutable lists) and
`MList` (singly-linked **M**utable lists). And, yes, I know that
naming structure is not parallel.  To use these, include
```scala
  import hwtest.slist.*
```
or
```scala
  import hwtest.mlist.*
```
at the top of the program.

The simplifications of `SList` and `MList` are two-fold:
1. Elements are limited to integers (and therefore no type parameter is needed).
2. These lists support only a handful of methods: `isEmpty`, `nonEmpty`, `head`, `tail`, and `::`, plus `SList.empty` or `MList.empty`.

In addition, `MList` supports assignments to the head and tail, as in
```scala
  mlist.head = 5
  mlist.tail = mlist.tail.tail
```

`SList` is a gentle introduction for students who have never dealt with linked
lists before. If they have dealt with linked lists before, then jumping
straight to the regular Scala `List` type is probably fine.

`MList` is much less gentle because of all the inherent confusions with
mutable linked lists, not the least of which is the possibility of cycles!

Included with `MList` in the `mlist` package is `MHeader`, defined as
```scala
  class MHeader[A](var info: A, var front: MList = MList.empty)
```
Often, the `info` will be used to hold the length of the list, but
it can be used for other purposes as well.

Both `SList`s and `MList`s should be written in test data using the same
format as for regular lists of integers, such as
```
  (1 2 3 4)
```
They will display in test results as `SList(1, 2, 3, 4)` or
`MList(1, 2, 3, 4)`.

An `MHeader` should be written in data using the format
```
  <info> (1 2 3 4)
```
where `<info>` is formatted according to the type of the info. For example,
```scala
  MHeader(4, MList(10, 20, 30, 40))
```
would be written in test data as
```
  4 (10 20 30 40)
```
and displayed in test results as
```
  MHeader(4 ; 10, 20, 30, 40)
```
where the part before the semi-colon is the `<info>` and the part after
the semi-colon is the elements of the list.

#### Cycles

Mutable lists are susceptible to cycles, either deliberate or accidental.
For example, suppose you had a list 1, 2, 3, 4, where the `tail` of the 4
node pointed back to the 2 node. This would be written in test data as
```
  (1 *2 3 4)
```
where the `*` indicates that the last node (in the case the 4 node) points back
to the node indicated by the `*` (in this case the 2 node). A list without a cycle has no `*`.

An `MList` with a cycle is displayed in test results as
```
  MList(1, *2, 3, 4->*)
```
where the `->*` attempts to make it visually obvious that the 4-node points
back to the 2-node.

A similar format is used when displaying an `MHeader` whose `MList` has a
cycle, such as
```
  MHeader("cycle" ; 1, *2, 3, 4->*)
```
if the info in the header was the string `"cycle"`.

### Extras: `BinaryTree`/`SearchTree`

The `BinaryTree` type is defined as
```scala
  enum BinaryTree[+A]:
    case Empty
    case Node(item: A, left: BinaryTree[A], right: BinaryTree[A])
```
but the `Empty` and `Node` constructors are exported so you can just
say, for example, `Empty` instead of `BinaryTree.Empty`.

The `SearchTree` type is very similar
```scala
  enum SearchTree[+A]:
    case Empty
    case Node(left: SearchTree[A], item: A, right: SearchTree[A])
```
where again the `Empty` and `Node` constructors are exported.

To access these data structures place
```scala
  import hwtest.binarytrees.*
```
or
```scala
  import hwtest.searchtrees.*
```
at the top of your file.

The major difference between the two types of trees is the placement of the
`item`. In a `BinaryTree`, the fields are `item`,`left`,`right`, and
in a `SearchTree` the fields are `left`,`item`,`right`.

**Although the `SearchTree` type is intended be ordered as a binary search tree,
neither the type nor the tester enforces that ordering. However, if a student
violates that ordering, such a violation should almost always be caught by
your tests.**

The test data for a binary tree should have the form given by
the following context-free grammar:
```
  <tree> = 'T' <item> ' ' <tree> ' ' <tree>
         | 'L' <item>
	 | 'E'
```
For example, the tree
```scala
  Node(1, Node(2, Node(3,Empty,Empty), Empty),
          Node(4, Empty, Node(5,Empty,Empty)))
```
would be encoded in test data as
```
  T1 T2 L3 E T4 E L5
```
or, because `Lx` is the same as `Tx E E`, it could also be encoded as
```
  T1 T2 T3 E E E T4 E T5 E E
```
The `L` format is a little bit easier if you are writing tests by hand,
but it's even easier to use a function to do the encoding, as in
```scala
  def encode(tree: BinaryTree[Int]): String = tree match
    case Empty => "E"
    case Node(x, left, right) => s"T$x ${encode(left)} ${encode(right)}"
```
If the elements are not integers, then use the appropriate encoding for `x`
as well.

The encoding for search trees is very similar except the element is placed
between the left and right, instead of before the left.
```scala
  def encode(tree: SearchTree[Int]): String = tree match
    case Empty => "E"
    case Node(left, x, right) => s"T${encode(left)} $x ${encode(right)}"
```

In test results, both binary trees and search trees are displayed in a
pictorial format, as in
```
      __25__
     /      \
    5     __457__
   / \   /       \
  1  13 36      18145
       \  \     /
       14 119 5124
```
The empty tree displays in test results as
```
  <empty tree>
```
But if the tree is itself embedded in another structure (such as a list of
trees), then it will display in a linear format instead, as in
```
 Node(1, Node(2, Empty, Empty), Node(3, Empty, Empty))
```

## Adding custom types

Coming soon.

### Parsers

Coming soon.

## Auto-grading

I do not use auto-grading, and `hwtest` does not offer a turnkey solution
for auto-grading.  However, it should be relatively straightforward to integrate
with an existing auto-grading infrastructure.

Typically, for autograding, you would create more extensive test cases than the
ones provided to students.  These would be placed in a `.tests` file as usual.

Next, you would copy this `.tests` file into the base directory of the code
being tested. `hwtest` gives priority to such local tests over remote or cached tests.

Finally, you would run the code and use a small script on the output to compute
the score.  At minimum, such a script would look for lines in the output like
```
Passed 4/5 tests in 0.22 seconds.
```
and base the score on the number or percentage of passed tests.
