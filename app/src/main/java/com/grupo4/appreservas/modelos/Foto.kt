package com.grupo4.appreservas.modelos

import java.util.Date

/**
 * Modelo de datos para las fotos del álbum grupal del tour.
 * Equivalente a Foto del diagrama UML.
 */
data class Foto(
    val idFoto: String,
    val idTour: String,
    val urlImagen: String,
    val nombreAutor: String,
    val fechaSubida: Date,
    val aprobada: Boolean = false // Por defecto no aprobada hasta que se revise
)

