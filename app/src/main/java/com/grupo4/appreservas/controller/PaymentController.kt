package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Payment
import com.grupo4.appreservas.service.BookingService
import com.grupo4.appreservas.service.PaymentService
import com.grupo4.appreservas.service.VoucherService

class PaymentController(
    private val paymentService: PaymentService,
    private val bookingService: BookingService,
    private val voucherService: VoucherService
) {

    suspend fun pagar(bookingId: String, metodo: MetodoPago): Payment? {
        val booking = bookingService.obtenerReserva(bookingId) ?: return null

        val requestData = mapOf(
            "bookingId" to bookingId,
            "monto" to booking.precioTotal
        )

        val payment = when (metodo) {
            MetodoPago.YAPE -> paymentService.payYape(requestData)
            MetodoPago.PLIN -> paymentService.payPlin(requestData)
            MetodoPago.TARJETA -> paymentService.payCard(requestData)
        }

        return payment
    }

    suspend fun process(bookingId: String, metodo: MetodoPago): Map<String, Any> {
        val payment = pagar(bookingId, metodo)

        if (payment != null) {
            // Confirmar el pago en el booking
            val bookingActualizado = bookingService.confirmarPago(bookingId, metodo.name)

            return mapOf(
                "success" to true,
                "paymentId" to payment.id,
                "bookingId" to bookingId,
                "estado" to payment.estado.name
            )
        }

        return mapOf(
            "success" to false,
            "error" to "No se pudo procesar el pago"
        )
    }

    fun generarComprobante(bookingId: String): Map<String, Any>? {
        val voucher = voucherService.emitir(bookingId) ?: return null

        return mapOf(
            "voucher" to voucher,
            "qrCode" to voucher.qrCode
        )
    }
}