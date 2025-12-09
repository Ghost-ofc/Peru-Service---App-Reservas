package com.grupo4.appreservas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para la autenticación de usuarios.
 * Equivalente a AutenticacionViewModel del diagrama UML.
 */
class AutenticacionViewModel(
    private val repository: PeruvianServiceRepository
) : ViewModel() {

    // LiveData para el usuario autenticado
    private val _usuarioAutenticado = MutableLiveData<Usuario?>()
    val usuarioAutenticado: LiveData<Usuario?> = _usuarioAutenticado

    // LiveData para mensajes de estado
    private val _mensajeEstado = MutableLiveData<String?>()
    val mensajeEstado: LiveData<String?> = _mensajeEstado

    /**
     * Registra un nuevo usuario.
     * Equivalente a registrarUsuario(nombreCompleto, nombreUsuario, contrasena) del diagrama UML.
     * Si no se proporciona un rol, se asigna el rol por defecto (turista).
     */
    fun registrarUsuario(nombreCompleto: String, nombreUsuario: String, contrasena: String, rolOpcional: Int? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Si no viene un rol, asignar el rol por defecto (turista = 2)
                    val rolId = rolOpcional ?: 2
                    val usuario = repository.crearUsuario(nombreCompleto, nombreUsuario, contrasena, rolId)
                    _usuarioAutenticado.postValue(usuario)
                    _mensajeEstado.postValue(null) // Limpiar mensaje de error si existe
                } catch (e: Exception) {
                    _mensajeEstado.postValue("Error al registrar usuario: ${e.message}")
                    _usuarioAutenticado.postValue(null)
                }
            }
        }
    }

    /**
     * Inicia sesión con credenciales.
     * Equivalente a iniciarSesion(nombreUsuario, contrasena) del diagrama UML.
     */
    fun iniciarSesion(nombreUsuario: String, contrasena: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val usuario = repository.validarCredenciales(nombreUsuario, contrasena)
                    if (usuario != null) {
                        _usuarioAutenticado.postValue(usuario)
                        _mensajeEstado.postValue(null) // Limpiar mensaje de error si existe
                    } else {
                        _mensajeEstado.postValue("Credenciales inválidas")
                        _usuarioAutenticado.postValue(null)
                    }
                } catch (e: Exception) {
                    _mensajeEstado.postValue("Error al iniciar sesión: ${e.message}")
                    _usuarioAutenticado.postValue(null)
                }
            }
        }
    }

    /**
     * Obtiene el usuario actual autenticado.
     * Equivalente a obtenerUsuarioActual(): Usuario del diagrama UML.
     */
    fun obtenerUsuarioActual(): Usuario? {
        return _usuarioAutenticado.value
    }
}

