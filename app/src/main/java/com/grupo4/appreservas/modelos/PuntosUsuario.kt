package com.grupo4.appreservas.modelos

import java.io.Serializable

/**
 * Modelo que representa los puntos acumulados de un usuario.
 * Según HU-007, los puntos se acumulan cuando el usuario completa reservas.
 */
data class PuntosUsuario(
    val usuarioId: Int,
    val puntosAcumulados: Int = 0,
    val nivel: String = "Explorador", // Nivel basado en puntos
    val puntosParaSiguienteNivel: Int = 0 // Puntos faltantes para el siguiente nivel
) : Serializable {
    companion object {
        /**
         * Calcula el nivel del usuario basado en sus puntos.
         * Niveles:
         * - Explorador: 0-500 puntos
         * - Explorador Experto: 501-1500 puntos
         * - Viajero Profesional: 1501-3000 puntos
         * - Maestro Viajero: 3001+ puntos
         */
        fun calcularNivel(puntos: Int): String {
            return when {
                puntos >= 3001 -> "Maestro Viajero"
                puntos >= 1501 -> "Viajero Profesional"
                puntos >= 501 -> "Explorador Experto"
                else -> "Explorador"
            }
        }

        /**
         * Calcula los puntos necesarios para el siguiente nivel.
         */
        fun calcularPuntosParaSiguienteNivel(puntos: Int): Int {
            return when {
                puntos < 501 -> 501 - puntos // Siguiente: Explorador Experto
                puntos < 1501 -> 1501 - puntos // Siguiente: Viajero Profesional
                puntos < 3001 -> 3001 - puntos // Siguiente: Maestro Viajero
                else -> 0 // Ya está en el nivel máximo
            }
        }

        /**
         * Puntos base por reserva completada.
         */
        const val PUNTOS_POR_RESERVA = 200
    }
}

