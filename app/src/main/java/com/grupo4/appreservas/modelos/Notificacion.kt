package com.grupo4.appreservas.modelos

import java.io.Serializable
import java.util.Date

/**
 * Modelo de Notificación según el diagrama UML.
 * Representa diferentes tipos de notificaciones: recordatorios, alertas climáticas y ofertas.
 */
enum class TipoNotificacion(val valor: String) {
    RECORDATORIO("Recordatorio"),
    ALERTA_CLIMATICA("Alerta Climática"),
    OFERTA_ULTIMO_MINUTO("Oferta de Último Minuto"),
    CONFIRMACION_RESERVA("Confirmación de Reserva"),
    CLIMA_FAVORABLE("Clima Favorable");

    companion object {
        fun fromString(valor: String): TipoNotificacion {
            return values().find { it.valor == valor } ?: RECORDATORIO
        }
    }
}

data class Notificacion(
    val id: String,
    val usuarioId: Int,
    val tipo: TipoNotificacion,
    val titulo: String,
    val descripcion: String,
    val fechaCreacion: Date,
    val fechaLeida: Date? = null,
    val leida: Boolean = false,
    // Datos adicionales según el tipo
    val tourId: String? = null,
    val destinoNombre: String? = null,
    val puntoEncuentro: String? = null,
    val horaTour: String? = null,
    val descuento: Int? = null, // Porcentaje de descuento para ofertas
    val recomendaciones: String? = null, // Para alertas climáticas
    val condicionesClima: String? = null // Para alertas climáticas
) : Serializable {
    fun estaLeida(): Boolean = leida
    fun esRecordatorio(): Boolean = tipo == TipoNotificacion.RECORDATORIO
    fun esAlertaClimatica(): Boolean = tipo == TipoNotificacion.ALERTA_CLIMATICA || tipo == TipoNotificacion.CLIMA_FAVORABLE
    fun esOferta(): Boolean = tipo == TipoNotificacion.OFERTA_ULTIMO_MINUTO
}

/**
 * Modelo de Clima según el diagrama UML.
 * Representa las condiciones climáticas de una ubicación.
 */
data class Clima(
    val ubicacion: String,
    val temperatura: Double,
    val condicion: String, // "Soleado", "Lluvioso", "Nublado", etc.
    val humedad: Int,
    val fecha: Date = Date()
) : Serializable

