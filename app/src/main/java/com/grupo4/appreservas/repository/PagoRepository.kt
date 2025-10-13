package com.grupo4.appreservas.repository

import com.grupo4.appreservas.modelos.Pago
import java.util.UUID

class PagoRepository private constructor() {

    private val paymentsCache = mutableMapOf<String, Pago>()

    companion object {
        @Volatile
        private var instance: PagoRepository? = null

        fun getInstance(): PagoRepository {
            return instance ?: synchronized(this) {
                instance ?: PagoRepository().also { instance = it }
            }
        }
    }

    fun save(payment: Pago): Pago {
        val nuevoPayment = if (payment.id.isEmpty()) {
            payment.copy(
                id = "PAY${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
                transaccionId = "TXN${System.currentTimeMillis()}"
            )
        } else {
            payment
        }
        paymentsCache[nuevoPayment.id] = nuevoPayment
        return nuevoPayment
    }

    fun findByBooking(bookingId: String): Pago? {
        return paymentsCache.values.find { it.bookingId == bookingId }
    }

    fun findByBookingId(bookingId: String): Pago? {
        return findByBooking(bookingId)
    }
}