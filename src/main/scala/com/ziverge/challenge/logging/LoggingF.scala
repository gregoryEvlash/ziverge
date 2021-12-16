package com.ziverge.challenge.logging

import cats.effect.Sync
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait LoggingF {
  protected def logger[F[_]: Sync]: LoggerF[F] = new LoggerF[F](getClass)
}

class LoggerF[F[_]: Sync](clazz: Class[_]) {

  private val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](clazz)

  def info(message: String): F[Unit] = logger.info(message)

  def warn(message: String): F[Unit] = logger.warn(message)

  def error(message: String): F[Unit] = logger.error(message)

  def debug(message: => String): F[Unit] =  logger.debug(message)

}
