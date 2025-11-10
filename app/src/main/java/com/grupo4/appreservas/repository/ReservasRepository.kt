package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.TourSlot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReservasRepository private constructor(private val dbHelper: DatabaseHelper) {

    companion object {
        @Volatile
        private var instance: ReservasRepository? = null

        fun getInstance(context: Context): ReservasRepository {
            return instance ?: synchronized(this) {
                val dbHelper = DatabaseHelper(context)
                instance ?: ReservasRepository(dbHelper).also { instance = it }
            }
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun save(reserva: Reserva): Reserva {
        val nuevoBooking = if (reserva.id.isEmpty()) {
            reserva.copy(id = "BK${UUID.randomUUID().toString().substring(0, 8).uppercase()}")
        } else {
            reserva
        }
        
        // Verificar si la reserva ya existe en la base de datos
        val reservaExistente = dbHelper.obtenerReservaPorId(nuevoBooking.id)
        
        // Si la reserva ya existe, mantener datos existentes importantes
        val reservaActualizada = if (reservaExistente != null) {
            // Mantener código QR existente si ya existe
            val codigoQR = if (reservaExistente.codigoQR.isNotEmpty()) {
                reservaExistente.codigoQR
            } else if (nuevoBooking.codigoQR.isNotEmpty()) {
                nuevoBooking.codigoQR
            } else {
                "QR${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
            }
            
            // Mantener nombre y documento si ya existen, de lo contrario usar los nuevos
            val nombreTurista = if (reservaExistente.nombreTurista.isNotEmpty()) {
                reservaExistente.nombreTurista
            } else {
                nuevoBooking.nombreTurista.ifEmpty { "Usuario" }
            }
            
            val documento = if (reservaExistente.documento.isNotEmpty()) {
                reservaExistente.documento
            } else {
                nuevoBooking.documento.ifEmpty { "" }
            }
            
            nuevoBooking.copy(
                codigoQR = codigoQR,
                codigoConfirmacion = codigoQR,
                nombreTurista = nombreTurista,
                documento = documento,
                // Mantener otros campos existentes si no se proporcionan nuevos
                horaRegistro = nuevoBooking.horaRegistro ?: reservaExistente.horaRegistro
            )
        } else {
            // Nueva reserva: generar código QR si no existe
            val codigoQR = if (nuevoBooking.codigoQR.isNotEmpty()) {
                nuevoBooking.codigoQR
            } else {
                "QR${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
            }
            
            nuevoBooking.copy(
                codigoQR = codigoQR,
                codigoConfirmacion = codigoQR,
                // Asegurar que nombre y documento no estén vacíos
                nombreTurista = nuevoBooking.nombreTurista.ifEmpty { "Usuario" },
                documento = nuevoBooking.documento.ifEmpty { "" }
            )
        }
        
        // Asegurar que tiene hora de registro si no la tiene
        val reservaFinal = if (reservaActualizada.horaRegistro.isNullOrEmpty()) {
            reservaActualizada.copy(horaRegistro = dateTimeFormat.format(Date()))
        } else {
            reservaActualizada
        }
        
        // insertarReserva ahora usa CONFLICT_REPLACE, por lo que actualizará si existe
        dbHelper.insertarReserva(reservaFinal)
        return reservaFinal
    }

    fun find(bookingId: String): Reserva? {
        return dbHelper.obtenerReservaPorId(bookingId)
    }

    fun findByBookingId(bookingId: String): Reserva? {
        return dbHelper.obtenerReservaPorId(bookingId)
    }

    fun obtenerReservasUsuario(userId: String): List<Reserva> {
        val usuarioIdInt = userId.toIntOrNull() ?: return emptyList()
        return dbHelper.obtenerReservasPorUsuario(usuarioIdInt)
    }

    fun obtenerReservaPorQR(codigoQR: String): Reserva? {
        return dbHelper.obtenerReservaPorQR(codigoQR)
    }

    /**
     * Obtiene las reservas confirmadas de un usuario.
     * Equivalente a obtenerReservasConfirmadas(usuarioId) del diagrama UML.
     */
    fun obtenerReservasConfirmadas(usuarioId: Int): List<Reserva> {
        val reservas = dbHelper.obtenerReservasPorUsuario(usuarioId)
        return reservas.filter { it.estaConfirmado() }
    }

    // TourSlot management
    fun saveTourSlot(slot: TourSlot) {
        dbHelper.insertarTourSlot(slot)
    }

    fun findTourSlot(tourSlotId: String): TourSlot? {
        return dbHelper.obtenerTourSlotPorId(tourSlotId)
    }

    fun crearTourSlotSiNoExiste(tourSlotId: String, fecha: Date, capacidad: Int): TourSlot {
        val slotExistente = dbHelper.obtenerTourSlotPorId(tourSlotId)
        return if (slotExistente != null) {
            slotExistente
        } else {
            val nuevoSlot = TourSlot(
                tourSlotId = tourSlotId,
                fecha = fecha,
                capacidad = capacidad,
                ocupados = 0
            )
            dbHelper.insertarTourSlot(nuevoSlot)
            nuevoSlot
        }
    }

    fun actualizarTourSlot(slot: TourSlot): Boolean {
        return dbHelper.actualizarTourSlot(slot) > 0
    }

    fun obtenerTourSlotsPorFecha(fecha: String): List<TourSlot> {
        return dbHelper.obtenerTourSlotsPorFecha(fecha)
    }
}