package com.ziverge.challenge

import com.ziverge.challenge.models.data.{DataRecord, EventType, Word}
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

import java.time.Instant
import scala.util.Try

package object json {

  object Codecs {

    implicit val decodeInstant: Decoder[Instant] = Decoder.decodeLong.emapTry { l =>
      Try(Instant.ofEpochSecond(l))
    }

    implicit val eventTypeDecoder: Decoder[EventType] = deriveDecoder
    implicit val wordDecoder: Decoder[Word] = deriveDecoder

    implicit val recordEncoder: Decoder[DataRecord] = (c: HCursor) => {
      for {
        eventType <- c.get[String]("event_type")
        data <- c.get[String]("data")
        timestamp <- c.get[Instant]("timestamp")
      } yield DataRecord(
        eventType = EventType(eventType), data = Word(data), timestamp = timestamp
      )
    }

    implicit val wordKeyEncoder: KeyEncoder[Word] = (key: Word) => key.value

    implicit val eventTypeKeyEncoder: KeyEncoder[EventType] = (key: EventType) => key.value

    implicit val wordMapEncoder: Encoder[Map[Word, Int]] = (a: Map[Word, Int]) => {
      Json.obj(a.map { case (k, v) => k.value -> v.asJson }.toSeq: _*)
    }
    implicit val eventMapEncoder: Encoder[Map[EventType, Map[Word, Int]]] = (a: Map[EventType, Map[Word, Int]]) => {
      Json.obj(a.map { case (k, v) => k.value -> v.asJson }.toSeq: _*)
    }

  }
}