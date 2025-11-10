package com.grupo4.appreservas.service

import android.content.Context
import com.grupo4.appreservas.modelos.Rol
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.RolRepository

/**
 * Servicio de Roles según el diagrama UML.
 * Equivalente a RepositorioRoles del diagrama (nivel de servicio).
 */
class RolesService(context: Context) {
    private val rolRepository = RolRepository(context)

    /**
     * Asigna un rol a un usuario.
     * Equivalente a asignarRol(usuario, rol) del diagrama UML.
     * 
     * @param usuario Usuario al que se le asignará el rol
     * @param rol Rol a asignar
     * @return Rol asignado o null si hay error
     */
    fun asignarRol(usuario: Usuario, rol: Rol): Rol? {
        // En este sistema, el rol se asigna al crear el usuario
        // Este método está disponible para futuras reasignaciones
        rolRepository.asignarRol(usuario, rol)
        return rol
    }

    /**
     * Obtiene el rol de un usuario por su ID.
     * Equivalente a obtenerRol(usuarioId): Rol del diagrama UML.
     * 
     * @param usuarioId ID del usuario
     * @return Rol del usuario o null si no se encuentra
     */
    fun obtenerRol(usuarioId: Int): Rol? {
        return rolRepository.obtenerRol(usuarioId)
    }
}