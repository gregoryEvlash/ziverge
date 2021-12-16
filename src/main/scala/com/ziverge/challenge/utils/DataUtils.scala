package com.ziverge.challenge.utils

import cats.Eq
import cats.effect.Sync
import cats.syntax.all._

import java.time.Instant

object DataUtils {

  implicit val instantEq: Eq[Instant] = new Eq[Instant] {
    override def eqv(x: Instant, y: Instant): Boolean = x.equals(y)
  }

  def toInstant[F[_]: Sync](s: String): F[Instant] =
    Sync[F].delay(Instant.parse(s)).adaptErr(x => new Throwable(s"Provided date format is wrong: ($x)"))


  def extractDates[F[_]: Sync](args: List[String]): F[(Instant, Instant)] = {
    for{
      s <- Sync[F].delay(args.head).adaptErr(_ => new Throwable("No start date arg provided"))
      e <- Sync[F].delay(args.tail.head).adaptErr(_ => new Throwable("No end date arg provided"))
      start <- toInstant(s)
      end <- toInstant(e)
    } yield start -> end
  }

}
