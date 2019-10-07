package com.avast.sst.micrometer.interop

import java.util.concurrent.TimeUnit

import cats.effect.syntax.bracket._
import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.avast.sst.micrometer.HttpStatusMetrics
import io.micrometer.core.instrument.MeterRegistry
import org.http4s.Response

import scala.language.higherKinds

class Http4sRouteMetrics[F[_]: Sync](meterRegistry: MeterRegistry, clock: Clock[F]) {

  private val F = Sync[F]

  def wrap(name: String)(route: => F[Response[F]]): F[Response[F]] = {
    val prefix = s"http.$name"
    val activeRequests = meterRegistry.counter(s"$prefix.active-requests")
    val timer = meterRegistry.timer(s"$prefix.total-time")
    val httpStatusCodes = new HttpStatusMetrics(prefix, meterRegistry)
    for {
      start <- clock.monotonic(TimeUnit.NANOSECONDS)
      response <- F.delay(activeRequests.increment())
                   .bracket { _ =>
                     route.flatTap(response => F.delay(httpStatusCodes.recordHttpStatus(response.status.code)))
                   } { _ =>
                     for {
                       time <- computeTime(start)
                       _ <- F.delay(activeRequests.increment(-1))
                       _ <- F.delay(timer.record(time, TimeUnit.NANOSECONDS))
                     } yield ()
                   }
    } yield response
  }

  private def computeTime(start: Long): F[Long] = clock.monotonic(TimeUnit.NANOSECONDS).map(_ - start)

}
