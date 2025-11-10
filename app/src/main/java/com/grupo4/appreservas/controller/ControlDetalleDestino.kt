package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinoService

/**
 * Controlador para la gestión del detalle de destinos.
 * Corresponde a ControlDetalleDestino del diagrama UML.
 */
class ControlDetalleDestino(
    private val destinoService: DestinoService,
    private val availabilityService: AvailabilityService
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

    /**
     * Obtiene la disponibilidad de cupos para un destino en una fecha específica.
     * 
     * @param destinoId ID del destino
     * @param fecha Fecha en formato "yyyy-MM-dd"
     * @return Mapa con información de disponibilidad (cuposDisponibles, cuposTotales, etc.)
     */
    fun obtenerCupos(destinoId: String, fecha: String): Map<String, Any>? {
        val tourSlotId = "${destinoId}_$fecha"
        return availabilityService.consultarDisponibilidad(tourSlotId)
    }

    /**
     * Obtiene el detalle completo de un destino incluyendo disponibilidad.
     * 
     * @param destinoId ID del destino
     * @param fecha Fecha opcional para consultar disponibilidad
     * @return Mapa con detalle del destino y disponibilidad si se proporciona fecha
     */
    fun obtenerDetalleCompleto(destinoId: String, fecha: String? = null): Map<String, Any>? {
        val destino = cargarDetalle(destinoId) ?: return null
        
        val resultado = mutableMapOf<String, Any>(
            "destino" to destino
        )
        
        if (fecha != null) {
            val disponibilidad = obtenerCupos(destinoId, fecha)
            disponibilidad?.let { resultado.putAll(it) }
        }
        
        return resultado
    }
}

