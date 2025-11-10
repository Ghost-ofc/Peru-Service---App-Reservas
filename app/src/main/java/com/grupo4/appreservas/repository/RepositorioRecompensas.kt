package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Logro
import com.grupo4.appreservas.modelos.PuntosUsuario

/**
 * Repositorio para manejar recompensas, puntos y logros.
 * Según HU-007 y el diagrama UML.
 */
class RepositorioRecompensas(private val context: Context) {

    private val dbHelper: DatabaseHelper by lazy { DatabaseHelper(context) }

    /**
     * Obtiene los puntos acumulados de un usuario.
     * Equivalente a obtenerPuntos(usuarioId) del diagrama UML.
     */
    fun obtenerPuntos(usuarioId: Int): Int {
        return dbHelper.obtenerPuntos(usuarioId)
    }

    /**
     * Suma puntos a un usuario.
     * Equivalente a sumarPuntos(usuarioId, puntos) del diagrama UML.
     */
    fun sumarPuntos(usuarioId: Int, puntos: Int): Boolean {
        // Inicializar puntos si el usuario no tiene registro
        dbHelper.inicializarPuntos(usuarioId)
        return dbHelper.sumarPuntos(usuarioId, puntos)
    }

    /**
     * Obtiene todos los logros de un usuario (desbloqueados y no desbloqueados).
     * Equivalente a obtenerLogros(usuarioId) del diagrama UML.
     */
    fun obtenerLogros(usuarioId: Int): List<Logro> {
        return dbHelper.obtenerLogros(usuarioId)
    }

    /**
     * Obtiene solo los logros desbloqueados de un usuario.
     */
    fun obtenerLogrosDesbloqueados(usuarioId: Int): List<Logro> {
        return dbHelper.obtenerLogrosDesbloqueados(usuarioId)
    }

    /**
     * Inserta o actualiza un logro para un usuario.
     */
    fun insertarLogro(usuarioId: Int, logro: Logro): Long {
        return dbHelper.insertarLogroParaUsuario(usuarioId, logro)
    }

    /**
     * Obtiene la información completa de puntos del usuario (incluyendo nivel).
     */
    fun obtenerPuntosUsuario(usuarioId: Int): PuntosUsuario {
        val puntos = obtenerPuntos(usuarioId)
        val nivel = PuntosUsuario.calcularNivel(puntos)
        val puntosParaSiguiente = PuntosUsuario.calcularPuntosParaSiguienteNivel(puntos)
        
        return PuntosUsuario(
            usuarioId = usuarioId,
            puntosAcumulados = puntos,
            nivel = nivel,
            puntosParaSiguienteNivel = puntosParaSiguiente
        )
    }

    /**
     * Verifica si un logro ya existe para un usuario.
     */
    fun existeLogro(usuarioId: Int, logroId: String): Boolean {
        return dbHelper.existeLogro(usuarioId, logroId)
    }

    /**
     * Obtiene el número de reservas confirmadas de un usuario.
     */
    fun obtenerNumeroReservasConfirmadas(usuarioId: Int): Int {
        return dbHelper.obtenerNumeroReservasConfirmadas(usuarioId)
    }
}

