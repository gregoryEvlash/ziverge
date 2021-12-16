package com.ziverge.challenge

import com.ziverge.challenge.models.data.{EventType, Word}

package object models {

  sealed trait CounterInfo

  sealed trait ZivergeServiceError
  sealed trait ZivergeServiceResult extends CounterInfo

  case class FullInfo(info: Map[EventType, Map[Word, Int]]) extends ZivergeServiceResult
  case class EventInfo(info: Map[Word, Int]) extends ZivergeServiceResult
  case class EventWordInfo(info: Int) extends ZivergeServiceResult

  type ZivergeServiceResponse = Either[ZivergeServiceError, ZivergeServiceResult]

  case class NotFoundEvent(event: EventType) extends ZivergeServiceError
  case class NotFoundEventWord(event: EventType, word: Word) extends ZivergeServiceError

  case class CustomError(error: String) extends ZivergeServiceError

}
