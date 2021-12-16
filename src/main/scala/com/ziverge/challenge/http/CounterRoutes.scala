package com.ziverge.challenge.http

import cats.effect.ConcurrentEffect
import cats.implicits._
import com.ziverge.challenge.logging.LoggingF
import com.ziverge.challenge.models.data.{EventType, Word}
import com.ziverge.challenge.service.DataService
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response}

class CounterRoutes[F[_]](service: DataService[F])
  (implicit val F: ConcurrentEffect[F])
  extends Http4sDsl[F] with RestHelper[F] with LoggingF {

  private final val COUNT = "count"
  private final val ALL = "all"

  type Route = PartialFunction[Request[F], F[Response[F]]]

  def all: Route = {
    case _ @ GET -> Root / COUNT / ALL =>
      logger.info("Fetching all counted info") *> handle(service.fetchAllInfo)
  }

  def event: Route = {
    case _ @ GET -> Root / COUNT / event =>
      logger.info(s"Fetching info for event type $event") *>
        handle(service.getForEvent(EventType(event)))
  }

  def word: Route = {
    case _ @ GET -> Root / COUNT / event / word =>
      logger.info(s"Fetching info for event type: $event and word: $word") *>
        handle(service.getFor(EventType(event), Word(word)))
  }

  def routes: Route = List(all, word, event).reduce(_ orElse _)
}

object CounterRoutes {
  def apply[F[_]](service: DataService[F])(implicit F: ConcurrentEffect[F]): F[CounterRoutes[F]] = F.delay(new CounterRoutes(service))
}


