package com.grupo4.appreservas.modelos

import java.util.Date

/**
 * Modelo de datos para las respuestas de encuestas de satisfacción.
 * Equivalente a EncuestaRespuesta del diagrama UML.
 */
data class EncuestaRespuesta(
    val idRespuesta: String,
    val idTour: String,
    val usuarioId: String,
    val calificacion: Int, // 1-5 estrellas
    val comentario: String,
    val fechaRespuesta: Date,
    val puntosOtorgados: Int = 50 // Puntos otorgados por completar la encuesta
) {
    /**
     * Valida que la calificación esté en el rango válido (1-5).
     */
    fun esCalificacionValida(): Boolean {
        return calificacion in 1..5
    }

    /**
     * Obtiene el texto descriptivo de la calificación.
     */
    fun obtenerTextoCalificacion(): String {
        return when (calificacion) {
            1 -> "Muy insatisfecho"
            2 -> "Insatisfecho"
            3 -> "Neutral"
            4 -> "Satisfecho"
            5 -> "Muy satisfecho"
            else -> "Sin calificar"
        }
    }
}

