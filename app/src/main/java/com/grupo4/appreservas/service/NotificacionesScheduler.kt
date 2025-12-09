package com.grupo4.appreservas.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.grupo4.appreservas.worker.NotificacionesWorker
import java.util.concurrent.TimeUnit

/**
 * Scheduler para programar trabajos periódicos de notificaciones.
 * Equivalente a NotificacionesScheduler del diagrama UML.
 */
class NotificacionesScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Programa el trabajo periódico de notificaciones.
     * Se ejecuta cada 6 horas para verificar recordatorios, alertas climáticas y ofertas.
     */
    fun programarNotificaciones() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NotificacionesWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "notificaciones_periodicas",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancela el trabajo periódico de notificaciones.
     */
    fun cancelarNotificaciones() {
        workManager.cancelUniqueWork("notificaciones_periodicas")
    }
}

