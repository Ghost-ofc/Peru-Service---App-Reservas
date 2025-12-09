package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para gestionar los tours del día del guía.
 * Equivalente a TourDelDiaViewModel del diagrama UML.
 */
class TourDelDiaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PeruvianServiceRepository = PeruvianServiceRepository.getInstance(application)

    private val _toursDelDia = MutableLiveData<List<Tour>>()
    val toursDelDia: LiveData<List<Tour>> = _toursDelDia

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Carga los tours del día para un guía.
     * Equivalente a cargarToursDelDia(guiaId) del diagrama UML.
     */
    fun cargarToursDelDia(guiaId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val tours = repository.obtenerToursDelDia(guiaId)
                    _toursDelDia.postValue(tours)
                } catch (e: Exception) {
                    _error.postValue("Error al cargar tours: ${e.message}")
                }
            }
        }
    }

    /**
     * Abre un tour específico.
     * Equivalente a abrirTour(tourId) del diagrama UML.
     */
    fun abrirTour(tourId: String): Tour? {
        return _toursDelDia.value?.find { it.tourId == tourId }
    }
}

