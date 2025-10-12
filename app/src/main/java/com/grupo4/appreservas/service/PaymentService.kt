package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.EstadoPago
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Payment
import com.grupo4.appreservas.repository.BookingRepository
import com.grupo4.appreservas.repository.PaymentRepository
import kotlinx.coroutines.delay

class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository
) {

    suspend fun payYape(req: Map<String, Any>): Payment {
        // Simular procesamiento con pasarela
        delay(1500)

        val bookingId = req["bookingId"] as String
        val monto = req["monto"] as Double

        val payment = Payment(
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO
        )

        return paymentRepository.save(payment)
    }

    suspend fun payPlin(req: Map<String, Any>): Payment {
        delay(1500)

        val bookingId = req["bookingId"] as String
        val monto = req["monto"] as Double

        val payment = Payment(
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.PLIN,
            estado = EstadoPago.APROBADO
        )

        return paymentRepository.save(payment)
    }

    suspend fun payCard(req: Map<String, Any>): Payment {
        delay(2000)

        val bookingId = req["bookingId"] as String
        val monto = req["monto"] as Double

        val payment = Payment(
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.TARJETA,
            estado = EstadoPago.APROBADO
        )

        return paymentRepository.save(payment)
    }
}