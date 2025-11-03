package com.grupo4.appreservas.modelos

import java.io.Serializable
import java.util.Date

enum class EstadoReserva(val valor: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado");

    companion object {
        fun fromString(valor: String): EstadoReserva {
            return values().find { it.valor == valor } ?: PENDIENTE
        }
    }
}

data class Reserva(
    // IDs
    val id: String = "",  // ID de reserva/booking general
    val reservaId: String = id, // Alias para compatibilidad con BD

    // Relaciones
    val userId: String,
    val usuarioId: Int = userId.toIntOrNull() ?: 0, // Para compatibilidad con BD
    val destinoId: String = "",
    val tourId: String = destinoId, // tourId es el mismo que destinoId en este contexto
    val tourSlotId: String = "", // ID específico del slot de fecha

    // Datos del turista
    val nombreTurista: String = "",
    val documento: String = "",

    // Información del tour
    val destino: Destino? = null,
    val fecha: Date,
    val horaInicio: String,
    val horaRegistro: String? = null, // Hora del check-in

    // Capacidad y pago
    val numPersonas: Int,
    val precioTotal: Double = 0.0,

    // Estado y códigos
    val estado: EstadoReserva,
    val estadoStr: String = estado.valor, // Para BD y adapters
    val codigoConfirmacion: String = "",
    val codigoQR: String = codigoConfirmacion, // QR es el mismo código
    val metodoPago: String = "",

    // Timestamps
    val fechaCreacion: Date = Date()
) : Serializable {

    // Métodos de conveniencia
    fun estaConfirmado(): Boolean = estado == EstadoReserva.CONFIRMADO
    fun estaPendiente(): Boolean = estado == EstadoReserva.PENDIENTE
    fun estaCancelado(): Boolean = estado == EstadoReserva.CANCELADO

    // Para compatibilidad con código existente
    fun getEstadoString(): String = estado.valor
}

data class TourSlot(
    val tourSlotId: String,
    val fecha: Date,
    val capacidad: Int,
    val ocupados: Int
) {
    fun cuposDisponibles(): Int = capacidad - ocupados
    fun tieneCapacidad(pax: Int): Boolean = cuposDisponibles() >= pax
}