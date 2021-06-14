package domains

import domains.channel.Channel._
import helpers.traits.ModelSpec

class ChannelDomainSpec extends ModelSpec {
  "ChannelId.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = ChannelId.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = ChannelId.create("")
        assert(result.leftSide === Left(EmptyStringError("ChannelId")))
      }
    }
  }

  "Channel.isMessageExists" when {
    "history length is not empty" should {
      "return true" in {
        forAll(channelTypedChannelMessageGen, channelMessageGen) {
          (_channel, message) =>
            val channel = _channel.copy(history = Seq(message))
            val result  = channel.isMessageExists
            assert(result === true)
        }
      }
    }

    "history length is empty" should {
      "return false" in {
        forAll(channelTypedChannelMessageGen) { (_channel) =>
          val channel = _channel.copy(history = Seq())
          val result  = channel.isMessageExists
          assert(result === false)
        }
      }
    }
  }

//  "ChannelMessageSentAt.create" when {
//    "given valid float" should {
//      "return Right value which equals given arg value" in {
//        ChannelMessageSentAt.create(1f)
//      }
//    }
//  }
}
