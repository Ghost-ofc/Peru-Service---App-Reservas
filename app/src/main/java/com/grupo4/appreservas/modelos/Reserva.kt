package com.grupo4.appreservas.modelos

import java.io.Serializable
import java.util.Date

enum class EstadoReserva {
    PENDIENTE_PAGO,
    PAGADA,
    CONFIRMADA,
    CANCELADA
}

data class Reserva(
    val id: String = "",
    val userId: String,
    val destinoId: String,
    val destino: Destino? = null,
    val fecha: Date,
    val horaInicio: String,
    val numPersonas: Int,
    val precioTotal: Double,
    val estado: EstadoReserva,
    val codigoConfirmacion: String = "",
    val metodoPago: String = "",
    val fechaCreacion: Date = Date()
) : Serializable

data class TourSlot(
    val tourSlotId: String,
    val fecha: Date,
    val capacidad: Int,
    val ocupados: Int
) {
    fun cuposDisponibles(): Int = capacidad - ocupados

    fun tieneCapacidad(pax: Int): Boolean = cuposDisponibles() >= pax
}