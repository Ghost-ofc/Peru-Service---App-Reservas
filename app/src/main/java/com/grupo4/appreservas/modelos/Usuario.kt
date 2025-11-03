package com.grupo4.appreservas.modelos

data class Usuario(
    val usuarioId: Int = 0,
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val rolId: Int, // 1 = Administrador, 2 = Turista
    val fechaCreacion: String = ""
)