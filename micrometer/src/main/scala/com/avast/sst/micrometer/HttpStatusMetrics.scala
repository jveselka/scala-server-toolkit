package com.avast.sst.micrometer

import io.micrometer.core.instrument.{Counter, MeterRegistry}

import scala.collection.concurrent.TrieMap

private[micrometer] class HttpStatusMetrics(prefix: String, meterRegistry: MeterRegistry) {

  private val meters = TrieMap[Int, Counter](
    1 -> meterRegistry.counter(s"$prefix.status.1xx"),
    2 -> meterRegistry.counter(s"$prefix.status.2xx"),
    3 -> meterRegistry.counter(s"$prefix.status.3xx"),
    4 -> meterRegistry.counter(s"$prefix.status.4xx"),
    5 -> meterRegistry.counter(s"$prefix.status.5xx")
  )

  def recordHttpStatus(status: Int): Unit = {
    meters(status / 100).increment()
    meters.getOrElseUpdate(status, meterRegistry.counter(s"$prefix.status.$status")).increment()
  }

}
