package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinoService

class CatalogoController(
    private val destinoService: DestinoService,
    private val availabilityService: AvailabilityService
) {

    fun solicitarDestinos(): List<Destino> {
        return destinoService.listarDestinos()
    }

    fun solicitarDisponibilidad(destinoId: String, fecha: String): Map<String, Any>? {
        // Implementación simplificada
        val destino = destinoService.obtenerDetalle(destinoId) ?: return null

        return mapOf(
            "destinoId" to destinoId,
            "fecha" to fecha,
            "cuposDisponibles" to 6
        )
    }

    fun aplicarFiltros(criterios: Map<String, Any>): List<Destino> {
        return destinoService.filtrarDestinos(criterios)
    }
}