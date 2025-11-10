package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Pago
import java.util.UUID

class PagoRepository private constructor(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    companion object {
        @Volatile
        private var instance: PagoRepository? = null

        fun getInstance(context: Context): PagoRepository {
            return instance ?: synchronized(this) {
                instance ?: PagoRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    fun save(payment: Pago): Pago {
        val nuevoPayment = if (payment.id.isEmpty()) {
            payment.copy(
                id = "PAY${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
                transaccionId = if (payment.transaccionId.isEmpty()) {
                    "TXN${System.currentTimeMillis()}"
                } else {
                    payment.transaccionId
                }
            )
        } else {
            payment
        }
        
        dbHelper.insertarPago(nuevoPayment)
        return nuevoPayment
    }

    fun findByBooking(bookingId: String): Pago? {
        return dbHelper.obtenerPagoPorBooking(bookingId)
    }

    fun findByBookingId(bookingId: String): Pago? {
        return findByBooking(bookingId)
    }

    fun findById(pagoId: String): Pago? {
        return dbHelper.obtenerPagoPorId(pagoId)
    }
}