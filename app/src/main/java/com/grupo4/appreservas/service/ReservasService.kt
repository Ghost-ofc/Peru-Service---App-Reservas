package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Booking
import com.grupo4.appreservas.modelos.EstadoBooking
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.DestinoRepository
import java.util.Date
import java.util.UUID

class ReservasService(
    private val reservasRepository: ReservasRepository,
    private val destinoRepository: DestinoRepository,
    private val availabilityService: AvailabilityService
) {

    fun crear(
        userId: String,
        destinoId: String,
        tourSlotId: String,
        fecha: Date,
        horaInicio: String,
        pax: Int
    ): Booking? {
        val destino = destinoRepository.getDetalle(destinoId) ?: return null

        // Verificar disponibilidad y bloquear asientos
        if (!availabilityService.lockSeats(tourSlotId, pax)) {
            return null
        }

        val precioTotal = destino.precio * pax

        val booking = Booking(
            id = "",
            userId = userId,
            destinoId = destinoId,
            destino = destino,
            fecha = fecha,
            horaInicio = horaInicio,
            numPersonas = pax,
            precioTotal = precioTotal,
            estado = EstadoBooking.PENDIENTE_PAGO
        )

        return reservasRepository.save(booking)
    }

    fun confirmarPago(bookingId: String, payment: String): Booking? {
        val booking = reservasRepository.find(bookingId) ?: return null

        val codigoConfirmacion = "PS${UUID.randomUUID().toString().substring(0, 8).uppercase()}"

        val bookingActualizado = booking.copy(
            estado = EstadoBooking.PAGADA,
            codigoConfirmacion = codigoConfirmacion,
            metodoPago = payment
        )

        return reservasRepository.save(bookingActualizado)
    }

    fun obtenerReserva(bookingId: String): Booking? {
        return reservasRepository.find(bookingId)
    }
}