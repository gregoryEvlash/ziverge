package com.ziverge.challenge.models


import java.time.Instant

package object data {

  final case class EventType(value: String) extends AnyVal
  final case class Word(value: String) extends AnyVal

  case class DataRecord(
    eventType: EventType,
    data: Word,
    timestamp: Instant
  )

}
