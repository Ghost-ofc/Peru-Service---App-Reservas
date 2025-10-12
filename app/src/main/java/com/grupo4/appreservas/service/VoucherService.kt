package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Voucher
import com.grupo4.appreservas.repository.BookingRepository

class VoucherService(
    private val bookingRepository: BookingRepository
) {

    fun emitir(bookingId: String): Voucher? {
        val booking = bookingRepository.find(bookingId) ?: return null
        val destino = booking.destino ?: return null

        return Voucher(
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