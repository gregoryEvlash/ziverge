package com.ziverge.challenge.db

import cats.Monad
import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.mtl.Stateful
import cats.syntax.all._
import com.ziverge.challenge.models.data.{EventType, Word}
import com.ziverge.challenge.utils.DataUtils.{FullMap, negateSMF, smF}


trait DataCounterStorage[F[_]] {

  def add(snapshot: FullMap): F[Unit]

  def sub(snapshot: FullMap): F[Unit]

  def fetchALL: F[FullMap]

  def get(event: EventType): F[Option[Map[Word, Int]]]

  def get(event: EventType, word: Word): F[Option[Int]]

}

object DataCounterStorage {

  private class RefStateful[F[_]: Monad, S](ref: Ref[F, S]) extends Stateful[F, S] {
    override val monad:             Monad[F] = implicitly
    override def modify(f: S => S): F[Unit]  = ref.update(f)
    override def get:               F[S]     = ref.get
    override def set(s: S):         F[Unit]  = ref.set(s)
  }

  def state[F[_]: Concurrent](): F[DataCounterStorage[F]] = {

    for {
      x <- Ref[F].of(Map.empty[EventType, Map[Word, Int]])
      m = new RefStateful[F, FullMap](x)
    } yield {
      new DataCounterStorage[F] {

        def add(snapshot: FullMap): F[Unit] =
          m.modify { m =>
            smF.combine(m, snapshot)
          }

        def sub(snapshot: FullMap): F[Unit] =
          m.modify { m =>
            negateSMF.combine(m, snapshot)
          }.as(())

        override def fetchALL: F[FullMap] = m.get

        override def get(event: EventType): F[Option[Map[Word, Int]]] = m.get.map(_.get(event))

        override def get(event: EventType, word: Word): F[Option[Int]] = get(event).map(_.flatMap(_.get(word)))

      }
    }

  }

}
