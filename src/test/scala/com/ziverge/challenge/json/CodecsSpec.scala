package com.ziverge.challenge.json

import com.ziverge.challenge.TestDataUtil
import com.ziverge.challenge.models.data.DataRecord
import io.circe.Decoder
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Codecs._
import io.circe.Decoder.Result

class CodecsSpec extends AnyWordSpec with Matchers with TestDataUtil {
  "Codecs" should {
    "parse DataRecord" in {
      val result: Result[DataRecord] = Decoder[DataRecord].decodeJson(testJson)
      result.isRight shouldBe true
    }
  }
}
