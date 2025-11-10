package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.ReservasService
import com.grupo4.appreservas.service.DestinoService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ReservasController(
    private val reservasService: ReservasService,
    private val availabilityService: AvailabilityService,
    private val destinoService: DestinoService
) {

    /**
     * Inicia el proceso de reserva para un tour específico.
     * Equivalente a iniciarReserva(tourId) del diagrama UML.
     * 
     * @param tourId ID del tour/destino para el que se inicia la reserva
     * @return Destino si existe, null en caso contrario
     */
    fun iniciarReserva(tourId: String): Destino? {
        return destinoService.obtenerDetalle(tourId)
    }

    fun consultarDisponibilidad(tourSlotId: String): Map<String, Any> {
        return availabilityService.consultarDisponibilidad(tourSlotId)
    }

    fun lockSeats(tourSlotId: String, numPersonas: Int): Boolean {
        return availabilityService.verificarYBloquearCupos(tourSlotId, numPersonas)
    }

    /**
     * Crea una reserva usando un objeto comando.
     * Equivalente a crearReserva(cmd) del diagrama UML.
     * 
     * @param userId ID del usuario
     * @param tourSlotId ID del slot de tour (formato: destinoId_fecha)
     * @param pax Cantidad de pasajeros
     * @param horaInicio Hora de inicio del tour (opcional, por defecto "08:00")
     * @return Reserva creada o null si hay error
     */
    fun crearReservaCmd(userId: String, tourSlotId: String, pax: Int, horaInicio: String = "08:00"): Reserva? {
        val parts = tourSlotId.split("_")
        if (parts.size < 3) {
            // Formato esperado: dest_001_2025-10-14
            return null
        }

        val destinoId = "${parts[0]}_${parts[1]}" // "dest_001"
        val fechaStr = parts[2] // "2025-10-14"

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = dateFormat.parse(fechaStr) ?: Date()

            return reservasService.crear(
                userId = userId,
                destinoId = destinoId,
                tourSlotId = tourSlotId,
                fecha = fecha,
                horaInicio = horaInicio,
                pax = pax
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}


