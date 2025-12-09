package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.EncuestaRespuesta
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gestionar las encuestas de satisfacción.
 * Equivalente a EncuestaViewModel del diagrama UML.
 */
class EncuestaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: PeruvianServiceRepository = PeruvianServiceRepository.getInstance(application)

    private val _mensajeEstado = MutableLiveData<String>()
    val mensajeEstado: LiveData<String> = _mensajeEstado

    private val _respuestaEncuesta = MutableLiveData<EncuestaRespuesta?>()
    val respuestaEncuesta: LiveData<EncuestaRespuesta?> = _respuestaEncuesta

    private val _tour = MutableLiveData<Tour?>()
    val tour: LiveData<Tour?> = _tour

    private val _encuestaEnviada = MutableLiveData<Boolean>()
    val encuestaEnviada: LiveData<Boolean> = _encuestaEnviada

    /**
     * Carga la información de la encuesta para un tour.
     * Equivalente a cargarEncuesta(idTour) del diagrama UML.
     */
    fun cargarEncuesta(idTour: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val tour = repositorio.obtenerTourPorId(idTour)
                    _tour.postValue(tour)
                    _mensajeEstado.postValue("Encuesta cargada correctamente")
                } catch (e: Exception) {
                    _mensajeEstado.postValue("Error al cargar encuesta: ${e.message}")
                    _tour.postValue(null)
                }
            }
        }
    }

    /**
     * Registra la respuesta de una encuesta.
     * Equivalente a registrarRespuesta(idTour, calificacion, comentario) del diagrama UML.
     */
    fun registrarRespuesta(idTour: String, usuarioId: Int, calificacion: Int, comentario: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Validar calificación
                    if (calificacion !in 1..5) {
                        _mensajeEstado.postValue("La calificación debe estar entre 1 y 5 estrellas")
                        _encuestaEnviada.postValue(false)
                        _respuestaEncuesta.postValue(null)
                        return@withContext
                    }

                    // Verificar si ya respondió
                    if (repositorio.yaRespondioEncuesta(idTour, usuarioId)) {
                        _mensajeEstado.postValue("Ya has respondido esta encuesta")
                        _encuestaEnviada.postValue(false)
                        _respuestaEncuesta.postValue(null)
                        return@withContext
                    }

                    // Guardar respuesta
                    val respuesta = repositorio.guardarRespuestaEncuesta(idTour, usuarioId, calificacion, comentario)
                    
                    if (respuesta != null) {
                        _respuestaEncuesta.postValue(respuesta)
                        _mensajeEstado.postValue("¡Gracias por tu opinión! Has ganado ${respuesta.puntosOtorgados} puntos")
                        _encuestaEnviada.postValue(true)
                    } else {
                        _mensajeEstado.postValue("Error al guardar la respuesta")
                        _encuestaEnviada.postValue(false)
                        _respuestaEncuesta.postValue(null)
                    }
                } catch (e: Exception) {
                    _mensajeEstado.postValue("Error al registrar respuesta: ${e.message}")
                    _encuestaEnviada.postValue(false)
                    _respuestaEncuesta.postValue(null)
                }
            }
        }
    }
}

