package com.grupo4.appreservas.controller

import android.content.Context
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.RepositorioCheckIn
import com.grupo4.appreservas.repository.RepositorioQR
import java.text.SimpleDateFormat
import java.util.*

/**
 * Controlador de Check-In según el diagrama UML.
 * Equivalente a CheckInViewModel del diagrama, pero en arquitectura MVC.
 * 
 * En MVC, este controller actúa como intermediario entre la Vista (EscaneoQRActivity)
 * y los Repositorios (Model).
 */
class ControlCheckIn(
    private val repositorioQR: RepositorioQR,
    private val repositorioCheckIn: RepositorioCheckIn
) {

    /**
     * Procesa el escaneo de un código QR.
     * Equivalente a procesarEscaneoQR(codigo, tourId) del diagrama UML.
     * 
     * @param codigo Código QR escaneado
     * @param tourId ID del tour actual
     * @param guiaId ID del guía que realiza el escaneo
     * @return ResultadoEscaneo con el resultado de la operación
     */
    fun procesarEscaneoQR(codigo: String, tourId: String, guiaId: Int = 1): ResultadoEscaneo {
        android.util.Log.d("ControlCheckIn", "Iniciando procesamiento de escaneo QR: codigo=$codigo, tourId=$tourId, guiaId=$guiaId")
        
        // 1. Validar que el código QR es válido
        if (!repositorioQR.validar(codigo)) {
            android.util.Log.e("ControlCheckIn", "QR no válido: codigo=$codigo")
            return ResultadoEscaneo.Error("QR no válido o no existe")
        }

        // 2. Obtener el ID de la reserva asociada al código QR
        val reservaId = repositorioQR.obtenerReservaId(codigo)
        if (reservaId == null) {
            android.util.Log.e("ControlCheckIn", "No se pudo obtener reservaId del código: codigo=$codigo")
            return ResultadoEscaneo.Error("No se pudo obtener la reserva del QR")
        }
        android.util.Log.d("ControlCheckIn", "ReservaId obtenido: $reservaId")

        // 3. Validar que la reserva pertenece al tour correcto
        if (!repositorioQR.perteneceATour(reservaId, tourId)) {
            android.util.Log.e("ControlCheckIn", "Reserva no pertenece al tour: reservaId=$reservaId, tourId=$tourId")
            return ResultadoEscaneo.Error("Este QR no pertenece a este tour")
        }
        android.util.Log.d("ControlCheckIn", "Reserva pertenece al tour: reservaId=$reservaId, tourId=$tourId")

        // 4. Validar que el código QR no ha sido usado previamente
        if (repositorioQR.estaUsado(codigo)) {
            android.util.Log.w("ControlCheckIn", "QR ya usado: codigo=$codigo")
            return ResultadoEscaneo.Error("QR no válido o ya registrado")
        }
        android.util.Log.d("ControlCheckIn", "QR no ha sido usado previamente: codigo=$codigo")

        // 5. Obtener la reserva completa para mostrar información
        val reserva = repositorioQR.obtenerReserva(codigo)
        if (reserva == null) {
            android.util.Log.e("ControlCheckIn", "No se pudo obtener la reserva: codigo=$codigo")
            return ResultadoEscaneo.Error("No se pudo obtener la información de la reserva")
        }
        android.util.Log.d("ControlCheckIn", "Reserva obtenida: reservaId=${reserva.reservaId}, tourId=${reserva.tourId}")

        // 6. Registrar el check-in
        val horaActual = obtenerHoraActual()
        android.util.Log.d("ControlCheckIn", "Registrando check-in: reservaId=$reservaId, guiaId=$guiaId, hora=$horaActual")
        val exito = repositorioCheckIn.registrar(reservaId, guiaId, horaActual)
        
        if (!exito) {
            android.util.Log.e("ControlCheckIn", "Error al registrar check-in: reservaId=$reservaId, guiaId=$guiaId")
            return ResultadoEscaneo.Error("Error al registrar el check-in")
        }
        
        android.util.Log.d("ControlCheckIn", "Check-in registrado exitosamente: reservaId=$reservaId, guiaId=$guiaId")

        // 7. Marcar el código QR como usado (después de registrar exitosamente)
        repositorioQR.marcarUsado(codigo)
        android.util.Log.d("ControlCheckIn", "QR marcado como usado: codigo=$codigo")

        // 8. Actualizar la reserva con la hora de registro
        val reservaActualizada = reserva.copy(horaRegistro = horaActual)

        // 9. Retornar éxito con la información de la reserva actualizada
        android.util.Log.d("ControlCheckIn", "Procesamiento exitoso: reservaId=$reservaId")
        return ResultadoEscaneo.Exito(reservaActualizada)
    }

    private fun obtenerHoraActual(): String {
        // Usar el mismo formato que la base de datos: "yyyy-MM-dd HH:mm:ss"
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formato.format(Date())
    }

    /**
     * Resultado del procesamiento de escaneo QR.
     * Equivalente a resultadoEscaneo: LiveData<String> del diagrama, pero adaptado a MVC.
     */
    sealed class ResultadoEscaneo {
        data class Exito(val reserva: Reserva) : ResultadoEscaneo()
        data class Error(val mensaje: String) : ResultadoEscaneo()
    }
}

