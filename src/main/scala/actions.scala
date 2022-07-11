package hwtest

/** Provides methods to delay arbitrary code ("actions") and to execute the
  * delayed actions later in FIFO order.
  *
  * When delayed code is eventually executed, its resulting value will be
  * discarded so the delayed code should be effectful.
  *
  * The point of this delay is to avoid potential initialization-order bugs
  * inside the homework object (`object hw7 extends ...`) that would otherwise
  * be very confusing for students.  For example, a test might indirectly
  * depend on a `val` that has not yet been initialized. Instead, the test
  * will delayed--invisibly to the student--to be executed inside the
  * `main` method, after object initialization is complete.
  */
trait Actions:

  private val queue = scala.collection.mutable.Queue.empty[() => Any]

  /** Runs all the delayed actions in FIFO order.
    *
    * Normally called once, inside the `main` method of the [[hwtest.hw]] class.
    */
  private[hwtest] def runActions(): Unit =
    while queue.nonEmpty do
      val action = queue.dequeue()
      action()

  /** Delays arbitrary code to be executed later by `runActions`.
    *
    * The delayed code from multiple calls to `registerAction` will be
    * executed in FIFO order. Because of this FIFO order, *nested* calls
    * of `registerAction` may behave in unexpected ways, especially when
    * recursion is involved.
    *
    * Note that values produced by the delayed code are discarded, so
    * such code should always be effectful. Contrariwise, any effectful
    * code in the body of a normal homework should usually be wrapped
    * a `registerAction`.
    *
    * @param action arbitrary code to be executed later
  */
  def registerAction(action: => Any): Unit =
    queue.enqueue(() => action)
