package com.grupo4.appreservas.modelos

data class Tour(
    val tourId: String,
    val nombre: String,
    val fecha: String,
    val hora: String,
    val puntoEncuentro: String,
    val capacidad: Int,
    val participantesConfirmados: Int = 0,
    val estado: String = "Pendiente" // Pendiente, En Curso, Completado
)