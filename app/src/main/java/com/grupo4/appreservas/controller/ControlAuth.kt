package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.modelos.Rol
import com.grupo4.appreservas.service.RolesService
import com.grupo4.appreservas.service.UsuariosService

/**
 * Controlador de Autenticación según el diagrama UML.
 * Equivalente a AutenticacionViewModel del diagrama, pero en arquitectura MVC.
 * 
 * En MVC, este controller actúa como intermediario entre la Vista (Activities)
 * y los Servicios/Repositorios (Model).
 */
class ControlAuth(
    private val servicioUsuarios: UsuariosService,
    private val servicioRoles: RolesService
) {

    /**
     * Registra un nuevo usuario.
     * Equivalente a registrar(nombre, correo, contrasena, rol) del diagrama UML.
     * 
     * @param nombre Nombre completo del usuario
     * @param correo Correo electrónico
     * @param contrasena Contraseña
     * @param rol Rol del usuario (1 = Administrador, 2 = Turista)
     * @return ResultadoAuth con el usuario creado o error
     */
    fun registrar(nombre: String, correo: String, contrasena: String, rol: Int = 2): ResultadoAuth {
        // Validaciones
        if (nombre.isBlank()) {
            return ResultadoAuth.Error("El nombre completo es requerido")
        }
        if (!esCorreoValido(correo)) {
            return ResultadoAuth.Error("Correo electrónico inválido")
        }
        if (contrasena.length < 6) {
            return ResultadoAuth.Error("La contraseña debe tener al menos 6 caracteres")
        }

        val datosRegistro = UsuariosService.DatosRegistro(
            nombreCompleto = nombre,
            correo = correo,
            contrasena = contrasena
        )

        // Crear usuario usando UsuariosService con el rol especificado
        val usuario = servicioUsuarios.crearUsuario(datosRegistro, rol)
        
        return if (usuario != null) {
            // Asignar rol usando RolesService (según diagrama UML)
            val rolObj = servicioRoles.obtenerRol(usuario.rolId)
            if (rolObj != null) {
                servicioRoles.asignarRol(usuario, rolObj)
            }
            ResultadoAuth.Exito(usuario)
        } else {
            ResultadoAuth.Error("El correo ya está registrado")
        }
    }

    /**
     * Inicia sesión con credenciales.
     * Equivalente a iniciarSesion(correo, contrasena) del diagrama UML.
     * 
     * @param correo Correo electrónico
     * @param contrasena Contraseña
     * @return ResultadoAuth con el usuario autenticado o error
     */
    fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth {
        if (correo.isBlank() || contrasena.isBlank()) {
            return ResultadoAuth.Error("Correo y contraseña son requeridos")
        }

        // Validar credenciales usando UsuariosService
        val usuario = servicioUsuarios.validarCredenciales(correo, contrasena)
        
        return if (usuario != null) {
            // Obtener rol del usuario usando RolesService (según diagrama UML)
            val rol = servicioRoles.obtenerRol(usuario.usuarioId)
            ResultadoAuth.Exito(usuario)
        } else {
            ResultadoAuth.Error("Credenciales incorrectas")
        }
    }

    /**
     * Obtiene el rol de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Rol del usuario o null
     */
    fun obtenerRol(usuarioId: Int): Rol? {
        return servicioRoles.obtenerRol(usuarioId)
    }

    /**
     * Redirige según el rol del usuario.
     * 
     * @param rolId ID del rol
     * @return Nombre de la actividad destino
     */
    fun redirigirPorRol(rolId: Int): String {
        return when (rolId) {
            1 -> "PanelGuiaActivity" // Administrador
            2 -> "CatalogoActivity"  // Turista
            else -> "LoginActivity"
        }
    }

    private fun esCorreoValido(correo: String): Boolean {
        val patronCorreo = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return patronCorreo.matches(correo)
    }

    // Método de compatibilidad con código existente
    fun registrarUsuario(datos: DatosRegistroUI): ResultadoAuth {
        return registrar(datos.nombreCompleto, datos.correo, datos.contrasena, 2)
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