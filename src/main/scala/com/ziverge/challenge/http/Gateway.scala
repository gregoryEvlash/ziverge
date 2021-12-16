package com.ziverge.challenge.http

import cats.effect.{Blocker, ConcurrentEffect, ExitCode, Sync, Timer}
import com.ziverge.challenge.config.HttpConf
import com.ziverge.challenge.logging.LoggingF
import io.circe.generic.AutoDerivation
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s._

class Gateway[F[_]: ConcurrentEffect: Timer](settings: HttpConf, counterRoutes: CounterRoutes[F], systemRoutes: SystemRoutes[F], blocker: Blocker)
    extends AutoDerivation
    with Http4sDsl[F]
    with LoggingF {

  private val endpoints = counterRoutes.routes orElse systemRoutes.routes

  def httpApp: HttpApp[F] = HttpRoutes.of[F](endpoints).orNotFound

  def run: fs2.Stream[F, ExitCode] = {
    BlazeServerBuilder[F](blocker.blockingContext)
      .bindHttp(settings.port, settings.host)
      .withHttpApp(httpApp)
      .serve

  }

}

object Gateway {
  def apply[F[_]: ConcurrentEffect : Timer]
  (settings: HttpConf, counterRoutes: CounterRoutes[F], systemRoutes: SystemRoutes[F], blocker: Blocker)
  (implicit F: Sync[F]): F[Gateway[F]] =
    F.delay(new Gateway(settings, counterRoutes, systemRoutes, blocker: Blocker))
}
