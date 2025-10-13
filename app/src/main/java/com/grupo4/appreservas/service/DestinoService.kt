package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.repository.DestinoRepository

class DestinoService(
    private val destinoRepository: DestinoRepository
) {

    fun listarDestinos(): List<Destino> {
        return destinoRepository.getDestinos()
    }

    fun filtrarDestinos(criterios: Map<String, Any>): List<Destino> {
        var destinos = destinoRepository.getDestinos()

        // Filtrar por categoría
        criterios["categoria"]?.let { categoria ->
            destinos = destinos.filter {
                it.categorias.contains(categoria as String)
            }
        }

        // Filtrar por precio
        criterios["precioMin"]?.let { min ->
            destinos = destinos.filter { it.precio >= (min as Double) }
        }

        criterios["precioMax"]?.let { max ->
            destinos = destinos.filter { it.precio <= (max as Double) }
        }

        return destinos
    }

    fun obtenerDetalle(destinoId: String): Destino? {
        return destinoRepository.getDetalle(destinoId)
    }
}