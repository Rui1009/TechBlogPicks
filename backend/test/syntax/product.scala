package syntax

object product extends ProductSyntax

trait ProductSyntax {
  implicit final def testSyntaxProduct[T <: Product](p: T): ProductOps[T] =
    new ProductOps(p)

}

final class ProductOps[T <: Product](private val p: T) extends AnyVal {
  /*
   * case classなどの最初の1要素を無視して、それ以外が等しいことを検証する
   * idなどauto incな値を無視して等値性を検証したいときに有用
   */
  def shouldEqExceptForFirst(that: T): Unit = shouldEqExceptFor(1)(that)

  /* Productの最初のnum個分の要素を無視して、それ以外の要素の等値性を検証する */
  def shouldEqExceptFor(num: Int)(that: T): Unit =
    shouldPartiallyEq(Seq.range(0, num): _*)(that)

  def shouldPartiallyEq(idxesToDrop: Int*)(that: T): Unit = {
    val thisIter = p.productIterator
    val thatIter = that.productIterator

    thisIter.zip(thatIter).zipWithIndex.foreach { case ((left, right), idx) =>
      if (!idxesToDrop.contains(idx)) assert(left == right)
    }
  }
}
