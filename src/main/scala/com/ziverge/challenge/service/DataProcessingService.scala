package com.ziverge.challenge.service

import cats.Applicative
import cats.effect.Concurrent.ops.toAllConcurrentOps
import cats.effect.{Concurrent, Sync, Timer}
import cats.syntax.all._
import com.ziverge.challenge.models.data.DataRecord
import com.ziverge.challenge.utils.DataUtils
import com.ziverge.challenge.utils.DataUtils._
import fs2.concurrent.Queue

import java.time.Instant
import scala.concurrent.duration.DurationInt

trait DataProcessingService[F[_]] {

  def process(inputStream: fs2.Stream[F, String], start: Instant, end: Instant): fs2.Stream[F, Unit]

}

object DataProcessingService {

  /* This implementation more durably in case application or processing failure.
    Because in data storage 2 on every batch we make a calculation and store all possible time window values
    in "database" wich assumed be real database, but current implemented as a map.

    Main disadvantage - sophisticated calculations
   */
  def storing[F[_]: Concurrent](dataService: DataService2[F], dataParser: DataParser[F, DataRecord]): F[DataProcessingService[F]] = Sync[F].delay {

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


  /*
   obviously should be in config
   */
  private val timeWindow = 1.hour
  private val qBuffer = 3700 // seconds + some buffer just in case

  /*
    This implementation is much simpler. It uses a scheduled jobs which focused on subtraction values from last hour state.

    Basically first stream add values to db and schedule task for subtract them executed by second stream

    It could be improved by introducing storage for messages that must be subtracted instead of making such dirty hack with .sleep
    I made it in order to show how we could achieve code simplification reducing "safety" OR we must add additional service tracking redundant data.

    Further main stream should be cut in 2 streams as well. One responsible for read and covert data, second - adding it to db

   */
  def reactive[F[_]: Concurrent: Timer](dataService: DataService[F], dataParser: DataParser[F, DataRecord]): F[DataProcessingService[F]] = {

    for {
      q <- Queue.bounded[F, FullMap](qBuffer)

    } yield {

      new DataProcessingService[F] {
        override def process(inputStream: fs2.Stream[F, String], start: Instant, end: Instant): fs2.Stream[F, Unit] = {
          val subStream = q.dequeue.evalMap(dataService.sub)
          val mainStream = {
            inputStream
              .evalMapChunk(x => dataParser.parse(x))
              .unNone
              .groupAdjacentBy(_.timestamp)
              .filter { case (data, _) => start.isBefore(data) && end.isAfter(data) }
              .evalMapChunk {
                case (_, chunk) =>
                  val c = DataUtils.count(chunk.toList)

                  val enqueueTask = (Timer[F].sleep(timeWindow) *> q.enqueue1(c)).start

                  dataService.add(c) *> enqueueTask *> Applicative[F].unit
              }
          }

          mainStream.concurrently(subStream)
        }

      }
    }
  }
}
