package com.grupo4.appreservas.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.grupo4.appreservas.controller.ControlNotificaciones
import com.grupo4.appreservas.repository.RepositorioNotificaciones
import com.grupo4.appreservas.repository.RepositorioOfertas
import com.grupo4.appreservas.repository.RepositorioClima
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.service.NotificacionesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker que ejecuta las tareas de notificaciones en segundo plano.
 * Se ejecuta periódicamente para verificar y generar notificaciones push.
 */
class NotificacionesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("NotificacionesWorker", "Iniciando verificación de notificaciones...")

            // Obtener todos los usuarios turistas (rolId = 2)
            val dbHelper = DatabaseHelper(applicationContext)
            val usuarios = dbHelper.obtenerTodosLosUsuariosTuristas()

            if (usuarios.isEmpty()) {
                Log.d("NotificacionesWorker", "No hay usuarios turistas para notificar")
                return@withContext Result.success()
            }

            // Inicializar servicios y controlador
            val repositorioNotificaciones = RepositorioNotificaciones(applicationContext)
            val repositorioOfertas = RepositorioOfertas(applicationContext)
            val repositorioClima = RepositorioClima(applicationContext)
            val notificacionesService = NotificacionesService(applicationContext)

            val controlNotificaciones = ControlNotificaciones(
                repositorioNotificaciones,
                repositorioOfertas,
                repositorioClima,
                notificacionesService,
                applicationContext
            )

            // Procesar notificaciones para cada usuario
            usuarios.forEach { usuarioId ->
                try {
                    // Generar recordatorios de tours próximos
                    controlNotificaciones.generarRecordatoriosTours(usuarioId)
                    Log.d("NotificacionesWorker", "Recordatorios generados para usuario $usuarioId")

                    // Detectar cambios climáticos
                    controlNotificaciones.detectarCambioClimatico(usuarioId)
                    Log.d("NotificacionesWorker", "Alertas climáticas verificadas para usuario $usuarioId")

                    // Generar ofertas de último minuto
                    controlNotificaciones.generarOfertaUltimoMinuto(usuarioId)
                    Log.d("NotificacionesWorker", "Ofertas verificadas para usuario $usuarioId")
                } catch (e: Exception) {
                    Log.e("NotificacionesWorker", "Error al procesar notificaciones para usuario $usuarioId: ${e.message}", e)
                }
            }

            Log.d("NotificacionesWorker", "Verificación de notificaciones completada")
            Result.success()
        } catch (e: Exception) {
            Log.e("NotificacionesWorker", "Error en worker de notificaciones: ${e.message}", e)
            Result.retry() // Reintentar si hay un error
        }
    }
}

