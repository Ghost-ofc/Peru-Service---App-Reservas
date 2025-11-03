package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.service.RolesService
import com.grupo4.appreservas.service.UsuariosService

class ControlAuth(
    private val servicioUsuarios: UsuariosService,
    private val servicioRoles: RolesService
) {

    fun registrarUsuario(datos: DatosRegistroUI): ResultadoAuth {
        // Validaciones
        if (datos.nombreCompleto.isBlank()) {
            return ResultadoAuth.Error("El nombre completo es requerido")
        }
        if (!esCorreoValido(datos.correo)) {
            return ResultadoAuth.Error("Correo electrónico inválido")
        }
        if (datos.contrasena.length < 6) {
            return ResultadoAuth.Error("La contraseña debe tener al menos 6 caracteres")
        }

        val datosRegistro = UsuariosService.DatosRegistro(
            nombreCompleto = datos.nombreCompleto,
            correo = datos.correo,
            contrasena = datos.contrasena
        )

        val usuario = servicioUsuarios.crearUsuario(datosRegistro)
        return if (usuario != null) {
            ResultadoAuth.Exito(usuario)
        } else {
            ResultadoAuth.Error("El correo ya está registrado")
        }
    }

    fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth {
        if (correo.isBlank() || contrasena.isBlank()) {
            return ResultadoAuth.Error("Correo y contraseña son requeridos")
        }

        val usuario = servicioUsuarios.validarCredenciales(correo, contrasena)
        return if (usuario != null) {
            ResultadoAuth.Exito(usuario)
        } else {
            ResultadoAuth.Error("Credenciales incorrectas")
        }
    }

    fun redirigirPorRol(rol: Int): String {
        return when (rol) {
            1 -> "DashboardAdministrador"
            2 -> "DashboardTurista"
            else -> "Login"
        }
    }

    private fun esCorreoValido(correo: String): Boolean {
        val patronCorreo = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return patronCorreo.matches(correo)
    }

    data class DatosRegistroUI(
        val nombreCompleto: String,
        val correo: String,
        val contrasena: String
    )

    sealed class ResultadoAuth {
        data class Exito(val usuario: Usuario) : ResultadoAuth()
        data class Error(val mensaje: String) : ResultadoAuth()
    }
}