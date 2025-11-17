package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Recibo
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.PeruvianServiceRepository

/**
 * Servicio para emisión de vouchers/comprobantes.
 * Equivalente a VoucherService del diagrama UML.
 */
class VoucherService(
    private val repository: PeruvianServiceRepository,
    private val qrService: QRService
) {

    /**
     * Emite un voucher para una reserva.
     * Equivalente a emitir(bookingId): Voucher del diagrama UML.
     */
    fun emitir(bookingId: String): Recibo? {
        val reserva = repository.buscarReservaPorId(bookingId) ?: return null

        // Generar código QR
        val qrCode = qrService.generate(reserva.codigoQR)

        // Obtener método de pago del pago asociado
        val pago = repository.buscarPagoPorBooking(bookingId)
        val metodoPagoStr = pago?.metodoPago?.name ?: reserva.metodoPago

        // Crear el recibo/voucher
        return Recibo(
            bookingId = reserva.id,
            codigoConfirmacion = reserva.codigoConfirmacion,
            qrCode = qrCode,
            destinoNombre = reserva.destino?.nombre ?: "",
            fecha = reserva.fecha,
            numPersonas = reserva.numPersonas,
            montoTotal = reserva.precioTotal,
            metodoPago = metodoPagoStr,
            horaInicio = reserva.horaInicio
        )
    }
}

