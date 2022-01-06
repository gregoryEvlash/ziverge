package com.ziverge.challenge

import cats.effect._
import com.ziverge.challenge.config.{ConfigLoader, ConfigProvider}
import com.ziverge.challenge.db.DataCounterStorage
import com.ziverge.challenge.http.{CounterRoutes, Gateway, SystemRoutes}
import com.ziverge.challenge.logging.LoggingF
import com.ziverge.challenge.service._
import fs2.concurrent.Queue

import scala.concurrent.ExecutionContext
import com.ziverge.challenge.utils.DataUtils._
/*
  dont remove it, i`m serious
 */
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.Implicits

object Application extends IOApp with LoggingF {

  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec: ExecutionContext = Implicits.global
    implicit val blocker: Blocker = Blocker.liftExecutionContext(ec)

    for {
      dates <- extractDates[IO](args)

      (start, end) = dates
      configs <- ConfigLoader.load[IO, ConfigProvider]
      q <- Queue.circularBuffer[IO, String](100)
      dataParser <- DataParser.record[IO]()
      dataStorage <- DataCounterStorage.state[IO]()
      dataService <- DataService.apply(dataStorage)
      dataProcessing <- DataProcessingService.reactive(dataService, dataParser)

      fileRoute <- CounterRoutes(dataService)
      systemRoutes <- SystemRoutes[IO]
      gateway <- Gateway(configs.httpConf, fileRoute, systemRoutes, blocker)

      reader = DataReader.inputStream[IO](blocker).through(q.enqueue)
      processingStream = dataProcessing.process(q.dequeue, start, end)

      i <- reader.compile.drain.start
      p <- processingStream.compile.drain.start
      _ <- gateway.run.compile.drain.guarantee(i.cancel *> p.cancel)
    } yield ExitCode.Success

  }


}
