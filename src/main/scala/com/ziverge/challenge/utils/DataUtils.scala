package com.ziverge.challenge.utils

import cats.{Applicative, Eq}
import cats.effect.Sync
import cats.syntax.all._

import java.time.Instant

object DataUtils {

  implicit val instantEq: Eq[Instant] = (x: Instant, y: Instant) => x.equals(y)

  def toInstant[F[_]: Sync](s: String): F[Instant] =
    Sync[F].delay(Instant.parse(s)).adaptErr(x => new Throwable(s"Provided date format is wrong: ($x)"))


  def extractDates[F[_]: Sync](args: List[String]): F[(Instant, Instant)] = {
    for{
      s <- Sync[F].delay(args.head).adaptErr(_ => new Throwable("No start date arg provided"))
      e <- Sync[F].delay(args.tail.head).adaptErr(_ => new Throwable("No end date arg provided"))
      start <- toInstant(s)
      end <- toInstant(e)
      _ <- Sync[F].raiseWhen(start.isAfter(end) || end.isBefore(Instant.now()))(new Throwable(s"Wrong date range provided start $start end $end"))
    } yield start -> end
  }

}
