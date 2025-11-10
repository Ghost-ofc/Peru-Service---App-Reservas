package com.grupo4.appreservas.service

import android.content.Context
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.DatabaseHelper
import java.util.Date
import java.util.UUID

class ReservasService(
    private val reservasRepository: ReservasRepository,
    private val destinoRepository: DestinoRepository,
    private val availabilityService: AvailabilityService,
    private val context: Context
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

        // Obtener datos del usuario para la reserva
        val usuarioIdInt = userId.toIntOrNull() ?: 0
        val dbHelper = DatabaseHelper(context)
        val usuario = dbHelper.buscarUsuarioPorId(usuarioIdInt)
        val nombreTurista = usuario?.nombreCompleto ?: "Usuario"
        val documento = usuario?.correo ?: "" // Usar correo como documento temporal

        // Construir tourId completo con formato: destinoId_fecha (ej: "dest_001_2025-11-10")
        // Esto permite obtener el tour completo con fecha y hora
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val fechaStr = dateFormat.format(fecha)
        val tourIdCompleto = "${destinoId}_$fechaStr"
        
        val reserva = Reserva(
            id = "",
            userId = userId,
            destinoId = destinoId,
            tourId = tourIdCompleto, // Usar tourId completo con fecha
            tourSlotId = tourSlotId, // Incluir tourSlotId para referencia
            destino = destino,
            fecha = fecha,
            horaInicio = horaInicio,
            numPersonas = pax,
            precioTotal = precioTotal,
            estado = EstadoReserva.PENDIENTE,
            nombreTurista = nombreTurista,
            documento = documento
        )

        return reservasRepository.save(reserva)
    }

    fun confirmarPago(bookingId: String, payment: String): Reserva? {
        val booking = reservasRepository.find(bookingId) ?: return null

        // Si la reserva ya tiene un código QR, mantenerlo; si no, generar uno nuevo
        val codigoConfirmacion = if (booking.codigoQR.isNotEmpty()) {
            booking.codigoQR
        } else {
            "QR${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        }

        // Asegurar que nombre y documento no estén vacíos
        val nombreTurista = if (booking.nombreTurista.isNotEmpty()) {
            booking.nombreTurista
        } else {
            // Obtener del usuario si no está en la reserva
            val usuarioIdInt = booking.usuarioId
            val dbHelper = DatabaseHelper(context)
            val usuario = dbHelper.buscarUsuarioPorId(usuarioIdInt)
            usuario?.nombreCompleto ?: "Usuario"
        }
        
        val documento = if (booking.documento.isNotEmpty()) {
            booking.documento
        } else {
            // Obtener del usuario si no está en la reserva
            val usuarioIdInt = booking.usuarioId
            val dbHelper = DatabaseHelper(context)
            val usuario = dbHelper.buscarUsuarioPorId(usuarioIdInt)
            usuario?.correo ?: ""
        }

        val bookingActualizado = booking.copy(
            estado = EstadoReserva.CONFIRMADO,
            codigoConfirmacion = codigoConfirmacion,
            codigoQR = codigoConfirmacion, // Asegurar que ambos códigos sean iguales
            metodoPago = payment,
            nombreTurista = nombreTurista,
            documento = documento
        )

        return reservasRepository.save(bookingActualizado)
    }

    fun obtenerReserva(bookingId: String): Reserva? {
        return reservasRepository.find(bookingId)
    }
}