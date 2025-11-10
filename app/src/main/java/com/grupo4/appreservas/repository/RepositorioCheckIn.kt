package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.CheckIn
import com.grupo4.appreservas.repository.DatabaseHelper

/**
 * Repositorio de Check-In según el diagrama UML.
 * Equivalente a RepositorioCheckIn del diagrama.
 */
class RepositorioCheckIn(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    /**
     * Registra un check-in de un turista.
     * Equivalente a registrar(reservaId, guiaId, hora): Boolean del diagrama UML.
     * 
     * @param reservaId ID de la reserva
     * @param guiaId ID del guía que realiza el check-in
     * @param hora Hora del registro
     * @return true si el registro fue exitoso, false en caso contrario
     */
    fun registrar(reservaId: String, guiaId: Int, hora: String): Boolean {
        android.util.Log.d("RepositorioCheckIn", "Intentando registrar check-in: reservaId=$reservaId, guiaId=$guiaId, hora=$hora")
        val checkIn = CheckIn(
            reservaId = reservaId,
            guiaId = guiaId,
            horaRegistro = hora,
            estado = "Confirmado"
        )
        val resultado = dbHelper.registrarCheckIn(checkIn)
        val exito = resultado != -1L
        android.util.Log.d("RepositorioCheckIn", "Resultado del registro: $exito (resultado=$resultado)")
        return exito
    }
}