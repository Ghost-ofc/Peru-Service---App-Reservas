package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.EstadoPago
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import kotlinx.coroutines.delay
import java.util.*

/**
 * Gateway de pago que simula la comunicación con pasarelas de pago externas.
 * Equivalente a PaymentGateway del diagrama UML.
 */
class PaymentGateway {

    /**
     * Procesa un cargo a través de la pasarela de pago.
     * Equivalente a charge(req): Payment del diagrama UML.
     */
    suspend fun charge(request: Map<String, Any>): Pago {
        // Simular delay de red
        delay(1000)

        val bookingId = request["bookingId"] as? String ?: ""
        val monto = request["monto"] as? Double ?: 0.0
        val metodoPago = request["metodoPago"] as? MetodoPago ?: MetodoPago.TARJETA

        // Simular éxito o fallo (90% éxito, 10% fallo para pruebas)
        val exito = Math.random() > 0.1

        val estado = if (exito) {
            EstadoPago.APROBADO
        } else {
            EstadoPago.RECHAZADO
        }

        val transaccionId = if (exito) {
            "TXN_${System.currentTimeMillis()}"
        } else {
            ""
        }

        return Pago(
            id = "",
            bookingId = bookingId,
            monto = monto,
            metodoPago = metodoPago,
            estado = estado,
            fecha = Date(),
            transaccionId = transaccionId
        )
    }
}

