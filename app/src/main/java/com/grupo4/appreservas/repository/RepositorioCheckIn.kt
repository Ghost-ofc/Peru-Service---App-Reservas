package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.CheckIn
import com.grupo4.appreservas.repository.DatabaseHelper

class RepositorioCheckIn(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun registrar(reservaId: String, guiaId: Int, hora: String): Boolean {
        val checkIn = CheckIn(
            reservaId = reservaId,
            guiaId = guiaId,
            horaRegistro = hora,
            estado = "Confirmado"
        )
        val resultado = dbHelper.registrarCheckIn(checkIn)
        return resultado != -1L
    }
}