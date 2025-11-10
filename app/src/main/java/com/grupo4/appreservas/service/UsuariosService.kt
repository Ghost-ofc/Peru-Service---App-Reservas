package com.grupo4.appreservas.service

import android.content.Context
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.UsuarioRepository

/**
 * Servicio de Usuarios según el diagrama UML.
 * Equivalente a RepositorioUsuarios del diagrama (nivel de servicio).
 */
class UsuariosService(context: Context){
    private val usuarioRepository = UsuarioRepository(context)

    /**
     * Crea un nuevo usuario.
     * Equivalente a crearUsuario(datos): Usuario del diagrama UML.
     * 
     * @param datos Datos del usuario a crear
     * @param rolId Rol del usuario (opcional, por defecto 2 = Turista)
     * @return Usuario creado o null si hay error
     */
    fun crearUsuario(datos: DatosRegistro, rolId: Int = 2): Usuario? {
        // El repositorio ya valida y encripta, solo creamos el usuario con datos básicos
        val nuevoUsuario = Usuario(
            nombreCompleto = datos.nombreCompleto,
            correo = datos.correo,
            contrasena = datos.contrasena, // El repositorio se encargará de encriptarla
            rolId = rolId
        )

        // UsuarioRepository valida duplicados y encripta la contraseña
        return usuarioRepository.crearUsuario(nuevoUsuario)
    }

    /**
     * Valida las credenciales de un usuario.
     * Equivalente a validarCredenciales(correo, contrasena): Usuario del diagrama UML.
     * 
     * @param correo Correo electrónico
     * @param contrasena Contraseña
     * @return Usuario si las credenciales son válidas, null en caso contrario
     */

    fun validarCredenciales(correo: String, contrasena: String): Usuario? {
        // UsuarioRepository ya maneja la validación y encriptación
        return usuarioRepository.validarCredenciales(correo, contrasena)
    }

    data class DatosRegistro(
        val nombreCompleto: String,
        val correo: String,
        val contrasena: String
    )
}