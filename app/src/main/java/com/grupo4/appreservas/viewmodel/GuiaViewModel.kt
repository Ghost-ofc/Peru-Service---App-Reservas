package com.grupo4.appreservas.viewmodel

import android.content.Context
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.repository.RepositorioAsignaciones

class GuiaViewModel(context: Context) {
    private val repositorioAsignaciones = RepositorioAsignaciones(context)

    fun cargarToursDelDia(guiaId: Int): List<Tour> {
        return repositorioAsignaciones.obtenerToursDelDia(guiaId)
    }

    fun abrirTour(tourId: String) {
        // Lógica para cambiar estado del tour si es necesario
    }
}