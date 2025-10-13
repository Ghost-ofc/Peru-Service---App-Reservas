package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Recibo
import com.grupo4.appreservas.repository.ReservasRepository

class ReciboService(
    private val reservasRepository: ReservasRepository
) {

    fun emitir(bookingId: String): Recibo? {
        val booking = reservasRepository.find(bookingId) ?: return null
        val destino = booking.destino ?: return null

        return Recibo(
            bookingId = bookingId,
            codigoConfirmacion = booking.codigoConfirmacion,
            qrCode = generarQRData(booking.codigoConfirmacion),
            destinoNombre = destino.nombre,
            fecha = booking.fecha,
            numPersonas = booking.numPersonas,
            horaInicio = booking.horaInicio,
            montoTotal = booking.precioTotal,
            metodoPago = booking.metodoPago

        )
    }

    private fun generarQRData(codigo: String): String {
        // En una implementación real, esto generaría un QR real
        return "QR_DATA_$codigo"
    }
}