package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Booking
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.BookingService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ReservationController(
    private val bookingService: BookingService,
    private val availabilityService: AvailabilityService
) {

    fun consultarDisponibilidad(tourSlotId: String): Map<String, Any> {
        return availabilityService.consultarDisponibilidad(tourSlotId)
    }

    fun lockSeats(tourSlotId: String, numPersonas: Int): Boolean {
        return availabilityService.verificarYBloquearCupos(tourSlotId, numPersonas)
    }

    fun crearReservaCmd(userId: String, tourSlotId: String, pax: Int): Booking? {
        val parts = tourSlotId.split("_")
        if (parts.size < 3) return null

        val destinoId = "${parts[0]}_${parts[1]}" // "dest_001"
        val fechaStr = parts[2] // "2025-10-14"

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = dateFormat.parse(fechaStr) ?: Date()

            return bookingService.crear(
                userId = userId,
                destinoId = destinoId,
                tourSlotId = tourSlotId,
                fecha = fecha,
                horaInicio = "08:00",
                pax = pax
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}


