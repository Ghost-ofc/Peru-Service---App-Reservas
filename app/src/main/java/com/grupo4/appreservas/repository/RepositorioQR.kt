package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.service.QRService

/**
 * Repositorio de QR según el diagrama UML.
 * Equivalente a RepositorioQR del diagrama.
 */
class RepositorioQR(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val qrService = QRService()

    /**
     * Valida si un código QR es válido.
     * Equivalente a validar(codigo): Boolean del diagrama UML.
     * 
     * @param codigo Código QR escaneado (puede ser "RESERVA:<reservaId>" o reservaId directamente)
     * @return true si el código es válido, false en caso contrario
     */
    fun validar(codigo: String): Boolean {
        // Primero extraer el reservaId del código QR si tiene formato "RESERVA:<reservaId>"
        val reservaId = qrService.extraerReservaId(codigo) ?: codigo
        android.util.Log.d("RepositorioQR", "Validando QR: codigo=$codigo, reservaId extraído=$reservaId")
        
        // Intentar buscar la reserva por reservaId
        val reservaPorId = dbHelper.obtenerReservaPorId(reservaId)
        if (reservaPorId != null) {
            android.util.Log.d("RepositorioQR", "Reserva encontrada por reservaId: ${reservaPorId.reservaId}, estado=${reservaPorId.estado}")
            if (reservaPorId.estado != EstadoReserva.CANCELADO) {
                return true
            } else {
                android.util.Log.w("RepositorioQR", "Reserva está cancelada: ${reservaPorId.reservaId}")
            }
        } else {
            android.util.Log.w("RepositorioQR", "Reserva no encontrada por reservaId: $reservaId")
        }
        
        // Si no se encuentra, buscar por código QR (formato antiguo: código de confirmación)
        val reserva = dbHelper.obtenerReservaPorQR(codigo)
        if (reserva != null) {
            android.util.Log.d("RepositorioQR", "Reserva encontrada por código QR: ${reserva.reservaId}, estado=${reserva.estado}")
            return reserva.estado != EstadoReserva.CANCELADO
        } else {
            android.util.Log.w("RepositorioQR", "Reserva no encontrada por código QR: $codigo")
        }
        
        return false
    }

    /**
     * Obtiene el ID de la reserva asociada a un código QR.
     * Equivalente a obtenerReservaId(codigo): String del diagrama UML.
     * 
     * @param codigo Código QR escaneado (puede ser "RESERVA:<reservaId>" o reservaId directamente)
     * @return ID de la reserva o null si no se encuentra
     */
    fun obtenerReservaId(codigo: String): String? {
        // Primero extraer el reservaId del código QR si tiene formato "RESERVA:<reservaId>"
        val reservaId = qrService.extraerReservaId(codigo) ?: codigo
        
        // Intentar buscar la reserva por reservaId
        val reservaPorId = dbHelper.obtenerReservaPorId(reservaId)
        if (reservaPorId != null) {
            return reservaPorId.reservaId
        }
        
        // Si no se encuentra, buscar por código QR (formato antiguo: código de confirmación)
        val reserva = dbHelper.obtenerReservaPorQR(codigo)
        return reserva?.reservaId
    }

    /**
     * Verifica si una reserva pertenece a un tour específico.
     * Equivalente a perteneceATour(reservaId, tourId): Boolean del diagrama UML.
     * 
     * @param reservaId ID de la reserva
     * @param tourId ID del tour (formato: "dest_001_2025-11-10")
     * @return true si la reserva pertenece al tour, false en caso contrario
     */
    fun perteneceATour(reservaId: String, tourId: String): Boolean {
        val reserva = dbHelper.obtenerReservaPorId(reservaId)
        if (reserva == null) {
            android.util.Log.e("RepositorioQR", "Reserva no encontrada: reservaId=$reservaId")
            return false
        }
        
        android.util.Log.d("RepositorioQR", "Comparando tourId: reserva.tourId=${reserva.tourId}, tourId=$tourId")
        
        // Comparar tourId completo de la reserva con el tourId del tour
        // Si la reserva tiene tourId con formato destinoId_fecha, debe coincidir exactamente
        // Si la reserva tiene solo destinoId, extraer destinoId del tourId y comparar
        if (reserva.tourId.contains("_") && reserva.tourId.count { it == '_' } >= 2) {
            // La reserva tiene formato destinoId_fecha, comparar directamente
            val coincide = reserva.tourId == tourId
            android.util.Log.d("RepositorioQR", "Comparación directa: ${reserva.tourId} == $tourId = $coincide")
            return coincide
        } else {
            // La reserva tiene solo destinoId, extraer destinoId del tourId y comparar
            val destinoIdDelTour = if (tourId.contains("_") && tourId.count { it == '_' } >= 2) {
                val partes = tourId.split("_")
                "${partes[0]}_${partes[1]}"
            } else {
                tourId
            }
            val coincide = reserva.tourId == destinoIdDelTour || reserva.destinoId == destinoIdDelTour
            android.util.Log.d("RepositorioQR", "Comparación por destinoId: reserva.tourId=${reserva.tourId}, reserva.destinoId=${reserva.destinoId}, destinoIdDelTour=$destinoIdDelTour, coincide=$coincide")
            return coincide
        }
    }

    /**
     * Verifica si un código QR ya ha sido usado.
     * Equivalente a estaUsado(codigo): Boolean del diagrama UML.
     * 
     * @param codigo Código QR escaneado (puede ser "RESERVA:<reservaId>" o reservaId directamente)
     * @return true si el código ya fue usado, false en caso contrario
     */
    fun estaUsado(codigo: String): Boolean {
        // Primero extraer el reservaId del código QR si tiene formato "RESERVA:<reservaId>"
        val reservaId = qrService.extraerReservaId(codigo) ?: codigo
        android.util.Log.d("RepositorioQR", "Verificando si QR está usado: codigo=$codigo, reservaId=$reservaId")
        
        // Verificar si la reserva ya fue usada (tiene check-in registrado)
        val estaUsado = dbHelper.estaReservaUsada(reservaId)
        android.util.Log.d("RepositorioQR", "QR usado: $estaUsado (reservaId=$reservaId)")
        return estaUsado
    }

    /**
     * Marca un código QR como usado.
     * Equivalente a marcarUsado(codigo) del diagrama UML.
     * 
     * @param codigo Código QR escaneado (puede ser "RESERVA:<reservaId>" o reservaId directamente)
     */
    fun marcarUsado(codigo: String) {
        // Primero extraer el reservaId del código QR si tiene formato "RESERVA:<reservaId>"
        val reservaId = qrService.extraerReservaId(codigo) ?: codigo
        
        // Marcar la reserva como usada (actualizar estado y hora)
        val reserva = dbHelper.obtenerReservaPorId(reservaId)
        if (reserva != null) {
            val horaActual = obtenerHoraActual()
            dbHelper.marcarReservaUsada(reserva.reservaId, horaActual)
        } else {
            // Si no se encuentra por reservaId, intentar buscar por código QR (formato antiguo)
            val reservaPorQR = dbHelper.obtenerReservaPorQR(codigo)
            reservaPorQR?.let {
                val horaActual = obtenerHoraActual()
                dbHelper.marcarReservaUsada(it.reservaId, horaActual)
            }
        }
    }

    /**
     * Obtiene la reserva completa asociada a un código QR.
     * 
     * @param codigo Código QR escaneado (puede ser "RESERVA:<reservaId>" o reservaId directamente)
     * @return Reserva o null si no se encuentra
     */
    fun obtenerReserva(codigo: String): Reserva? {
        // Primero extraer el reservaId del código QR si tiene formato "RESERVA:<reservaId>"
        val reservaId = qrService.extraerReservaId(codigo) ?: codigo
        
        // Intentar buscar la reserva por reservaId
        val reservaPorId = dbHelper.obtenerReservaPorId(reservaId)
        if (reservaPorId != null) {
            return reservaPorId
        }
        
        // Si no se encuentra, buscar por código QR (formato antiguo: código de confirmación)
        return dbHelper.obtenerReservaPorQR(codigo)
    }

    private fun obtenerHoraActual(): String {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return formato.format(java.util.Date())
    }
}