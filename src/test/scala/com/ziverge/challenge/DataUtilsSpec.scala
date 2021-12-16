package com.ziverge.challenge

import cats.effect.IO
import com.ziverge.challenge.utils.DataUtils
import org.scalatest.{Exceptional, Succeeded}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DataUtilsSpec extends AnyWordSpec with Matchers with TestDataUtil {

  "DataUtils" should {

    "parse ISO DateTimeFormat" in {
      DataUtils.toInstant[IO]("2021-12-16T18:10:00.000Z").attempt.map {
        case Right(_) => Succeeded
        case _        => Exceptional.apply(new Throwable(s"No ISO date time format usage"))
      }
    }

    "extract ISO dates from args" in {
       val args = List("2021-12-16T18:10:00.000Z", "2021-12-16T18:10:00.000Z")
      DataUtils.extractDates[IO](args).attempt.map {
        case Right(_) => Succeeded
        case _        => Exceptional.apply(new Throwable(s"Non ISO date time format useage"))
      }
    }

    "should contains at least 2 dates" in {
       val args = List("2021-12-16T18:10:00.000Z")
      DataUtils.extractDates[IO](args).attempt.map {
        case Right(_) => Succeeded
        case _        => Exceptional.apply(new Throwable(s"Should be provided both start and end dates"))
      }
    }

  }

}
