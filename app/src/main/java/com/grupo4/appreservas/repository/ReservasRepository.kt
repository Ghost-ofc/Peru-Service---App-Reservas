package com.grupo4.appreservas.repository

import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.TourSlot
import java.util.Date
import java.util.UUID

class ReservasRepository private constructor() {

    private val bookingsCache = mutableMapOf<String, Reserva>()
    private val tourSlotsCache = mutableMapOf<String, TourSlot>()

    companion object {
        @Volatile
        private var instance: ReservasRepository? = null

        fun getInstance(): ReservasRepository {
            return instance ?: synchronized(this) {
                instance ?: ReservasRepository().also { instance = it }
            }
        }
    }

    fun save(reserva: Reserva): Reserva {
        val nuevoBooking = if (reserva.id.isEmpty()) {
            reserva.copy(id = "BK${UUID.randomUUID().toString().substring(0, 8).uppercase()}")
        } else {
            reserva
        }
        bookingsCache[nuevoBooking.id] = nuevoBooking
        return nuevoBooking
    }

    fun find(bookingId: String): Reserva? {
        return bookingsCache[bookingId]
    }

    fun findByBookingId(bookingId: String): Reserva? {
        return bookingsCache[bookingId]
    }

    fun obtenerReservasUsuario(userId: String): List<Reserva> {
        return bookingsCache.values.filter { it.userId == userId }
    }

    // TourSlot management
    fun saveTourSlot(slot: TourSlot) {
        tourSlotsCache[slot.tourSlotId] = slot
    }

    fun findTourSlot(tourSlotId: String): TourSlot? {
        return tourSlotsCache[tourSlotId]
    }

    fun crearTourSlotSiNoExiste(tourSlotId: String, fecha: Date, capacidad: Int): TourSlot {
        return tourSlotsCache.getOrPut(tourSlotId) {
            TourSlot(
                tourSlotId = tourSlotId,
                fecha = fecha,
                capacidad = capacidad,
                ocupados = 0
            )
        }
    }
}