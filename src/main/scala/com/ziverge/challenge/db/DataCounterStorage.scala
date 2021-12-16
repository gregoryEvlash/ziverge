package com.ziverge.challenge.db

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.ziverge.challenge.models.data.{EventType, Word}


trait DataCounterStorage[F[_]] {

  def inc(key: EventType, word: Word): F[Unit]

  def get(event: EventType): F[Option[Map[Word, Int]]]

  def get(event: EventType, word: Word): F[Option[Int]]

  def all: F[Map[EventType, Map[Word, Int]]]

}

object DataCounterStorage {

  type WordCounter = Map[Word, Int]
  type FullMap = Map[EventType, WordCounter]

  def apply[F[_]: Concurrent](): F[DataCounterStorage[F]] = {

    for {
      keyMap <- Ref[F].of(Map.empty[EventType, WordCounter])
    } yield {
      new DataCounterStorage[F] {

        // todo comment about triemap and mutations
        override def inc(eventType: EventType, word: Word): F[Unit] = {
          keyMap.modify { map =>
            val wordMap = map.getOrElse(eventType, Map.empty[Word, Int])
            val countValue = wordMap.getOrElse(word, 0) + 1

            val newWordMap = (wordMap - word) ++ Map(word -> countValue)

            val newEventMap = (map - eventType) ++ Map(eventType -> newWordMap)

            newEventMap -> map
          }
        }

        override def get(event: EventType): F[Option[Map[Word, Int]]] = keyMap.get.map(_.get(event))

        override def get(event: EventType, word: Word): F[Option[Int]] = get(event).map(_.flatMap(_.get(word)))

        override def all: F[Map[EventType, Map[Word, Int]]] = keyMap.get.map(_.view.mapValues(_.toMap).toMap)
      }
    }

  }

}
