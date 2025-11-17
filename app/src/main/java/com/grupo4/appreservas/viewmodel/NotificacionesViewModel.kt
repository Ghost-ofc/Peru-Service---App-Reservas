package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para gestionar las notificaciones del usuario.
 * Equivalente a NotificacionesViewModel del diagrama UML.
 */
class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PeruvianServiceRepository = PeruvianServiceRepository.getInstance(application)

    private val _recordatorios = MutableLiveData<List<Notificacion>>()
    val recordatorios: LiveData<List<Notificacion>> = _recordatorios

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Carga los recordatorios/notificaciones del usuario.
     * Equivalente a cargarRecordatoriosUsuario(usuarioId) del diagrama UML.
     */
    fun cargarRecordatoriosUsuario(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val notificaciones = repository.obtenerRecordatorios(usuarioId)
                    _recordatorios.postValue(notificaciones)
                } catch (e: Exception) {
                    _error.postValue("Error al cargar notificaciones: ${e.message}")
                }
            }
        }
    }

    /**
     * Detecta cambios en condiciones (clima, ofertas, etc.).
     * Equivalente a detectarCambio() del diagrama UML.
     */
    fun detectarCambio(usuarioId: Int, actualUbicacion: String? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Detectar cambios de clima
                    val hayCambioClima = repository.obtenerCondicionesYDetectarCambio(actualUbicacion)
                    if (hayCambioClima) {
                        // Recargar notificaciones para incluir nuevas alertas climáticas
                        cargarRecordatoriosUsuario(usuarioId)
                    }
                } catch (e: Exception) {
                    _error.postValue("Error al detectar cambios: ${e.message}")
                }
            }
        }
    }

    /**
     * Genera ofertas de último minuto.
     * Equivalente a generarOferta() del diagrama UML.
     */
    fun generarOferta(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Obtener tours con descuento
                    val toursConDescuento = repository.obtenerToursConDescuento()
                    
                    // Crear notificaciones de ofertas para el usuario
                    for (tour in toursConDescuento) {
                        repository.crearNotificacionOferta(usuarioId, tour.tourId, tour.nombre, 20) // 20% descuento
                    }
                    
                    // Recargar notificaciones
                    cargarRecordatoriosUsuario(usuarioId)
                } catch (e: Exception) {
                    _error.postValue("Error al generar ofertas: ${e.message}")
                }
            }
        }
    }

    /**
     * Marca una notificación como leída.
     */
    fun marcarComoLeida(notificacionId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    repository.marcarNotificacionComoLeida(notificacionId)
                    // Recargar notificaciones
                    val usuarioId = _recordatorios.value?.firstOrNull()?.usuarioId ?: return@withContext
                    cargarRecordatoriosUsuario(usuarioId)
                } catch (e: Exception) {
                    _error.postValue("Error al marcar como leída: ${e.message}")
                }
            }
        }
    }

    /**
     * Marca todas las notificaciones como leídas.
     */
    fun marcarTodasComoLeidas(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    repository.marcarTodasLasNotificacionesComoLeidas(usuarioId)
                    cargarRecordatoriosUsuario(usuarioId)
                } catch (e: Exception) {
                    _error.postValue("Error al marcar todas como leídas: ${e.message}")
                }
            }
        }
    }
}

