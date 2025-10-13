package com.grupo4.appreservas.repository

import com.grupo4.appreservas.modelos.Booking
import com.grupo4.appreservas.modelos.TourSlot
import java.util.Date
import java.util.UUID

class ReservasRepository private constructor() {

    private val bookingsCache = mutableMapOf<String, Booking>()
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

    fun save(booking: Booking): Booking {
        val nuevoBooking = if (booking.id.isEmpty()) {
            booking.copy(id = "BK${UUID.randomUUID().toString().substring(0, 8).uppercase()}")
        } else {
            booking
        }
        bookingsCache[nuevoBooking.id] = nuevoBooking
        return nuevoBooking
    }

    fun find(bookingId: String): Booking? {
        return bookingsCache[bookingId]
    }

    fun findByBookingId(bookingId: String): Booking? {
        return bookingsCache[bookingId]
    }

    fun obtenerReservasUsuario(userId: String): List<Booking> {
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