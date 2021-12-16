package com.ziverge.challenge.service

import cats.effect.IO
import cats.implicits.toTraverseOps
import com.ziverge.challenge.TestDataUtil
import com.ziverge.challenge.db.DataCounterStorage
import com.ziverge.challenge.models.{FullInfo, NotFoundEvent}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Exceptional, Succeeded}

class DataServiceSpec extends AnyWordSpec with Matchers with TestDataUtil {

  "DataServiceSpec" should {

    "fetch all data" in {
      val result =
        for {
          storage <- DataCounterStorage[IO]()
          service <- DataService[IO](storage)
          _ <- testData.traverse(x => storage.inc(x.eventType, x.data))
          result <- service.fetchAllInfo

        } yield {
          result match {
            case Right(FullInfo(found)) =>
              found.size shouldBe 3
              found.get(eventA).flatMap(_.get(wordI)) shouldBe Some(1)
              found.get(eventA).flatMap(_.get(wordK)) shouldBe Some(1)
              found.get(eventB).flatMap(_.get(wordJ)) shouldBe Some(1)
              found.get(eventC).flatMap(_.get(wordK)) shouldBe Some(1)
              found.get(eventX) shouldBe None
            case _                      => Exceptional.apply(new Throwable("wrong type result"))
          }
        }

      result.unsafeRunSync()
    }

    "respond typed in case not found event" in {
      val result =
        for {
          storage <- DataCounterStorage[IO]()
          service <- DataService[IO](storage)
          _ <- testData.traverse(x => storage.inc(x.eventType, x.data))
          result <- service.getForEvent(eventX)
        } yield {
          result match {
            case Left(_: NotFoundEvent) => Succeeded
            case x                      => Exceptional.apply(new Throwable(s"wrong result type $x"))
          }
        }

      result.unsafeRunSync()
    }

    "respond typed in case not found event and word" in {
      val result =
        for {
          storage <- DataCounterStorage[IO]()
          service <- DataService[IO](storage)
          _ <- testData.traverse(x => storage.inc(x.eventType, x.data))
          result <- service.getFor(eventA, wordZ)
        } yield {
          result match {
            case Left(_: NotFoundEvent) => Succeeded
            case x                      => Exceptional.apply(new Throwable(s"wrong result type $x"))
          }
        }

      result.unsafeRunSync()
    }

  }
}