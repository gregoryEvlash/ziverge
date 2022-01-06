package com.ziverge.challenge.service

import cats.effect.Sync
import cats.implicits.toFunctorOps
import com.ziverge.challenge.db.DataCounterStorage2
import com.ziverge.challenge.models._
import com.ziverge.challenge.models.data.DataRecord
import com.ziverge.challenge.utils.DataUtils.makeMap

import java.time.Instant

trait DataService2[F[_]] {

  def count(record: DataRecord): F[Unit]

  def fetchAllInfo: F[ZivergeServiceResponse]

}

object DataService2 {

  def apply[F[_]: Sync](storage: DataCounterStorage2[F]): F[DataService2[F]] = Sync[F].delay {
    new DataService2[F] {

      override def count(record: DataRecord): F[Unit] =
        storage.store(record.timestamp -> makeMap(record))

      override def fetchAllInfo: F[ZivergeServiceResponse] =
        storage.fetchALL(Instant.now).map(x => Right(FullInfo(x)))

    }
  }

}
