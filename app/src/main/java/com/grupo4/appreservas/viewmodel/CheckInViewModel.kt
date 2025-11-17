package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    private val _resultadoEscaneo = MutableLiveData<String>()
    val resultadoEscaneo: LiveData<String> = _resultadoEscaneo

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Procesa el escaneo de un código QR.
     * Equivalente a procesarEscaneoQR(codigoReserva, tourId) del diagrama UML.
     */
    fun procesarEscaneoQR(codigoReserva: String, tourId: String, guiaId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Validar el QR
                    if (!repository.validarQR(codigoReserva)) {
                        _resultadoEscaneo.postValue("QR no válido o ya registrado")
                        return@withContext
                    }

                    // Obtener el ID de la reserva
                    val reservaId = repository.obtenerReservaId(codigoReserva)
                    if (reservaId.isEmpty()) {
                        _resultadoEscaneo.postValue("QR no válido o ya registrado")
                        return@withContext
                    }

                    // Verificar que la reserva pertenece al tour
                    if (!repository.perteneceATour(reservaId, tourId)) {
                        _resultadoEscaneo.postValue("QR no válido o ya registrado")
                        return@withContext
                    }

                    // Verificar si ya fue usado
                    if (repository.estaUsado(codigoReserva)) {
                        _resultadoEscaneo.postValue("QR no válido o ya registrado")
                        return@withContext
                    }

                    // Registrar el check-in
                    val horaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val exito = repository.registrarCheckIn(reservaId, guiaId, horaActual)

                    if (exito) {
                        // Marcar como usado
                        repository.marcarUsado(codigoReserva)
                        _resultadoEscaneo.postValue("Asistencia confirmada")
                    } else {
                        _resultadoEscaneo.postValue("Error al registrar asistencia")
                    }
                } catch (e: Exception) {
                    _error.postValue("Error al procesar QR: ${e.message}")
                    _resultadoEscaneo.postValue("Error al procesar QR")
                }
            }
        }
    }
}

