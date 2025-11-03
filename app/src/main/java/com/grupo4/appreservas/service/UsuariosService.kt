package com.grupo4.appreservas.service

import android.content.Context
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.UsuarioRepository
import java.security.MessageDigest

class UsuariosService(context: Context){
    private val usuarioRepository = UsuarioRepository(context)

    fun crearUsuario(datos: DatosRegistro): Usuario? {
        // Validar que el correo no exista
        if (usuarioRepository.buscarPorCorreo(datos.correo) != null) {
            return null // Usuario ya existe
        }

        val contrasenaEncriptada = encriptarContrasena(datos.contrasena)
        val nuevoUsuario = Usuario(
            nombreCompleto = datos.nombreCompleto,
            correo = datos.correo,
            contrasena = contrasenaEncriptada,
            rolId = 2 // Siempre Turista para registro público
        )

        return if (usuarioRepository.crearUsuario(nuevoUsuario?: return null) != null) {
            usuarioRepository.buscarPorCorreo(datos.correo)
        } else {
            null
        }
    }

    fun validarCredenciales(correo: String, contrasena: String): Usuario? {
        val usuario = usuarioRepository.buscarPorCorreo(correo) ?: return null
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

    data class DatosRegistro(
        val nombreCompleto: String,
        val correo: String,
        val contrasena: String
    )
}