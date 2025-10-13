package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.EstadoPago
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.PagoRepository
import kotlinx.coroutines.delay

class PagoService(
    private val pagoRepository: PagoRepository,
    private val reservasRepository: ReservasRepository
) {

    suspend fun payYape(req: Map<String, Any>): Pago {
        // Simular procesamiento con pasarela
        delay(1500)

        val bookingId = req["bookingId"] as String
        val monto = req["monto"] as Double

        val payment = Pago(
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO
        )

        return pagoRepository.save(payment)
    }

    suspend fun payPlin(req: Map<String, Any>): Pago {
        delay(1500)

        val bookingId = req["bookingId"] as String
        val monto = req["monto"] as Double

        val payment = Pago(
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.PLIN,
            estado = EstadoPago.APROBADO
        )

        return pagoRepository.save(payment)
    }

    suspend fun payCard(req: Map<String, Any>): Pago {
        delay(2000)

        val bookingId = req["bookingId"] as String
        val monto = req["monto"] as Double

        val payment = Pago(
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.TARJETA,
            estado = EstadoPago.APROBADO
        )

        return pagoRepository.save(payment)
    }
}