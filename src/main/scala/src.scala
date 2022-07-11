package hwtest

/** Iterator through a string representing test data, primarily used
  * as input to `Parser`s.
  *
  * Note that the iterator normally contains all the test data for all the
  * tests, not just the data for a single test. This "global" `Src` is
  * maintained in [[hwtest.hw]].
  *
  * The `Src` iterator is primarly used as input to `Parser`s.
  *
  * Note: Although `Src` is conceptually an iterator implementing the
  * standard `hasNext` and `next()` methods, it does **NOT** inherit from
  * the `Iterator` trait.
  *
  * @param text the test data as a `String`
  */
class Src(val text: String):
  private var pos = 0

  /** Tests whether this source contains another character. */
  def hasNext: Boolean =
    pos < text.length

  /** Tests whether this source contains another significant character
    * (non-whitespace, non-comment).
    */
  def hasSignificant: Boolean =
    skipWhite()
    hasNext

  /** Returns the next character, consuming it.
    * Returns `'$'` if source is empty.
    */
  def next(): Char =
    if hasNext then
      val ch = text(pos)
      pos += 1
      ch
    else '$' // return '$' when empty instead of throwing an exception

  /** Returns the next character, without consuming it.
    * Returns `'$'` if source is empty.
    */
  def head: Char = if hasNext then text(pos) else '$'

  /** Is this the end of the current test (as indicated by a `$` in the source)?
    *
    * Consumes the `$`.
    */
  def endOfTest(): Boolean =
    skipWhite()
    if head == '$' then
      next()
      true
    else
      false

  /** Discards whitespace--including comments--until the next non-whitespace
    * character (or the end of the source).
    *
    * Comments count as whitespace, from a `#` to the nearest `\n` or `\r`,
    * after which it skips more whitespace (including the `\n` in a `\r\n`).
    */
  def skipWhite(): Unit =
    while hasNext do
      if head.isWhitespace then next()
      else if head == '#' then
        while hasNext && head != '\n' && head != '\r' do next()
      else return
