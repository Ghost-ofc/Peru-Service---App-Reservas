package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.DestinoService

/**
 * Controlador para la gestión del detalle de destinos.
 * Corresponde a ControlDetalleDestino del diagrama UML.
 */
class ControlDetalleDestino(
    private val destinoService: DestinoService
) {

    /**
     * Carga el detalle de un destino por su ID.
     * Equivalente a cargarDetalle(id) del diagrama UML.
     * 
     * @param destinoId ID del destino a cargar
     * @return Destino con todos sus detalles, o null si no existe
     */
    fun cargarDetalle(destinoId: String): Destino? {
        return destinoService.obtenerDetalle(destinoId)
    }
}
