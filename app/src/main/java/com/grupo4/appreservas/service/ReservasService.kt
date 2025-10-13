package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.EstadoReserva
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
    ): Reserva? {
        val destino = destinoRepository.getDetalle(destinoId) ?: return null

        // Verificar disponibilidad y bloquear asientos
        if (!availabilityService.lockSeats(tourSlotId, pax)) {
            return null
        }

        val precioTotal = destino.precio * pax

        val reserva = Reserva(
            id = "",
            userId = userId,
            destinoId = destinoId,
            destino = destino,
            fecha = fecha,
            horaInicio = horaInicio,
            numPersonas = pax,
            precioTotal = precioTotal,
            estado = EstadoReserva.PENDIENTE_PAGO
        )

        return reservasRepository.save(reserva)
    }

    fun confirmarPago(bookingId: String, payment: String): Reserva? {
        val booking = reservasRepository.find(bookingId) ?: return null

        val codigoConfirmacion = "PS${UUID.randomUUID().toString().substring(0, 8).uppercase()}"

        val bookingActualizado = booking.copy(
            estado = EstadoReserva.PAGADA,
            codigoConfirmacion = codigoConfirmacion,
            metodoPago = payment
        )

        return reservasRepository.save(bookingActualizado)
    }

    fun obtenerReserva(bookingId: String): Reserva? {
        return reservasRepository.find(bookingId)
    }
}