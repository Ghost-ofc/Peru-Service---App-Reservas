package com.grupo4.appreservas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.PeruvianServiceRepository

/**
 * ViewModel para la gestión de reservas.
 * Corresponde al ReservaViewModel del diagrama UML.
 */
class ReservaViewModel(
    private val repository: PeruvianServiceRepository
) : ViewModel() {

    // LiveData para cupos disponibles
    private val _cuposDisponibles = MutableLiveData<Int>()
    val cuposDisponibles: LiveData<Int> = _cuposDisponibles

    // LiveData para disponibilidad
    private val _disponibilidad = MutableLiveData<Boolean>()
    val disponibilidad: LiveData<Boolean> = _disponibilidad

    /**
     * Inicia el proceso de reserva para un tour.
     * Equivalente a iniciarReserva(idTour) del diagrama UML.
     */
    fun iniciarReserva(idTour: String) {
        // Se puede usar para inicializar datos cuando se abre la pantalla de reserva
    }

    /**
     * Consulta la disponibilidad de asientos para un tour slot.
     * Equivalente a consultarDisponibilidadAsientos(idTourSlot) del diagrama UML.
     */
    fun consultarDisponibilidadAsientos(idTourSlot: String) {
        val cupos = repository.consultarCuposDisponibles(idTourSlot)
        _cuposDisponibles.value = cupos
        _disponibilidad.value = cupos > 0
    }

    /**
     * Crea una reserva.
     * Equivalente a crearReserva(comandoReserva) del diagrama UML.
     */
    fun crearReserva(idUsuario: Int, idTourSlot: String, cantidadPasajeros: Int): Reserva? {
        // Primero bloquear los asientos
        val asientosBloqueados = repository.bloquearAsientos(idTourSlot, cantidadPasajeros)
        
        if (!asientosBloqueados) {
            return null // No hay suficientes cupos disponibles
        }

        // Crear la reserva
        return repository.crearReserva(idUsuario, idTourSlot, cantidadPasajeros)
    }
}

