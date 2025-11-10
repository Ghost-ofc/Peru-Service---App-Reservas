package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Recibo
import com.grupo4.appreservas.repository.ReservasRepository

/**
 * ReciboService (VoucherService) maneja la generación de comprobantes.
 * Utiliza QRService para generar códigos QR según el diagrama UML.
 */
class ReciboService(
    private val reservasRepository: ReservasRepository,
    private val qrService: QRService
) {

    /**
     * Emite un comprobante para una reserva.
     * Equivalente a emitir(bookingId): Voucher del diagrama UML.
     * 
     * @param bookingId ID de la reserva
     * @return Recibo (Voucher) con los datos de la reserva y QR generado
     */
    fun emitir(bookingId: String): Recibo? {
        val booking = reservasRepository.find(bookingId) ?: return null
        val destino = booking.destino ?: return null

        // Usar QRService para generar el código QR con el reservaId
        // Esto permite que el guía pueda escanearlo y registrar el check-in
        val qrCode = qrService.generate(bookingId)

        return Recibo(
            bookingId = bookingId,
            codigoConfirmacion = booking.codigoConfirmacion,
            qrCode = qrCode,
            destinoNombre = destino.nombre,
            fecha = booking.fecha,
            numPersonas = booking.numPersonas,
            horaInicio = booking.horaInicio,
            montoTotal = booking.precioTotal,
            metodoPago = booking.metodoPago
        )
    }
}