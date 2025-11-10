package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Rol
import com.grupo4.appreservas.modelos.Usuario

/**
 * Repositorio de Roles según el diagrama UML.
 * Equivalente a RepositorioRoles del diagrama.
 */
class RolRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    /**
     * Asigna un rol a un usuario.
     * Equivalente a asignarRol(usuario, rol) del diagrama UML.
     * 
     * @param usuario Usuario al que se le asignará el rol
     * @param rol Rol a asignar
     */
    fun asignarRol(usuario: Usuario, rol: Rol) {
        // En este sistema, el rol se asigna al crear el usuario
        // Este método está disponible para futuras reasignaciones
        // Por ahora, el rol ya viene en usuario.rolId
    }

    /**
     * Obtiene el rol de un usuario por su ID.
     * Equivalente a obtenerRol(usuarioId): Rol del diagrama UML.
     * 
     * @param usuarioId ID del usuario
     * @return Rol del usuario o null si no se encuentra
     */
    fun obtenerRol(usuarioId: Int): Rol? {
        // Obtener el usuario para acceder a su rolId
        val usuario = dbHelper.buscarUsuarioPorId(usuarioId)
        return usuario?.let {
            dbHelper.obtenerRol(it.rolId)
        }
    }

    /**
     * Busca un rol por su ID.
     * 
     * @param rolId ID del rol
     * @return Rol o null si no se encuentra
     */
    fun buscarRolPorId(rolId: Int): Rol? {
        return dbHelper.obtenerRol(rolId)
    }
}