package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Notificacion

/**
 * Repositorio de Notificaciones según el diagrama UML.
 * Equivalente a RepositorioNotificaciones del diagrama.
 */
class RepositorioNotificaciones(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    /**
     * Obtiene los recordatorios (notificaciones) de un usuario.
     * Equivalente a obtenerRecordatorios(usuarioId): List<Notificacion> del diagrama UML.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones del usuario
     */
    fun obtenerRecordatorios(usuarioId: Int): List<Notificacion> {
        return dbHelper.obtenerNotificacionesPorUsuario(usuarioId)
    }

    /**
     * Envía una notificación push.
     * Equivalente a enviarNotificacionPush(notificacion) del diagrama UML.
     * 
     * @param notificacion Notificación a enviar
     * @return true si se envió correctamente, false en caso contrario
     */
    fun enviarNotificacionPush(notificacion: Notificacion): Boolean {
        // Guardar la notificación en la base de datos
        val resultado = dbHelper.insertarNotificacion(notificacion)
        return resultado != -1L
    }

    /**
     * Obtiene las notificaciones no leídas de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones no leídas
     */
    fun obtenerNotificacionesNoLeidas(usuarioId: Int): List<Notificacion> {
        return dbHelper.obtenerNotificacionesNoLeidasPorUsuario(usuarioId)
    }

    /**
     * Marca una notificación como leída.
     * 
     * @param notificacionId ID de la notificación
     * @return true si se marcó correctamente, false en caso contrario
     */
    fun marcarComoLeida(notificacionId: String): Boolean {
        return dbHelper.marcarNotificacionComoLeida(notificacionId)
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas.
     * 
     * @param usuarioId ID del usuario
     * @return Número de notificaciones marcadas como leídas
     */
    fun marcarTodasComoLeidas(usuarioId: Int): Int {
        return dbHelper.marcarTodasComoLeidas(usuarioId)
    }
}

