package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.modelos.Recibo
import com.grupo4.appreservas.service.PaymentService
import com.grupo4.appreservas.service.VoucherService
import kotlinx.coroutines.runBlocking

/**
 * Controlador para el procesamiento de pagos.
 * Equivalente a PaymentController del diagrama UML.
 */
class PaymentController(
    private val paymentService: PaymentService,
    private val voucherService: VoucherService
) {

    /**
     * Procesa un pago para una reserva.
     * Equivalente a pagar(bookingId, metodo) del diagrama UML.
     */
    suspend fun pagar(bookingId: String, metodo: MetodoPago, monto: Double): Pago? {
        val request = mapOf(
            "bookingId" to bookingId,
            "monto" to monto
        )

        return when (metodo) {
            MetodoPago.YAPE -> paymentService.payYapePlin(request)
            MetodoPago.PLIN -> paymentService.payPlin(request)
            MetodoPago.TARJETA -> paymentService.payCard(request)
        }
    }

    /**
     * Genera un comprobante para una reserva.
     * Equivalente a generarComprobante(bookingId) del diagrama UML.
     */
    fun generarComprobante(bookingId: String): Recibo? {
        return voucherService.emitir(bookingId)
    }
}

