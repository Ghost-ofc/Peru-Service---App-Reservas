package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.DestinationService

class FiltrosController(
    private val destinationService: DestinationService
) {

    fun listarDestinos(): List<Destino> {
        return destinationService.listarDestinos()
    }

    fun filtrarDestinos(criterios: Map<String, Any>): List<Destino> {
        return destinationService.filtrarDestinos(criterios)
    }
}