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
 * Servicio para programar trabajos periódicos de notificaciones push.
 */
class NotificacionesScheduler(private val context: Context) {

    companion object {
        private const val WORK_NAME = "notificaciones_periodicas"
        // Android requiere mínimo 15 minutos para trabajos periódicos
        // WorkManager puede variar el tiempo de ejecución para optimizar batería
        private const val INTERVALO_MINUTOS = 15L // Mínimo permitido por Android
    }

    /**
     * Programa el worker de notificaciones para ejecutarse periódicamente.
     * Android requiere un mínimo de 15 minutos para trabajos periódicos.
     * WorkManager puede variar el tiempo de ejecución para optimizar la batería,
     * por lo que puede ejecutarse con menos frecuencia que el intervalo especificado.
     * 
     * El worker se ejecutará en segundo plano para generar notificaciones push
     * (recordatorios, alertas climáticas, ofertas) incluso cuando la app esté cerrada.
     * 
     * Nota: El worker contiene lógica para evitar generar notificaciones duplicadas,
     * por lo que ejecutarse más frecuentemente no causará spam de notificaciones.
     */
    fun programarNotificacionesPeriodicas() {
        // Crear restricciones: ejecutar en cualquier condición para maximizar las notificaciones
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No requiere conexión
            .setRequiresBatteryNotLow(false) // No requiere batería alta
            .setRequiresCharging(false) // No requiere estar cargando
            .setRequiresDeviceIdle(false) // No requiere que el dispositivo esté inactivo
            .build()

        // Crear trabajo periódico (mínimo 15 minutos requerido por Android)
        // WorkManager puede variar el tiempo de ejecución para optimizar la batería
        val trabajoPeriodico = PeriodicWorkRequestBuilder<NotificacionesWorker>(
            INTERVALO_MINUTOS,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("notificaciones") // Tag para identificarlo
            .build()

        // Programar el trabajo (si ya existe, lo reemplaza)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Reemplazar si ya existe para actualizar el intervalo
            trabajoPeriodico
        )
        
        android.util.Log.d("NotificacionesScheduler", "Notificaciones push programadas para ejecutarse periódicamente (cada ${INTERVALO_MINUTOS} minutos mínimo)")
    }

    /**
     * Cancela el trabajo periódico de notificaciones.
     */
    fun cancelarNotificacionesPeriodicas() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Ejecuta el trabajo de notificaciones de forma inmediata (una sola vez).
     * Útil para pruebas o para ejecutar manualmente.
     */
    fun ejecutarNotificacionesInmediatas() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val trabajoUnico = androidx.work.OneTimeWorkRequestBuilder<NotificacionesWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(trabajoUnico)
    }
}

