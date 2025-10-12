package com.grupo4.appreservas.repository

import com.grupo4.appreservas.modelos.Payment
import java.util.UUID

class PaymentRepository private constructor() {

    private val paymentsCache = mutableMapOf<String, Payment>()

    companion object {
        @Volatile
        private var instance: PaymentRepository? = null

        fun getInstance(): PaymentRepository {
            return instance ?: synchronized(this) {
                instance ?: PaymentRepository().also { instance = it }
            }
        }
    }

    fun save(payment: Payment): Payment {
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

    fun findByBooking(bookingId: String): Payment? {
        return paymentsCache.values.find { it.bookingId == bookingId }
    }

    fun findByBookingId(bookingId: String): Payment? {
        return findByBooking(bookingId)
    }
}