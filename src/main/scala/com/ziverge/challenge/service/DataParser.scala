package com.ziverge.challenge.service

import cats.effect.Sync
import cats.syntax.all._
import com.ziverge.challenge.json.Codecs._
import com.ziverge.challenge.logging.LoggingF
import com.ziverge.challenge.models.data.DataRecord
import io.circe
import io.circe.{Decoder, parser => circeParser}

trait DataParser[F[_], T] {

  def parse(string: String): F[Option[T]]

}

object DataParser {

  def record[F[_]: Sync](): F[DataParser[F, DataRecord]] =
    Sync[F].delay {
      new DataParser[F, DataRecord] with LoggingF {
        override def parse(string: String): F[Option[DataRecord]] = {

          val result: Either[circe.Error, DataRecord] = for {
            json <- circeParser.parse(string)
            rec <- Decoder[DataRecord].decodeJson(json)
          } yield rec

          result.fold(
            err => logger[F].debug(s"Unable to parse line: $string. Reason ${ err.toString }").as(Option.empty[DataRecord]),
            _.some.pure
          )

        }
      }
    }

}