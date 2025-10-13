package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.TourSlot
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.DestinoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvailabilityService(
    private val destinoRepository: DestinoRepository,
    private val reservasRepository: ReservasRepository
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun consultarDisponibilidad(tourSlotId: String): Map<String, Any> {
        val parts = tourSlotId.split("_")
        if (parts.size < 3) {
            return mapOf(
                "tourSlotId" to tourSlotId,
                "cuposDisponibles" to 0,
                "cuposTotales" to 0,
                "disponible" to false
            )
        }


        val destinoId = "${parts[0]}_${parts[1]}"
        val fechaStr = parts[2]

        val destino = destinoRepository.getDetalle(destinoId)

        if (destino == null) {
            return mapOf(
                "tourSlotId" to tourSlotId,
                "cuposDisponibles" to 0,
                "cuposTotales" to 0,
                "disponible" to false
            )
        }

        // Parsear la fecha
        val fecha = try {
            dateFormat.parse(fechaStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        // Buscar o crear el TourSlot
        var tourSlot = reservasRepository.findTourSlot(tourSlotId)

        if (tourSlot == null) {
            // Si no existe, crearlo con capacidad máxima del destino
            tourSlot = reservasRepository.crearTourSlotSiNoExiste(
                tourSlotId = tourSlotId,
                fecha = fecha,
                capacidad = destino.maxPersonas
            )
        }

        val cuposDisponibles = tourSlot.capacidad - tourSlot.ocupados

        return mapOf(
            "tourSlotId" to tourSlotId,
            "cuposDisponibles" to cuposDisponibles,
            "cuposTotales" to tourSlot.capacidad,
            "ocupados" to tourSlot.ocupados,
            "disponible" to (cuposDisponibles > 0)
        )
    }

    fun cupos(tourSlotId: String, fecha: Date): Int {
        val slot = reservasRepository.findTourSlot(tourSlotId)
        return slot?.cuposDisponibles() ?: 0
    }

    fun verificarYBloquearCupos(tourSlotId: String, numPersonas: Int): Boolean {
        val tourSlot = reservasRepository.findTourSlot(tourSlotId) ?: return false

        val cuposDisponibles = tourSlot.capacidad - tourSlot.ocupados

        if (cuposDisponibles >= numPersonas) {
            // Bloquear los cupos actualizando el slot
            val slotActualizado = tourSlot.copy(
                ocupados = tourSlot.ocupados + numPersonas
            )
            reservasRepository.saveTourSlot(slotActualizado)
            return true
        }

        return false
    }

    fun lockSeats(tourSlotId: String, pax: Int): Boolean {
        return verificarYBloquearCupos(tourSlotId, pax)
    }

    fun liberarCupos(tourSlotId: String, numPersonas: Int) {
        val tourSlot = reservasRepository.findTourSlot(tourSlotId) ?: return

        val slotActualizado = tourSlot.copy(
            ocupados = maxOf(0, tourSlot.ocupados - numPersonas)
        )
        reservasRepository.saveTourSlot(slotActualizado)
    }

    fun find(tourSlotId: String): TourSlot? {
        return reservasRepository.findTourSlot(tourSlotId)
    }

    fun bulkCupos(listaIds: List<String>, fechaOpcional: Date?): Map<String, Int> {
        val resultado = mutableMapOf<String, Int>()

        listaIds.forEach { destinoId ->
            // Para cada destino, obtener o crear un slot
            val tourSlotId = generarTourSlotId(destinoId, fechaOpcional ?: Date())
            val cuposDisp = cupos(tourSlotId, fechaOpcional ?: Date())
            resultado[destinoId] = cuposDisp
        }

        return resultado
    }

    private fun generarTourSlotId(destinoId: String, fecha: Date): String {
        val fechaStr = dateFormat.format(fecha)
        return "${destinoId}_$fechaStr"
    }

    fun crearSlotSiNoExiste(destinoId: String, fecha: Date, capacidad: Int): TourSlot {
        val tourSlotId = generarTourSlotId(destinoId, fecha)
        return reservasRepository.crearTourSlotSiNoExiste(tourSlotId, fecha, capacidad)
    }
}