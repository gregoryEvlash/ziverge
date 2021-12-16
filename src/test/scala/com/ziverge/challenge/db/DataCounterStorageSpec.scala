package com.ziverge.challenge.db

import cats.effect.IO
import cats.implicits.toTraverseOps
import com.ziverge.challenge.TestDataUtil
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DataCounterStorageSpec extends AnyWordSpec with Matchers with TestDataUtil {

  "DataCounterStorage" should {

    "count events and words" in {
      val result =
        for {
          storage <- DataCounterStorage[IO]()
          _ <- testData.traverse(x => storage.inc(x.eventType, x.data))
          _ <- storage.inc(eventA, wordI)
          _ <- storage.inc(eventA, wordI)

          found <- storage.all
        } yield {

          found.size shouldBe 3
          found.get(eventA).flatMap(_.get(wordI)) shouldBe Some(3)
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
          storage <- DataCounterStorage[IO]()
          _ <- storage.inc(eventA, wordJ)
          _ <- storage.inc(eventA, wordJ)
          _ <- storage.inc(eventA, wordK)
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
          storage <- DataCounterStorage[IO]()
          _ <- storage.inc(eventA, wordJ)
          _ <- storage.inc(eventA, wordJ)
          _ <- storage.inc(eventA, wordK)
          foundO <- storage.get(eventA, wordJ)
          found = foundO.getOrElse(-1)
        } yield {

          found shouldBe 2
        }

      result.unsafeRunSync()

    }


  }

}
