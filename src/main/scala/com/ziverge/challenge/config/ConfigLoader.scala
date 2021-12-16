package com.ziverge.challenge.config

import cats.effect.Sync
import pureconfig.{ConfigReader, ConfigSource, Derivation}
import cats.implicits._
import pureconfig.generic.auto._

object ConfigLoader {
  def load[F[_], S](implicit S: Derivation[ConfigReader[S]], F: Sync[F]): F[S] =
    F.delay(ConfigSource.default.load[S]).flatMap {
      case Right(settings) => F.pure(settings)
      case Left(e) =>
        F.raiseError(
          new IllegalStateException(
            s"Failed to read config ${e.toList.mkString(",")}"
          )
        )
    }
}
