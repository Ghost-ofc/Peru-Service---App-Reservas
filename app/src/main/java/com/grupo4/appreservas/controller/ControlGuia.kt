package com.grupo4.appreservas.controller

import android.content.Context
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.repository.RepositorioAsignaciones

/**
 * Controlador de Guía según el diagrama UML.
 * Equivalente a GuiaViewModel del diagrama, pero en arquitectura MVC.
 * 
 * En MVC, este controller actúa como intermediario entre la Vista (PanelGuiaActivity, DetalleTourActivity)
 * y los Repositorios (Model).
 */
class ControlGuia(
    private val repositorioAsignaciones: RepositorioAsignaciones
) {

    /**
     * Carga los tours asignados a un guía para el día actual.
     * Equivalente a cargarToursDelDia(guiaId) del diagrama UML.
     * 
     * @param guiaId ID del guía
     * @return Lista de tours del día
     */
    fun cargarToursDelDia(guiaId: Int): List<Tour> {
        return repositorioAsignaciones.obtenerToursDelDia(guiaId)
    }
    
    /**
     * Carga todos los tours asignados a un guía, ordenados por fecha ascendente.
     * 
     * @param guiaId ID del guía
     * @return Lista de todos los tours del guía ordenados por fecha (ascendente)
     */
    fun cargarTodosLosTours(guiaId: Int): List<Tour> {
        return repositorioAsignaciones.obtenerTodosLosTours(guiaId)
    }

    /**
     * Abre un tour específico.
     * Equivalente a abrirTour(tourId) del diagrama UML.
     * 
     * @param tourId ID del tour
     */
    fun abrirTour(tourId: String) {
        // Lógica para cambiar estado del tour si es necesario
        // Por ahora, solo es un método de conveniencia
    }
}

