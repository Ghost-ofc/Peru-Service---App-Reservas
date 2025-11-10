package com.grupo4.appreservas.modelos

import java.io.Serializable
import java.util.Date

/**
 * Modelo que representa un logro/achievement del usuario.
 * Según HU-007, los logros se desbloquean cuando el usuario completa ciertas acciones.
 */
data class Logro(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val icono: String = "", // Nombre del recurso drawable
    val puntosRequeridos: Int = 0, // Puntos necesarios para desbloquear (si aplica)
    val tipo: TipoLogro,
    val criterio: CriterioLogro,
    val fechaDesbloqueo: Date? = null, // null si no está desbloqueado
    val desbloqueado: Boolean = fechaDesbloqueo != null
) : Serializable

/**
 * Tipos de logros según las acciones del usuario.
 */
enum class TipoLogro(val valor: String) {
    PRIMER_VIAJE("Primer Viaje"),
    VIAJERO_FRECUENTE("Viajero Frecuente"),
    EXPLORADOR_SEMANA("Explorador de la Semana"),
    EXPLORADOR_MES("Explorador del Mes"),
    TOURS_COMPLETADOS("Tours Completados"),
    PUNTOS_ACUMULADOS("Puntos Acumulados");

    companion object {
        fun fromString(valor: String): TipoLogro {
            return values().find { it.valor == valor } ?: PRIMER_VIAJE
        }
    }
}

/**
 * Criterios para desbloquear logros.
 */
data class CriterioLogro(
    val tipo: TipoCriterio,
    val valor: Int // Valor numérico del criterio (ej: número de tours, puntos, etc.)
) : Serializable

enum class TipoCriterio(val valor: String) {
    TOURS_COMPLETADOS("tours_completados"),
    PUNTOS_ACUMULADOS("puntos_acumulados"),
    TOURS_EN_SEMANA("tours_en_semana"),
    TOURS_EN_MES("tours_en_mes"),
    PRIMERA_RESERVA("primera_reserva");

    companion object {
        fun fromString(valor: String): TipoCriterio {
            return values().find { it.valor == valor } ?: TOURS_COMPLETADOS
        }
    }
}

