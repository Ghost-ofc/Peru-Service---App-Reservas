package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.EstadoPago
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import kotlinx.coroutines.delay
import java.util.Date

/**
 * PaymentGateway maneja la comunicación con la pasarela de pagos externa.
 * Según el diagrama UML, este componente se encarga de realizar el cargo real.
 * 
 * En producción, esta clase se conectaría a una API real de pasarela de pagos
 * (como Stripe, PayPal, o una pasarela local como Culqi, Niubiz, etc.).
 */
class PaymentGateway {

    /**
     * Realiza el cargo con la pasarela de pagos externa.
     * Equivalente a charge(req): Payment del diagrama UML.
     * 
     * @param req Mapa con los datos de la transacción (bookingId, monto, metodoPago)
     * @return Pago con el resultado de la transacción
     */
    suspend fun charge(req: Map<String, Any>): Pago {
        val bookingId = req["bookingId"] as String
        val monto = req["monto"] as Double
        val metodoPago = req["metodoPago"] as? MetodoPago
            ?: throw IllegalArgumentException("Método de pago no especificado")

        // Simular comunicación con pasarela externa
        // En producción, aquí se haría una llamada HTTP a la API de la pasarela
        val delayTime = when (metodoPago) {
            MetodoPago.YAPE, MetodoPago.PLIN -> 1500L // Billeteras digitales: más rápido
            MetodoPago.TARJETA -> 2000L // Tarjeta: requiere más validación
        }

        delay(delayTime)

        // Simular respuesta de la pasarela
        // En producción, aquí se procesaría la respuesta real de la API
        // Por ahora, simulamos que todos los pagos son aprobados
        val transaccionId = "TXN${System.currentTimeMillis()}"

        return Pago(
            id = "", // El ID se generará en el repository
            bookingId = bookingId,
            monto = monto,
            metodoPago = metodoPago,
            estado = EstadoPago.APROBADO, // En producción, esto vendría de la respuesta de la pasarela
            fecha = Date(),
            transaccionId = transaccionId
        )
    }
}

