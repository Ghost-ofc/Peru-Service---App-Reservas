package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.DestinoService

/**
 * Controlador para el catálogo de destinos.
 * Maneja la lógica de negocio para la visualización del catálogo.
 */
class CatalogoController(
    private val destinoService: DestinoService
) {

    /**
     * Solicita la lista de destinos disponibles.
     */
    fun solicitarDestinos(): List<Destino> {
        return destinoService.listarDestinos()
    }
}
