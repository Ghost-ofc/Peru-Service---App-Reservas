package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Rol

class RolRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun asignarRol(usuario: Int, rol: Int) {
        // Lógica para asignar rol
        // En este caso ya viene asignado al crear el usuario
    }

    fun buscarRolPorId(usuarioId: Int): Rol? {
        return dbHelper.obtenerRol(usuarioId)
    }


}