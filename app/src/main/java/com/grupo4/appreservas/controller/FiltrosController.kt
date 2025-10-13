package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.DestinoService

class FiltrosController(
    private val destinoService: DestinoService
) {

    fun listarDestinos(): List<Destino> {
        return destinoService.listarDestinos()
    }

    fun filtrarDestinos(criterios: Map<String, Any>): List<Destino> {
        return destinoService.filtrarDestinos(criterios)
    }
}