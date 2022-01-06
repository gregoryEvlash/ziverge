package com.ziverge.challenge.db

import cats.{Parallel, Semigroup}
import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.ziverge.challenge.db.DataCounterStorage2.Snapshot
import com.ziverge.challenge.models.data.{EventType, Word}
import com.ziverge.challenge.utils.DataUtils.{FullMap, WordCounter, negateSMF, smF}

import java.time.{Duration, Instant}


trait DataCounterStorage2[F[_]] {

  def store(snapshot: Snapshot): F[Unit]

  def fetchALL(instant: Instant): F[FullMap]

}

object DataCounterStorage2 {

  type Snapshot = (Instant, FullMap)

  /*
    As a storage we use a list of events. Event represent timestamp and snapshot of counted events & words.
    On each insert we append event to the beginning, meantime reduce tail to prevent data leakage.
    On request we go through all elements and combine them in parallel.

    Store operation would be quiet fast. But read operation might be heavy because you need to calculate state at request time.

   */
  def actionBased[F[_]: Concurrent: Parallel](): F[DataCounterStorage2[F]] = {
    for {
      x <- Ref[F].of(List.empty[Snapshot])
    } yield {
      new DataCounterStorage2[F] {

        override def store(snapshot: Snapshot): F[Unit] = {
          x.modify { s =>
            val hourAgo = snapshot._1.minus(Duration.ofHours(1))
            val timeWindowReduced = s.takeWhile { case (i, _) => i isAfter hourAgo }

            (snapshot :: timeWindowReduced) -> s
          }
        }

        override def fetchALL(instant: Instant): F[FullMap] = {
          x.get.flatMap { l =>
            implicit val semigroup: Semigroup[FullMap] = smF
            val hourAgo = instant.minus(Duration.ofHours(1))

            l.parFoldMapA {
              case (i, m) if i isAfter hourAgo => m.pure
              case _                           => Map.empty[EventType, WordCounter].pure
            }
          }
        }

      }
    }
  }


  /*
    The main idea of this approach is to store state of count for the last hour window.

    Which means on each insert:
       we take the received counted snapshot from blackbox
       find changes for that last hour (last stored state minus state 1 hour ago (represent 1 hour ago state of time window))
       add current snapshot from blackbox to this hour diff

    For fetch we just need to take latest and subtract latest out of time window

   */
  def lastHourStateBased[F[_]: Concurrent: Parallel](): F[DataCounterStorage2[F]] = {

    for {
      x <- Ref[F].of(List.empty[Snapshot])
    } yield {
      new DataCounterStorage2[F] {

        override def store(snapshot: Snapshot): F[Unit] = {
          // This could be optimized, for instance instead of using immutable scala.List we could use array buffer,
          // plus store pointer on last elements. Use list builders etc. But i want to simplify code for better understanding main idea.
          x.modify { s =>

            val (snapTime, currentSnapshot) = snapshot

            val hourAgo = snapTime.minus(Duration.ofHours(1))

            // find latest hour calculation state
            val latestValueWithinTimeWindow = s.collectFirst {
              case (i, m) if i isAfter hourAgo => m
            }.getOrElse(Map.empty)

            // find latest state out of time window
            val latestOutOfTimeWindow = s.collectFirst {
              case x @ (i, _) if i isBefore hourAgo => x
            }

            // represent how changed state for last hour
            val previousHourDiff = negateSMF.combine(
              latestValueWithinTimeWindow,
              latestOutOfTimeWindow.map(_._2).getOrElse(Map.empty)
            )

            // combine new event snapshot with previous hour state
            val newHourState = smF.combine(currentSnapshot, previousHourDiff)

            // make clean of unused historical data to prevent memory leakage
            val reducedTail = s.takeWhile { case (i, _) => i isAfter hourAgo }

            // we must keep track earliest state out of time window in order to have a proper data calculation
            val reducedIncludingEarliest = reducedTail ::: List.from(latestOutOfTimeWindow)

            (snapTime -> newHourState :: reducedIncludingEarliest) -> s
          }
        }

        def fetchALL(instant: Instant): F[FullMap] = {
          x.get.map { l =>
            val hourAgo = instant.minus(Duration.ofHours(1))

            val latest = l.collectFirst {
              case (i, m) if i isAfter hourAgo => m
            }.getOrElse(Map.empty)

            val earliest = l.collectFirst {
              case (i, m) if i isBefore hourAgo => m
            }.getOrElse(Map.empty)

            negateSMF.combine(latest, earliest)
          }
        }
      }
    }

  }
}


