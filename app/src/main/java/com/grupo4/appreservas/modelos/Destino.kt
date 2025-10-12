package com.grupo4.appreservas.modelos

import java.io.Serializable

data class Destino(
    val id: String = "",
    val nombre: String = "",
    val ubicacion: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val duracionHoras: Int = 0,
    val maxPersonas: Int = 0,
    val categorias: List<String> = emptyList(),
    val imagenUrl: String = "",
    val calificacion: Double = 0.0,
    val numReseñas: Int = 0,
    val disponibleTodosDias: Boolean = true,
    val incluye: List<String> = emptyList()
) : Serializable