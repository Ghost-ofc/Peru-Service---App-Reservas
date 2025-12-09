package com.grupo4.appreservas.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.grupo4.appreservas.modelos.TipoNotificacion
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.service.NotificacionesService
import java.text.SimpleDateFormat
import java.util.*

/**
 * Worker que se ejecuta en segundo plano para generar y enviar notificaciones.
 * Equivalente a NotificacionesWorker del diagrama UML.
 */
class NotificacionesWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val repository = PeruvianServiceRepository.getInstance(context)
    private val notificacionesService = NotificacionesService(context)

    override fun doWork(): Result {
        return try {
            // Obtener todos los usuarios turistas
            val usuariosTuristas = repository.obtenerTodosLosUsuariosTuristas()

            for (usuario in usuariosTuristas) {
                // 1. Generar recordatorios de horario
                generarRecordatoriosHorario(usuario.usuarioId)

                // 2. Detectar y enviar alertas climáticas
                generarAlertasClimaticas(usuario.usuarioId)

                // 3. Generar ofertas de último minuto
                generarOfertasUltimoMinuto(usuario.usuarioId)

                // 4. Enviar encuestas automáticas para tours finalizados (HU-009)
                generarEncuestasAutomaticas(usuario.usuarioId)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    /**
     * Genera recordatorios de horario para tours próximos a iniciar.
     */
    private fun generarRecordatoriosHorario(usuarioId: Int) {
        // Obtener reservas confirmadas del usuario
        val reservas = repository.obtenerReservasPorUsuario(usuarioId)
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val horaActual = Calendar.getInstance()

        for (reserva in reservas) {
            if (reserva.estaConfirmado() && reserva.tourId.isNotEmpty()) {
                val tour = repository.obtenerTourPorId(reserva.tourId)
                tour?.let {
                    // Verificar si el tour es hoy
                    if (it.fecha == fechaHoy) {
                        // Parsear la hora del tour
                        val partesHora = it.hora.split(":")
                        if (partesHora.size >= 2) {
                            val horaTour = partesHora[0].toIntOrNull() ?: 0
                            val minutoTour = partesHora[1].toIntOrNull() ?: 0
                            
                            // Crear recordatorio 2 horas antes
                            val horaRecordatorio = horaTour - 2
                            if (horaRecordatorio >= 0 && horaActual.get(Calendar.HOUR_OF_DAY) == horaRecordatorio) {
                                // Verificar si ya existe un recordatorio para este tour
                                val notificaciones = repository.obtenerRecordatorios(usuarioId)
                                val yaExiste = notificaciones.any { 
                                    it.tourId == reserva.tourId && 
                                    it.tipo == TipoNotificacion.RECORDATORIO &&
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.fechaCreacion) == fechaHoy
                                }
                                
                                if (!yaExiste) {
                                    repository.crearNotificacionRecordatorio(
                                        usuarioId,
                                        it.tourId,
                                        it.nombre,
                                        it.hora,
                                        it.puntoEncuentro
                                    )
                                    
                                    // Enviar notificación push
                                    val notificacion = repository.obtenerRecordatorios(usuarioId)
                                        .firstOrNull { 
                                            it.tourId == reserva.tourId && 
                                            it.tipo == TipoNotificacion.RECORDATORIO 
                                        }
                                    notificacion?.let { notif ->
                                        notificacionesService.mostrarNotificacion(notif, usuarioId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Genera alertas climáticas si se detectan cambios.
     */
    private fun generarAlertasClimaticas(usuarioId: Int) {
        // Obtener ubicación del usuario (por defecto Cusco)
        val ubicacion = "Cusco"
        
        // Detectar cambios climáticos
        val hayCambio = repository.obtenerCondicionesYDetectarCambio(ubicacion)
        
        if (hayCambio) {
            // Verificar si ya existe una alerta climática reciente (últimas 6 horas)
            val notificaciones = repository.obtenerRecordatorios(usuarioId)
            val ahora = Date()
            val hace6Horas = Date(ahora.time - 6 * 60 * 60 * 1000)
            
            val yaExiste = notificaciones.any {
                it.tipo == TipoNotificacion.ALERTA_CLIMATICA &&
                it.fechaCreacion.after(hace6Horas)
            }
            
            if (!yaExiste) {
                val condiciones = "Lluvia intensa detectada"
                val recomendaciones = "Lleva paraguas y ropa impermeable. El tour puede continuar con las precauciones adecuadas."
                
                repository.crearNotificacionAlertaClimatica(
                    usuarioId,
                    ubicacion,
                    condiciones,
                    recomendaciones
                )
                
                // Enviar notificación push
                val notificacion = repository.obtenerRecordatorios(usuarioId)
                    .firstOrNull { 
                        it.tipo == TipoNotificacion.ALERTA_CLIMATICA &&
                        it.fechaCreacion.after(hace6Horas)
                    }
                notificacion?.let {
                    notificacionesService.mostrarNotificacion(it, usuarioId)
                }
            }
        }
    }

    /**
     * Genera ofertas de último minuto para tours con baja ocupación.
     */
    private fun generarOfertasUltimoMinuto(usuarioId: Int) {
        val toursConDescuento = repository.obtenerToursConDescuento()
        
        for (tour in toursConDescuento) {
            // Verificar si ya existe una oferta para este tour (últimas 24 horas)
            val notificaciones = repository.obtenerRecordatorios(usuarioId)
            val ahora = Date()
            val hace24Horas = Date(ahora.time - 24 * 60 * 60 * 1000)
            
            val yaExiste = notificaciones.any {
                it.tipo == TipoNotificacion.OFERTA_ULTIMO_MINUTO &&
                it.tourId == tour.tourId &&
                it.fechaCreacion.after(hace24Horas)
            }
            
            if (!yaExiste) {
                repository.crearNotificacionOferta(
                    usuarioId,
                    tour.tourId,
                    tour.nombre,
                    20 // 20% descuento
                )
                
                // Enviar notificación push
                val notificacion = repository.obtenerRecordatorios(usuarioId)
                    .firstOrNull { 
                        it.tipo == TipoNotificacion.OFERTA_ULTIMO_MINUTO &&
                        it.tourId == tour.tourId &&
                        it.fechaCreacion.after(hace24Horas)
                    }
                notificacion?.let {
                    notificacionesService.mostrarNotificacion(it, usuarioId)
                }
            }
        }
    }

    /**
     * Genera encuestas automáticas para tours que han finalizado (HU-009).
     * Se envía la encuesta 1 hora después de que finaliza el tour.
     */
    private fun generarEncuestasAutomaticas(usuarioId: Int) {
        // Obtener reservas confirmadas del usuario
        val reservas = repository.obtenerReservasPorUsuario(usuarioId)
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ahora = Calendar.getInstance()

        for (reserva in reservas) {
            if (reserva.estaConfirmado() && reserva.tourId.isNotEmpty()) {
                val tour = repository.obtenerTourPorId(reserva.tourId)
                tour?.let {
                    // Verificar si el tour fue ayer o antes (ya finalizó)
                    val fechaTour = try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.fecha)
                    } catch (e: Exception) {
                        null
                    }
                    
                    if (fechaTour != null) {
                        val fechaHoyDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaHoy)
                        if (fechaHoyDate != null && fechaTour.before(fechaHoyDate)) {
                            // El tour ya finalizó, verificar si ya se envió la encuesta
                            val notificaciones = repository.obtenerRecordatorios(usuarioId)
                            val yaExisteEncuesta = notificaciones.any {
                                it.tourId == reserva.tourId &&
                                it.tipo == TipoNotificacion.ENCUESTA_SATISFACCION
                            }
                            
                            // Verificar si el usuario ya respondió
                            val yaRespondio = repository.yaRespondioEncuesta(reserva.tourId, usuarioId)
                            
                            if (!yaExisteEncuesta && !yaRespondio) {
                                // Enviar encuesta automática
                                val exito = repository.enviarEncuestaAutomatica(reserva.tourId, usuarioId)
                                
                                if (exito) {
                                    // Enviar notificación push
                                    val notificacion = repository.obtenerRecordatorios(usuarioId)
                                        .firstOrNull {
                                            it.tourId == reserva.tourId &&
                                            it.tipo == TipoNotificacion.ENCUESTA_SATISFACCION
                                        }
                                    notificacion?.let { notif ->
                                        notificacionesService.mostrarNotificacion(notif, usuarioId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

