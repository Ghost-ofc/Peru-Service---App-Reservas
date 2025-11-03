package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.DatabaseHelper

class RepositorioQR(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun validar(codigo: String): Boolean {
        val reserva = dbHelper.obtenerReservaPorQR(codigo)
        return reserva != null && reserva.estado == EstadoReserva.PENDIENTE
    }

    fun obtenerReserva(codigo: String): Reserva? {
        return dbHelper.obtenerReservaPorQR(codigo)
    }

    fun estaUsado(codigo: String): Boolean {
        return dbHelper.estaReservaUsada(codigo)
    }

    fun marcarUsado(codigo: String) {
        val reserva = dbHelper.obtenerReservaPorQR(codigo)
        reserva?.let {
            val horaActual = obtenerHoraActual()
            dbHelper.marcarReservaUsada(it.reservaId, horaActual)
        }
    }

    private fun obtenerHoraActual(): String {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return formato.format(java.util.Date())
    }
}