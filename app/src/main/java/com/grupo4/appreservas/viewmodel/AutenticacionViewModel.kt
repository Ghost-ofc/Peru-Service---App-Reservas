package com.grupo4.appreservas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.PeruvianServiceRepository

/**
 * ViewModel para la autenticación de usuarios.
 * Corresponde al AutenticacionViewModel del diagrama UML.
 */
class AutenticacionViewModel(
    private val repository: PeruvianServiceRepository
) : ViewModel() {

    // LiveData para el usuario autenticado
    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> = _usuario

    /**
     * Registra un nuevo usuario.
     * Equivalente a registrar(nombre, correo, contrasena, rol) del diagrama UML.
     */
    fun registrar(nombre: String, correo: String, contrasena: String, rol: Int) {
        val usuario = repository.crearUsuario(nombre, correo, contrasena, rol)
        _usuario.value = usuario
    }

    /**
     * Inicia sesión con credenciales.
     * Equivalente a iniciarSesion(correo, contrasena, rol) del diagrama UML.
     */
    fun iniciarSesion(correo: String, contrasena: String) {
        val usuario = repository.validarCredenciales(correo, contrasena)
        _usuario.value = usuario
    }
}

