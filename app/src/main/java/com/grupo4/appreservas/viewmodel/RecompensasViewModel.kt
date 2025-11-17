package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.Logro
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para gestionar las recompensas (puntos y logros) del usuario.
 * Equivalente a RecompensasViewModel del diagrama UML.
 */
class RecompensasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PeruvianServiceRepository = PeruvianServiceRepository.getInstance(application)

    private val _puntos = MutableLiveData<Int>()
    val puntos: LiveData<Int> = _puntos

    private val _logros = MutableLiveData<List<Logro>>()
    val logros: LiveData<List<Logro>> = _logros

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Actualiza los puntos del usuario cuando completa una reserva.
     * Equivalente a actualizarPuntos(usuarioId, reservaId) del diagrama UML.
     */
    fun actualizarPuntos(usuarioId: Int, reservaId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Sumar puntos por reserva completada
                    repository.sumarPuntosPorReserva(usuarioId, reservaId)
                    
                    // Verificar y desbloquear logros
                    verificarYDesbloquearLogros(usuarioId)
                    
                    // Recargar puntos actualizados
                    val puntosActuales = repository.obtenerPuntos(usuarioId)
                    _puntos.postValue(puntosActuales)
                    
                    // Recargar logros
                    cargarLogros(usuarioId)
                } catch (e: Exception) {
                    _error.postValue("Error al actualizar puntos: ${e.message}")
                }
            }
        }
    }

    /**
     * Carga los logros del usuario.
     * Equivalente a cargarLogros(usuarioId) del diagrama UML.
     */
    fun cargarLogros(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val logros = repository.obtenerLogros(usuarioId)
                    _logros.postValue(logros)
                } catch (e: Exception) {
                    _error.postValue("Error al cargar logros: ${e.message}")
                }
            }
        }
    }

    /**
     * Carga los puntos del usuario.
     */
    fun cargarPuntos(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val puntos = repository.obtenerPuntos(usuarioId)
                    _puntos.postValue(puntos)
                } catch (e: Exception) {
                    _error.postValue("Error al cargar puntos: ${e.message}")
                }
            }
        }
    }

    /**
     * Verifica y desbloquea logros según los criterios del usuario.
     */
    private fun verificarYDesbloquearLogros(usuarioId: Int) {
        val reservasConfirmadas = repository.obtenerReservasConfirmadas(usuarioId)
        val puntosActuales = repository.obtenerPuntos(usuarioId)
        
        // Verificar logro "Primer Viaje"
        if (reservasConfirmadas.size == 1) {
            repository.desbloquearLogro(usuarioId, "PRIMER_VIAJE")
        }
        
        // Verificar logro "Viajero Frecuente" (5+ reservas)
        if (reservasConfirmadas.size >= 5) {
            repository.desbloquearLogro(usuarioId, "VIAJERO_FRECUENTE")
        }
        
        // Verificar logro por tours completados (10+ reservas)
        if (reservasConfirmadas.size >= 10) {
            repository.desbloquearLogro(usuarioId, "TOURS_10")
        }
        
        // Verificar logros por puntos acumulados
        when {
            puntosActuales >= 1000 -> repository.desbloquearLogro(usuarioId, "PUNTOS_1000")
            puntosActuales >= 500 -> repository.desbloquearLogro(usuarioId, "PUNTOS_500")
        }
    }
}

