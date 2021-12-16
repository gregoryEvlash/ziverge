package com.ziverge.challenge.service

import cats.effect.{Blocker, ContextShift, Sync}
import fs2.{io, text}

import java.nio.file.Paths

object DataReader {

  def inputStream[F[_]: Sync: ContextShift](blocker: Blocker): fs2.Stream[F, String] =
    io.file.readAll[F](Paths.get("buffer"), blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
}
