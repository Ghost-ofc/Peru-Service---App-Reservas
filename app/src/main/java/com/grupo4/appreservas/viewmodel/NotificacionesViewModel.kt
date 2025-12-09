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

    private val _notificaciones = MutableLiveData<List<Notificacion>>()
    val notificaciones: LiveData<List<Notificacion>> = _notificaciones

    private val _recordatorios = MutableLiveData<List<Notificacion>>()
    val recordatorios: LiveData<List<Notificacion>> = _recordatorios

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Carga los recordatorios del usuario.
     * Equivalente a cargarRecordatoriosUsuario(usuarioId) del diagrama UML.
     */
    fun cargarRecordatoriosUsuario(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val recordatorios = repository.obtenerRecordatorios(usuarioId)
                    _recordatorios.postValue(recordatorios)
                    // Actualizar lista completa de notificaciones
                    actualizarListaCompleta(usuarioId)
                } catch (e: Exception) {
                    _error.postValue("Error al cargar recordatorios: ${e.message}")
                }
            }
        }
    }

    /**
     * Carga las alertas climáticas del usuario.
     * Equivalente a cargarAlertasClimaticas(usuarioId) del diagrama UML.
     */
    fun cargarAlertasClimaticas(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val alertas = repository.obtenerAlertasClimaticas(usuarioId)
                    // Actualizar lista completa de notificaciones
                    actualizarListaCompleta(usuarioId)
                } catch (e: Exception) {
                    _error.postValue("Error al cargar alertas climáticas: ${e.message}")
                }
            }
        }
    }

    /**
     * Carga las ofertas de último minuto del usuario.
     * Equivalente a cargarOfertasUltimoMinuto(usuarioId) del diagrama UML.
     */
    fun cargarOfertasUltimoMinuto(usuarioId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val ofertas = repository.obtenerOfertasUltimoMinuto(usuarioId)
                    // Actualizar lista completa de notificaciones
                    actualizarListaCompleta(usuarioId)
                } catch (e: Exception) {
                    _error.postValue("Error al cargar ofertas: ${e.message}")
                }
            }
        }
    }

    /**
     * Actualiza la lista completa de notificaciones combinando todos los tipos.
     */
    private fun actualizarListaCompleta(usuarioId: Int) {
        val todasLasNotificaciones = mutableListOf<Notificacion>()
        todasLasNotificaciones.addAll(repository.obtenerRecordatorios(usuarioId))
        todasLasNotificaciones.addAll(repository.obtenerAlertasClimaticas(usuarioId))
        todasLasNotificaciones.addAll(repository.obtenerOfertasUltimoMinuto(usuarioId))
        // Agregar otras notificaciones (encuestas, etc.)
        val otrasNotificaciones = repository.obtenerNotificacionesNoLeidasPorUsuario(usuarioId)
            .filter { it.tipo != com.grupo4.appreservas.modelos.TipoNotificacion.RECORDATORIO &&
                      it.tipo != com.grupo4.appreservas.modelos.TipoNotificacion.ALERTA_CLIMATICA &&
                      it.tipo != com.grupo4.appreservas.modelos.TipoNotificacion.CLIMA_FAVORABLE &&
                      it.tipo != com.grupo4.appreservas.modelos.TipoNotificacion.OFERTA_ULTIMO_MINUTO }
        todasLasNotificaciones.addAll(otrasNotificaciones)
        // Ordenar por fecha de creación (más recientes primero)
        _notificaciones.postValue(todasLasNotificaciones.sortedByDescending { it.fechaCreacion })
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

