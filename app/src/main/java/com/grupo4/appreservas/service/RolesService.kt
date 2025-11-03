package com.grupo4.appreservas.service

import android.content.Context
import com.grupo4.appreservas.modelos.Rol
import com.grupo4.appreservas.repository.RolRepository

class RolesService(context: Context) {
    private val rolRepository = RolRepository(context)

    fun asignarRol(usuario: Int, rol: Int): Rol? {
        // En este caso, el rol ya viene asignado en el usuario
        // Este método es para casos futuros de reasignación
        return rolRepository.buscarRolPorId(rol)
    }

    fun obtenerRol(usuarioId: Int): Rol? {
        // Implementar lógica para obtener el rol del usuario
        // Por ahora retorna el rol directamente
        return null
    }
}