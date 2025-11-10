package com.grupo4.appreservas.controller

import android.content.Context
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.modelos.TipoNotificacion
import com.grupo4.appreservas.repository.RepositorioNotificaciones
import com.grupo4.appreservas.repository.RepositorioOfertas
import com.grupo4.appreservas.repository.RepositorioClima
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.service.NotificacionesService
import java.util.*
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Controlador de Notificaciones según el diagrama UML.
 * Equivalente a NotificacionesViewModel del diagrama, pero en arquitectura MVC.
 * 
 * En MVC, este controller actúa como intermediario entre la Vista (NotificacionesActivity)
 * y los Repositorios (Model).
 */
class ControlNotificaciones(
    private val repositorioNotificaciones: RepositorioNotificaciones,
    private val repositorioOfertas: RepositorioOfertas,
    private val repositorioClima: RepositorioClima,
    private val notificacionesService: NotificacionesService,
    private val context: Context
) {

    /**
     * Carga los recordatorios (notificaciones) de un usuario.
     * Equivalente a cargarRecordatorios(usuarioId) del diagrama UML.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones del usuario
     */
    fun cargarRecordatorios(usuarioId: Int): List<Notificacion> {
        return repositorioNotificaciones.obtenerRecordatorios(usuarioId)
    }

    /**
     * Detecta cambios climáticos y genera notificaciones si es necesario.
     * Equivalente a detectarCambioClimatico() del diagrama UML.
     * 
     * @param usuarioId ID del usuario
     */
    fun detectarCambioClimatico(usuarioId: Int) {
        val dbHelper = DatabaseHelper(context)
        
        // Obtener reservas confirmadas del usuario para tours futuros
        val reservas = dbHelper.obtenerReservasPorUsuario(usuarioId)
            .filter { it.estaConfirmado() }
        
        val fechaHoy = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        reservas.forEach { reserva ->
            try {
                // Obtener la fecha del tour desde la reserva
                val fechaTour = reserva.fecha
                
                // Verificar si el tour es en los próximos 3 días
                val calendario = Calendar.getInstance()
                calendario.time = fechaHoy
                calendario.add(Calendar.DAY_OF_MONTH, 3)
                val fechaLimite = calendario.time
                
                // Comparar fechas (solo día, sin hora)
                val calFechaTour = Calendar.getInstance()
                calFechaTour.time = fechaTour
                calFechaTour.set(Calendar.HOUR_OF_DAY, 0)
                calFechaTour.set(Calendar.MINUTE, 0)
                calFechaTour.set(Calendar.SECOND, 0)
                calFechaTour.set(Calendar.MILLISECOND, 0)
                
                val calFechaHoy = Calendar.getInstance()
                calFechaHoy.time = fechaHoy
                calFechaHoy.set(Calendar.HOUR_OF_DAY, 0)
                calFechaHoy.set(Calendar.MINUTE, 0)
                calFechaHoy.set(Calendar.SECOND, 0)
                calFechaHoy.set(Calendar.MILLISECOND, 0)
                
                val calFechaLimite = Calendar.getInstance()
                calFechaLimite.time = fechaLimite
                calFechaLimite.set(Calendar.HOUR_OF_DAY, 0)
                calFechaLimite.set(Calendar.MINUTE, 0)
                calFechaLimite.set(Calendar.SECOND, 0)
                calFechaLimite.set(Calendar.MILLISECOND, 0)
                
                if (calFechaTour.after(calFechaHoy) && calFechaTour.before(calFechaLimite) || calFechaTour.time == calFechaHoy.time) {
                    // Obtener ubicación del destino
                    val destino = reserva.destino
                    val ubicacion = destino?.ubicacion ?: return@forEach
                    
                    // Obtener condiciones climáticas actuales
                    val climaActual = repositorioClima.obtenerCondiciones(ubicacion)
                    
                    // Detectar si hay un cambio significativo
                    if (repositorioClima.detectarCambio(climaActual)) {
                        // Generar notificación de alerta climática
                        val notificacion = crearNotificacionAlertaClimatica(
                            usuarioId = usuarioId,
                            destinoNombre = destino.nombre,
                            clima = climaActual,
                            tourId = reserva.tourId,
                            puntoEncuentro = destino.ubicacion
                        )
                        
                        // Guardar y enviar notificación
                        repositorioNotificaciones.enviarNotificacionPush(notificacion)
                        notificacionesService.enviarNotificacionPush(notificacion)
                    } else if (climaActual.condicion == "Soleado" && climaActual.temperatura > 20) {
                        // Clima favorable - generar notificación positiva
                        val notificacion = crearNotificacionClimaFavorable(
                            usuarioId = usuarioId,
                            destinoNombre = destino.nombre,
                            clima = climaActual
                        )
                        
                        // Solo enviar si no existe ya una notificación similar reciente
                        val notificacionesExistentes = repositorioNotificaciones.obtenerRecordatorios(usuarioId)
                        val yaExiste = notificacionesExistentes.any {
                            it.tipo == TipoNotificacion.CLIMA_FAVORABLE &&
                            it.destinoNombre == destino.nombre &&
                            Math.abs(it.fechaCreacion.time - Date().time) < 24 * 60 * 60 * 1000 // Menos de 24 horas
                        }
                        
                        if (!yaExiste) {
                            repositorioNotificaciones.enviarNotificacionPush(notificacion)
                            notificacionesService.enviarNotificacionPush(notificacion)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ControlNotificaciones", "Error al detectar cambio climático: ${e.message}", e)
            }
        }
    }

    /**
     * Genera ofertas de último minuto para tours con baja ocupación.
     * Equivalente a generarOfertaUltimoMinuto() del diagrama UML.
     * 
     * @param usuarioId ID del usuario
     */
    fun generarOfertaUltimoMinuto(usuarioId: Int) {
        try {
            // Obtener tours con baja ocupación
            val toursConBajaOcupacion = repositorioOfertas.toursConBajaOcupacion()
            
            toursConBajaOcupacion.forEach { tour ->
                // Verificar si el usuario no tiene ya una reserva para este tour
                val dbHelper = DatabaseHelper(context)
                val reservasUsuario = dbHelper.obtenerReservasPorUsuario(usuarioId)
                val tieneReserva = reservasUsuario.any { it.tourId == tour.tourId }
                
                if (!tieneReserva) {
                    // Generar descuento
                    val descuento = repositorioOfertas.generarDescuento(tour.tourId)
                    
                    if (descuento > 0) {
                        // Obtener destino
                        val destinoId = if (tour.tourId.contains("_")) {
                            val partes = tour.tourId.split("_")
                            "${partes[0]}_${partes[1]}"
                        } else {
                            tour.tourId
                        }
                        val destino = dbHelper.obtenerDestinoPorId(destinoId)
                        
                        // Crear notificación de oferta
                        val notificacion = crearNotificacionOferta(
                            usuarioId = usuarioId,
                            tour = tour,
                            destinoNombre = destino?.nombre ?: tour.nombre,
                            descuento = descuento
                        )
                        
                        // Verificar si ya existe una notificación de oferta para este tour
                        val notificacionesExistentes = repositorioNotificaciones.obtenerRecordatorios(usuarioId)
                        val yaExiste = notificacionesExistentes.any {
                            it.tipo == TipoNotificacion.OFERTA_ULTIMO_MINUTO &&
                            it.tourId == tour.tourId
                        }
                        
                        if (!yaExiste) {
                            // Guardar y enviar notificación
                            repositorioNotificaciones.enviarNotificacionPush(notificacion)
                            notificacionesService.enviarNotificacionPush(notificacion)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ControlNotificaciones", "Error al generar oferta de último minuto: ${e.message}", e)
        }
    }

    /**
     * Genera recordatorios de tours próximos para un usuario.
     * 
     * @param usuarioId ID del usuario
     */
    fun generarRecordatoriosTours(usuarioId: Int) {
        val dbHelper = DatabaseHelper(context)
        
        // Obtener reservas confirmadas del usuario
        val reservas = dbHelper.obtenerReservasPorUsuario(usuarioId)
            .filter { it.estaConfirmado() }
        
        val fechaHoy = Date()
        val calendario = Calendar.getInstance()
        calendario.time = fechaHoy
        calendario.add(Calendar.HOUR, 24) // 24 horas antes del tour
        val fechaLimiteRecordatorio = calendario.time
        
        reservas.forEach { reserva ->
            try {
                val fechaTour = reserva.fecha
                
                // Comparar fechas (solo día, sin hora)
                val calFechaTour = Calendar.getInstance()
                calFechaTour.time = fechaTour
                calFechaTour.set(Calendar.HOUR_OF_DAY, 0)
                calFechaTour.set(Calendar.MINUTE, 0)
                calFechaTour.set(Calendar.SECOND, 0)
                calFechaTour.set(Calendar.MILLISECOND, 0)
                
                val calFechaHoy = Calendar.getInstance()
                calFechaHoy.time = fechaHoy
                calFechaHoy.set(Calendar.HOUR_OF_DAY, 0)
                calFechaHoy.set(Calendar.MINUTE, 0)
                calFechaHoy.set(Calendar.SECOND, 0)
                calFechaHoy.set(Calendar.MILLISECOND, 0)
                
                val calFechaLimite = Calendar.getInstance()
                calFechaLimite.time = fechaLimiteRecordatorio
                calFechaLimite.set(Calendar.HOUR_OF_DAY, 0)
                calFechaLimite.set(Calendar.MINUTE, 0)
                calFechaLimite.set(Calendar.SECOND, 0)
                calFechaLimite.set(Calendar.MILLISECOND, 0)
                
                // Verificar si el tour es mañana (entre ahora y 24 horas)
                if ((calFechaTour.after(calFechaHoy) || calFechaTour.time == calFechaHoy.time) && calFechaTour.before(calFechaLimite)) {
                    // Verificar si ya existe un recordatorio para este tour
                    val notificacionesExistentes = repositorioNotificaciones.obtenerRecordatorios(usuarioId)
                    val yaExiste = notificacionesExistentes.any {
                        it.tipo == TipoNotificacion.RECORDATORIO &&
                        it.tourId == reserva.tourId &&
                        Math.abs(it.fechaCreacion.time - Date().time) < 12 * 60 * 60 * 1000 // Menos de 12 horas
                    }
                    
                    if (!yaExiste) {
                        // Obtener tour para obtener punto de encuentro y hora
                        val tour = dbHelper.obtenerTourPorId(reserva.tourId)
                        val destino = reserva.destino
                        
                        // Crear notificación de recordatorio
                        val notificacion = crearNotificacionRecordatorio(
                            usuarioId = usuarioId,
                            destinoNombre = destino?.nombre ?: tour?.nombre ?: "Tour",
                            tourId = reserva.tourId,
                            puntoEncuentro = tour?.puntoEncuentro ?: destino?.ubicacion ?: "",
                            horaTour = reserva.horaInicio ?: tour?.hora ?: ""
                        )
                        
                        // Guardar y enviar notificación
                        repositorioNotificaciones.enviarNotificacionPush(notificacion)
                        notificacionesService.enviarNotificacionPush(notificacion)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ControlNotificaciones", "Error al generar recordatorio: ${e.message}", e)
            }
        }
    }

    /**
     * Crea una notificación de recordatorio de tour.
     */
    private fun crearNotificacionRecordatorio(
        usuarioId: Int,
        destinoNombre: String,
        tourId: String,
        puntoEncuentro: String,
        horaTour: String
    ): Notificacion {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaFormateada = try {
            val hora = horaTour.split(":").getOrNull(0)?.toIntOrNull() ?: 0
            val minuto = horaTour.split(":").getOrNull(1)?.toIntOrNull() ?: 0
            String.format("%02d:%02d", hora, minuto)
        } catch (e: Exception) {
            horaTour
        }
        
        return Notificacion(
            id = "REC_${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.RECORDATORIO,
            titulo = "Recordatorio de Tour",
            descripcion = "Tu tour '$destinoNombre' es mañana a las $horaFormateada",
            fechaCreacion = Date(),
            tourId = tourId,
            destinoNombre = destinoNombre,
            puntoEncuentro = puntoEncuentro,
            horaTour = horaFormateada
        )
    }

    /**
     * Crea una notificación de alerta climática.
     */
    private fun crearNotificacionAlertaClimatica(
        usuarioId: Int,
        destinoNombre: String,
        clima: com.grupo4.appreservas.modelos.Clima,
        tourId: String,
        puntoEncuentro: String
    ): Notificacion {
        val recomendaciones = when (clima.condicion) {
            "Lluvioso" -> "Lleva ropa impermeable y verifica posibles cambios"
            "Nublado" -> "Lleva una chaqueta ligera por si hace frío"
            "Soleado" -> "Lleva protector solar y una gorra"
            else -> "Verifica el clima antes de salir"
        }
        
        return Notificacion(
            id = "CLIMA_${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.ALERTA_CLIMATICA,
            titulo = "Alerta Climática",
            descripcion = "Se pronostican ${clima.condicion.lowercase()} para tu tour del $destinoNombre",
            fechaCreacion = Date(),
            tourId = tourId,
            destinoNombre = destinoNombre,
            puntoEncuentro = puntoEncuentro,
            recomendaciones = recomendaciones,
            condicionesClima = "${clima.temperatura}°C, ${clima.condicion}, Humedad: ${clima.humedad}%"
        )
    }

    /**
     * Crea una notificación de clima favorable.
     */
    private fun crearNotificacionClimaFavorable(
        usuarioId: Int,
        destinoNombre: String,
        clima: com.grupo4.appreservas.modelos.Clima
    ): Notificacion {
        return Notificacion(
            id = "FAV_${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.CLIMA_FAVORABLE,
            titulo = "Clima Favorable",
            descripcion = "Excelente clima para tu próximo tour. ¡Lleva protector solar!",
            fechaCreacion = Date(),
            destinoNombre = destinoNombre,
            recomendaciones = "Lleva ropa impermeable y verifica posibles cambios",
            condicionesClima = "${clima.temperatura}°C, ${clima.condicion}, Humedad: ${clima.humedad}%"
        )
    }

    /**
     * Crea una notificación de oferta de último minuto.
     */
    private fun crearNotificacionOferta(
        usuarioId: Int,
        tour: com.grupo4.appreservas.modelos.Tour,
        destinoNombre: String,
        descuento: Int
    ): Notificacion {
        return Notificacion(
            id = "OFERTA_${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.OFERTA_ULTIMO_MINUTO,
            titulo = "¡Oferta de Último Minuto!",
            descripcion = "$descuento% de descuento en $destinoNombre",
            fechaCreacion = Date(),
            tourId = tour.tourId,
            destinoNombre = destinoNombre,
            descuento = descuento
        )
    }

    /**
     * Marca una notificación como leída.
     * 
     * @param notificacionId ID de la notificación
     * @return true si se marcó correctamente, false en caso contrario
     */
    fun marcarComoLeida(notificacionId: String): Boolean {
        return repositorioNotificaciones.marcarComoLeida(notificacionId)
    }

    /**
     * Marca todas las notificaciones como leídas.
     * 
     * @param usuarioId ID del usuario
     * @return Número de notificaciones marcadas como leídas
     */
    fun marcarTodasComoLeidas(usuarioId: Int): Int {
        return repositorioNotificaciones.marcarTodasComoLeidas(usuarioId)
    }
}

