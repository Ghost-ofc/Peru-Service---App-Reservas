package com.grupo4.appreservas.viewmodel

import android.content.Context
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.RepositorioCheckIn
import com.grupo4.appreservas.repository.RepositorioQR

class CheckInViewModel(context: Context) {
    private val repositorioQR = RepositorioQR(context)
    private val repositorioCheckIn = RepositorioCheckIn(context)

    fun procesarEscaneoQR(codigo: String, tourId: String): ResultadoEscaneo {
        // Validar que el código existe
        val reserva = repositorioQR.obtenerReserva(codigo)
            ?: return ResultadoEscaneo.Error("QR no válido o no existe")

        // Validar que pertenece al tour correcto
        if (reserva.tourId != tourId) {
            return ResultadoEscaneo.Error("Este QR no pertenece a este tour")
        }

        // Validar que no esté ya usado
        if (repositorioQR.estaUsado(codigo)) {
            return ResultadoEscaneo.Error("QR no válido o ya registrado")
        }

        // Marcar como usado
        repositorioQR.marcarUsado(codigo)

        // Registrar check-in
        val horaActual = obtenerHoraActual()
        val guiaId = 1 // Obtener del contexto de sesión
        repositorioCheckIn.registrar(reserva.reservaId, guiaId, horaActual)

        return ResultadoEscaneo.Exito(reserva)
    }

    private fun obtenerHoraActual(): String {
        val formato = java.text.SimpleDateFormat("HH:mm a", java.util.Locale.getDefault())
        return formato.format(java.util.Date())
    }

    sealed class ResultadoEscaneo {
        data class Exito(val reserva: Reserva) : ResultadoEscaneo()
        data class Error(val mensaje: String) : ResultadoEscaneo()
    }
}