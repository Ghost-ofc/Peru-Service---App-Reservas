package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.service.ReservasService
import com.grupo4.appreservas.service.PagoService
import com.grupo4.appreservas.service.ReciboService

class PagoController(
    private val pagoService: PagoService,
    private val reservasService: ReservasService,
    private val reciboService: ReciboService
) {

    suspend fun pagar(bookingId: String, metodo: MetodoPago): Pago? {
        val booking = reservasService.obtenerReserva(bookingId) ?: return null

        val requestData = mapOf(
            "bookingId" to bookingId,
            "monto" to booking.precioTotal
        )

        val payment = when (metodo) {
            MetodoPago.YAPE -> pagoService.payYape(requestData)
            MetodoPago.PLIN -> pagoService.payPlin(requestData)
            MetodoPago.TARJETA -> pagoService.payCard(requestData)
        }

        return payment
    }

    suspend fun process(bookingId: String, metodo: MetodoPago): Map<String, Any> {
        val booking = reservasService.obtenerReserva(bookingId)
        if (booking == null) {
            return mapOf("success" to false, "error" to "Reserva no encontrada")
        }

        return try {
            val requestData = mapOf(
                "bookingId" to booking.id,
                "monto" to booking.precioTotal
            )

            val payment = when (metodo) {
                MetodoPago.YAPE -> pagoService.payYape(requestData)
                MetodoPago.PLIN -> pagoService.payPlin(requestData)
                MetodoPago.TARJETA -> pagoService.payCard(requestData)
            }

            val bookingActualizado = reservasService.confirmarPago(bookingId, metodo.name)

            mapOf(
                "success" to true,
                "paymentId" to payment.id,
                "bookingId" to bookingId,
                "estado" to payment.estado.name
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to "No se pudo procesar el pago"
            )
        }
    }

    fun generarComprobante(bookingId: String): Map<String, Any>? {
        val voucher = reciboService.emitir(bookingId) ?: return null

        return mapOf(
            "voucher" to voucher,
            "qrCode" to voucher.qrCode
        )
    }
}