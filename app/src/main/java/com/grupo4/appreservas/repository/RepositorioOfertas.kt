package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Tour

/**
 * Repositorio de Ofertas según el diagrama UML.
 * Equivalente a RepositorioOfertas del diagrama.
 */
class RepositorioOfertas(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    /**
     * Obtiene los tours con baja ocupación.
     * Equivalente a toursConBajaOcupacion(): List<Tour> del diagrama UML.
     * 
     * Un tour se considera con baja ocupación si tiene menos del 50% de su capacidad ocupada
     * y la fecha del tour es en el futuro próximo (próximos 7 días).
     * 
     * @return Lista de tours con baja ocupación
     */
    fun toursConBajaOcupacion(): List<Tour> {
        val todosLosTours = dbHelper.obtenerTodosLosTours()
        val fechaHoy = java.util.Date()
        val calendario = java.util.Calendar.getInstance()
        calendario.time = fechaHoy
        calendario.add(java.util.Calendar.DAY_OF_MONTH, 7)
        val fechaLimite = calendario.time

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        return todosLosTours.filter { tour ->
            try {
                val fechaTour = dateFormat.parse(tour.fecha)
                if (fechaTour != null) {
                    // Verificar que el tour esté en los próximos 7 días
                    val estaEnProximosDias = (fechaTour.after(fechaHoy) || fechaTour.time == fechaHoy.time) && fechaTour.before(fechaLimite)
                    
                    // Calcular porcentaje de ocupación
                    val porcentajeOcupacion = if (tour.capacidad > 0) {
                        (tour.participantesConfirmados.toDouble() / tour.capacidad.toDouble()) * 100
                    } else {
                        100.0
                    }
                    
                    // Considerar baja ocupación si está por debajo del 50%
                    estaEnProximosDias && porcentajeOcupacion < 50.0
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Genera un descuento para un tour.
     * Equivalente a generarDescuento(tourId) del diagrama UML.
     * 
     * @param tourId ID del tour
     * @return Porcentaje de descuento generado (entre 20% y 40%)
     */
    fun generarDescuento(tourId: String): Int {
        val tour = dbHelper.obtenerTourPorId(tourId)
        if (tour == null) {
            return 0
        }

        // Calcular descuento basado en la ocupación
        val porcentajeOcupacion = if (tour.capacidad > 0) {
            (tour.participantesConfirmados.toDouble() / tour.capacidad.toDouble()) * 100
        } else {
            100.0
        }

        // Mayor descuento para menor ocupación
        return when {
            porcentajeOcupacion < 20.0 -> 40 // 40% de descuento
            porcentajeOcupacion < 35.0 -> 30 // 30% de descuento
            porcentajeOcupacion < 50.0 -> 20 // 20% de descuento
            else -> 0 // Sin descuento
        }
    }

}

