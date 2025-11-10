package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.DatabaseHelper

class RepositorioAsignaciones(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun obtenerToursDelDia(guiaId: Int): List<Tour> {
        val fechaHoy = obtenerFechaHoy()
        return dbHelper.obtenerToursDelGuia(guiaId, fechaHoy)
    }
    
    /**
     * Obtiene todos los tours asignados a un guía, ordenados por fecha ascendente.
     * 
     * @param guiaId ID del guía
     * @return Lista de todos los tours del guía ordenados por fecha (ascendente)
     */
    fun obtenerTodosLosTours(guiaId: Int): List<Tour> {
        return dbHelper.obtenerTodosLosToursDelGuia(guiaId)
    }

    fun obtenerParticipantes(tourId: String): List<Reserva> {
        return dbHelper.obtenerReservasPorTour(tourId)
    }

    private fun obtenerFechaHoy(): String {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formato.format(java.util.Date())
    }
}