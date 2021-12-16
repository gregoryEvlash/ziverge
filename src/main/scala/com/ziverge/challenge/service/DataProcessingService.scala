package com.ziverge.challenge.service

import cats.effect.{Concurrent, Sync}
import cats.implicits.toTraverseOps
import cats.syntax.all._
import com.ziverge.challenge.models.data.DataRecord
import com.ziverge.challenge.utils.DataUtils._

import java.time.Instant

trait DataProcessingService[F[_]] {

  def process(inputStream: fs2.Stream[F, String], start: Instant, end: Instant): fs2.Stream[F, Unit]

}

object DataProcessingService {

  def apply[F[_]: Concurrent](dataService: DataService[F], dataParser: DataParser[F, DataRecord]): F[DataProcessingService[F]] = Sync[F].delay {

    (inputStream: fs2.Stream[F, String], start: Instant, end: Instant) =>
      inputStream
        .evalMapChunk(x => dataParser.parse(x))
        .unNone
        .groupAdjacentBy(_.timestamp)
        .filter { case (data, _) => start.isBefore(data) && end.isAfter(data) }
        .evalMapChunk {
          case (_, chunk) => chunk.traverse(dataService.count).as(())
        }

  }
}
