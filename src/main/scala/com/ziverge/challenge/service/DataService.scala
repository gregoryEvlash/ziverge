package com.ziverge.challenge.service

import cats.effect.Sync
import cats.implicits.toFunctorOps
import com.ziverge.challenge.db.DataCounterStorage
import com.ziverge.challenge.models._
import com.ziverge.challenge.models.data.{EventType, Word}
import com.ziverge.challenge.utils.DataUtils.FullMap

trait DataService[F[_]] {

  def add(snapshot: FullMap): F[Unit]

  def sub(snapshot: FullMap): F[Unit]

  def fetchAllInfo: F[ZivergeServiceResponse]

  def getForEvent(eventType: EventType): F[ZivergeServiceResponse]

  def getFor(eventType: EventType, word: Word): F[ZivergeServiceResponse]

}

object DataService {

  def apply[F[_]: Sync](storage: DataCounterStorage[F]): F[DataService[F]] = Sync[F].delay {
    new DataService[F] {

      override def add(snapshot: FullMap): F[Unit] =
        storage.add(snapshot)

      override def sub(snapshot: FullMap): F[Unit] =
        storage.sub(snapshot)

      override def fetchAllInfo: F[ZivergeServiceResponse] =
        storage.fetchALL.map(x => Right(FullInfo(x)))

      override def getForEvent(eventType: EventType): F[ZivergeServiceResponse] =
        storage.get(eventType).map {
          case Some(x) => Right(EventInfo(x))
          case None    => Left(NotFoundEvent(eventType))
        }

      override def getFor(eventType: EventType, word: Word): F[ZivergeServiceResponse] =
        storage.get(eventType, word).map {
          case Some(x) => Right(EventWordInfo(x))
          case None    => Left(NotFoundEventWord(eventType, word))
        }
    }
  }

}
