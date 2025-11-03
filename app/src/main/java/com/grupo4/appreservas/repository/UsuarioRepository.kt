package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Usuario
import java.security.MessageDigest

class UsuarioRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun crearUsuario(usuario: Usuario): Usuario? {
        // Verificar si el usuario ya existe
        if (dbHelper.buscarUsuarioPorCorreo(usuario.correo) != null) {
            return null
        }

        val contrasenaEncriptada = encriptarContrasena(usuario.contrasena)
        val nuevoUsuario = Usuario(
            nombreCompleto = usuario.nombreCompleto,
            correo = usuario.correo,
            contrasena = contrasenaEncriptada,
            rolId = usuario.rolId
        )

        val resultado = dbHelper.insertarUsuario(nuevoUsuario)
        return if (resultado != -1L) {
            dbHelper.buscarUsuarioPorCorreo(usuario.correo)
        } else {
            null
        }
    }

    fun validarCredenciales(correo: String, contrasena: String): Usuario? {
        val usuario = dbHelper.buscarUsuarioPorCorreo(correo) ?: return null
        val contrasenaEncriptada = encriptarContrasena(contrasena)

        return if (usuario.contrasena == contrasenaEncriptada) {
            usuario
        } else {
            null
        }
    }

    private fun encriptarContrasena(contrasena: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(contrasena.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    internal fun buscarPorCorreo(correo: String): Usuario? {
        return dbHelper.buscarUsuarioPorCorreo(correo)
    }

}
