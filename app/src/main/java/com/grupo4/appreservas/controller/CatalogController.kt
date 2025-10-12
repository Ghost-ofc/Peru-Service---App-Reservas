package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinationService

class CatalogController(
    private val destinationService: DestinationService,
    private val availabilityService: AvailabilityService
) {

    fun solicitarDestinos(): List<Destino> {
        return destinationService.listarDestinos()
    }

    fun solicitarDisponibilidad(destinoId: String, fecha: String): Map<String, Any>? {
        // Implementación simplificada
        val destino = destinationService.obtenerDetalle(destinoId) ?: return null

        return mapOf(
            "destinoId" to destinoId,
            "fecha" to fecha,
            "cuposDisponibles" to 6
        )
    }

    fun aplicarFiltros(criterios: Map<String, Any>): List<Destino> {
        return destinationService.filtrarDestinos(criterios)
    }
}