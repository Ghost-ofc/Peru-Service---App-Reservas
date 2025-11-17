package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.repository.PeruvianServiceRepository

/**
 * Servicio para operaciones relacionadas con destinos.
 */
class DestinoService(
    private val repository: PeruvianServiceRepository
) {

    fun listarDestinos(): List<Destino> {
        return repository.buscarDestinos()
    }

    fun obtenerDetalle(destinoId: String): Destino? {
        return repository.buscarDestinoPorId(destinoId)
    }
}
