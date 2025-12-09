package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.CheckIn
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gestionar el check-in mediante escaneo QR.
 * Equivalente a CheckInViewModel del diagrama UML.
 */
class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PeruvianServiceRepository = PeruvianServiceRepository.getInstance(application)

    private val _resultadoCheckin = MutableLiveData<CheckIn?>()
    val resultadoCheckin: LiveData<CheckIn?> = _resultadoCheckin

    private val _mensajeEstado = MutableLiveData<String?>()
    val mensajeEstado: LiveData<String?> = _mensajeEstado

    /**
     * Procesa el escaneo de un código QR.
     * Equivalente a procesarEscaneoQR(codigoQR, idTour, idGuia) del diagrama UML.
     */
    fun procesarEscaneoQR(codigoQR: String, idTour: String, idGuia: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Validar el código QR y obtener la reserva
                    val reserva = repository.validarCodigoQR(codigoQR, idTour)
                    
                    if (reserva == null) {
                        _mensajeEstado.postValue("QR no válido o ya registrado")
                        _resultadoCheckin.postValue(null)
                        return@withContext
                    }

                    // Registrar el check-in
                    val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val checkIn = repository.registrarCheckIn(reserva.id, idGuia, fechaHora)

                    if (checkIn != null) {
                        _resultadoCheckin.postValue(checkIn)
                        _mensajeEstado.postValue(null) // Limpiar mensaje de error si existe
                    } else {
                        _mensajeEstado.postValue("Error al registrar asistencia")
                        _resultadoCheckin.postValue(null)
                    }
                } catch (e: Exception) {
                    _mensajeEstado.postValue("Error al procesar QR: ${e.message}")
                    _resultadoCheckin.postValue(null)
                }
            }
        }
    }
}

