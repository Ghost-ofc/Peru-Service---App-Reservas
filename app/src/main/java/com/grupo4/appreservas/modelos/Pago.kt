package com.grupo4.appreservas.modelos

import java.util.Date

enum class EstadoPago {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    CANCELADO
}

enum class MetodoPago {
    YAPE,
    PLIN,
    TARJETA
}

data class Pago(
    val id: String = "",
    val bookingId: String,
    val monto: Double,
    val metodoPago: MetodoPago,
    val estado: EstadoPago,
    val fecha: Date = Date(),
    val transaccionId: String = "",

)

data class Recibo(
    val bookingId: String,
    val codigoConfirmacion: String,
    val qrCode: String,
    val destinoNombre: String,
    val fecha: Date,
    val numPersonas: Int,
    val montoTotal: Double,
    val metodoPago: String,
    val horaInicio: String
) : java.io.Serializable
