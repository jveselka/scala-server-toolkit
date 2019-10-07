package com.avast.sst.micrometer

import cats.effect.Sync
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.{ClassLoaderMetrics, JvmGcMetrics, JvmMemoryMetrics, JvmThreadMetrics}
import io.micrometer.core.instrument.binder.system.ProcessorMetrics

object MicrometerJvmModule {

  def make[F[_]: Sync](registry: MeterRegistry): F[Unit] = {
    Sync[F].delay {
      new ClassLoaderMetrics().bindTo(registry)
      new JvmMemoryMetrics().bindTo(registry)
      new JvmGcMetrics().bindTo(registry)
      new ProcessorMetrics().bindTo(registry)
      new JvmThreadMetrics().bindTo(registry)
    }
  }

}
