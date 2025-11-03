package com.grupo4.appreservas.modelos

data class CheckIn(
    val checkInId: Int = 0,
    val reservaId: String,
    val guiaId: Int,
    val horaRegistro: String,
    val estado: String = "Confirmado"
)