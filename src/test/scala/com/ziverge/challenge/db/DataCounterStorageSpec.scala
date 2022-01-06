package com.ziverge.challenge.db

import cats.effect.IO
import com.ziverge.challenge.TestDataUtil
import com.ziverge.challenge.utils.DataUtils
import com.ziverge.challenge.utils.DataUtils.makeMap
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DataCounterStorageSpec extends AnyWordSpec with Matchers with TestDataUtil {

  "DataCounterStorage" should {
    "count events and words" in {
      val result =
        for {
          storage <- DataCounterStorage.state[IO]()
          full = DataUtils.count(testData)

          _ <- storage.add(full)
          _ <- storage.add(DataUtils.makeMap(recAI))
          _ <- storage.add(DataUtils.makeMap(recAI))
          _ <- storage.sub(DataUtils.makeMap(recAI))
          _ <- storage.add(DataUtils.makeMap(recCK))
          _ <- storage.sub(DataUtils.makeMap(recCK))

          found <- storage.fetchALL
        } yield {

          found.size shouldBe 3
          found.get(eventA).flatMap(_.get(wordI)) shouldBe Some(2)
          found.get(eventA).flatMap(_.get(wordK)) shouldBe Some(1)
          found.get(eventB).flatMap(_.get(wordJ)) shouldBe Some(1)
          found.get(eventC).flatMap(_.get(wordK)) shouldBe Some(1)
          found.get(eventX) shouldBe None

        }

      result.unsafeRunSync()

    }

    "search by event type" in {
      val result =
        for {
          storage <- DataCounterStorage.state[IO]()
          _ <- storage.add(makeMap(record(eventA, wordJ)))
          _ <- storage.add(makeMap(record(eventA, wordJ)))
          _ <- storage.add(makeMap(record(eventA, wordK)))
          foundO <- storage.get(eventA)
          found = foundO.getOrElse(Map.empty)
        } yield {

          found.size shouldBe 2
          found.get(wordJ) shouldBe Some(2)
          found.get(wordK) shouldBe Some(1)
          found.get(wordI) shouldBe None
        }

      result.unsafeRunSync()

    }

    "search by event type and word" in {
      val result =
        for {
          storage <- DataCounterStorage.state[IO]()
          _ <- storage.add(makeMap(record(eventA, wordJ)))
          _ <- storage.add(makeMap(record(eventA, wordJ)))
          _ <- storage.add(makeMap(record(eventA, wordK)))
          foundO <- storage.get(eventA, wordJ)
          found = foundO.getOrElse(-1)
        } yield {

          found shouldBe 2
        }

      result.unsafeRunSync()

    }
  }

}
