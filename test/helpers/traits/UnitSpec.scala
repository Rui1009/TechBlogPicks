package helpers.traits

import helpers.gens.AllGen
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

trait UnitSpec
    extends AnyWordSpec
    with ScalaFutures // Futureへの簡易アクセッサ
    with OptionValues // Optionの簡易アクセッサ
    with EitherValues // Eitherの簡易アクセッサ
    with PrivateMethodTester // プライベートフィールドアクセス
    with BeforeAndAfter // before/after用
    with Inside // モデルがネストしたときの簡易テスト構文
    with ScalaCheckDrivenPropertyChecks // pbt用
    with AllGen
