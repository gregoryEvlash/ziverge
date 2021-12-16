package com.ziverge.challenge.http

import cats.effect.Sync
import com.ziverge.challenge.logging.LoggingF
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response}


class SystemRoutes[F[_]](implicit F: Sync[F]) extends Http4sDsl[F] with LoggingF{

  private final val HEALTHCHECK = "heartbeat"

  type Route = PartialFunction[Request[F], F[Response[F]]]

  def heartBeat: Route = {
    case GET -> Root / HEALTHCHECK => Ok()
  }

  def routes: Route = List(heartBeat).reduce(_ orElse _)

}

object SystemRoutes {
  def apply[F[_]](implicit F: Sync[F]): F[SystemRoutes[F]] =
    F.delay(new SystemRoutes)
}



