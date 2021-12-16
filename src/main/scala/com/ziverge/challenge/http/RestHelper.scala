package com.ziverge.challenge.http

import cats.effect.Sync
import cats.implicits._
import com.ziverge.challenge.logging.LoggingF
import com.ziverge.challenge.models._
import org.http4s.Response
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.impl.Statuses
import com.ziverge.challenge.json.Codecs._

object RestHelper extends Statuses with LoggingF {

  def result[F[_]]: PartialFunction[ZivergeServiceResult, Response[F]] = {
    case FullInfo(info)      =>
      Response[F](Ok).withEntity(info)
    case EventInfo(info)     =>
      Response[F](Ok).withEntity(info)
    case EventWordInfo(info) =>
      Response[F](Ok).withEntity(info)
  }

  def error[F[_]]: PartialFunction[ZivergeServiceError, Response[F]] = {
    case NotFoundEvent(event)           => Response[F](NotFound).withEntity(s"No info found for ${ event.value }")
    case NotFoundEventWord(event, word) => Response[F](NotFound).withEntity(s"No info found for event $event and word ${ word.value }")
  }

  def handle[F[_]: Sync](value: F[ZivergeServiceResponse]): F[Response[F]] = {
    (value <* logger.debug("Prepare response"))
      .map(_.fold(
        e => error(e),
        v => result(v)
      ))
  }

}
