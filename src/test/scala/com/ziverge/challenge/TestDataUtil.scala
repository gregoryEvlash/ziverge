package com.ziverge.challenge

import cats.effect.{ContextShift, IO}
import com.ziverge.challenge.models.data.{DataRecord, EventType, Word}
import io.circe.{Json, parser}

import java.time.Instant
import scala.concurrent.ExecutionContext.global


trait TestDataUtil {

  val testJson: Json = parser.parse("""{ "event_type": "AAA", "data": "III", "timestamp": 1639505602 }""").toOption.get

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  val eventA: EventType = EventType("A")
  val eventB: EventType = EventType("B")
  val eventC: EventType = EventType("C")
  val eventX: EventType = EventType("X")

  val wordI: Word = Word("I")
  val wordJ: Word = Word("J")
  val wordK: Word = Word("K")
  val wordZ: Word = Word("Z")

  def record(eventType: EventType, word: Word) = DataRecord(eventType, word, Instant.now())

  val recAI = DataRecord(eventA, wordI, Instant.now())
  val recCK = DataRecord(eventC, wordK, Instant.now())

  val testData = List(
    recAI,
    record(eventB, wordJ),
    recCK,
    record(eventA, wordK),
  )


}
