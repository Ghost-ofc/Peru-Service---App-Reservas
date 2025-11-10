package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.Logro
import com.grupo4.appreservas.modelos.PuntosUsuario
import com.grupo4.appreservas.modelos.TipoLogro
import com.grupo4.appreservas.modelos.CriterioLogro
import com.grupo4.appreservas.modelos.TipoCriterio
import com.grupo4.appreservas.repository.RepositorioRecompensas
import com.grupo4.appreservas.repository.ReservasRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel para recompensas y logros según HU-007 y diagrama UML.
 * Equivalente a RecompensasViewModel del diagrama.
 */
class RecompensasViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorioRecompensas = RepositorioRecompensas(application)
    private val repositorioReservas = ReservasRepository.getInstance(application)

    // LiveData para puntos (según diagrama UML)
    private val _puntos = MutableLiveData<Int>()
    val puntos: LiveData<Int> = _puntos

    // LiveData para logros (según diagrama UML)
    private val _logros = MutableLiveData<List<Logro>>()
    val logros: LiveData<List<Logro>> = _logros

    // LiveData para información completa de puntos (nivel, etc.)
    private val _puntosUsuario = MutableLiveData<PuntosUsuario>()
    val puntosUsuario: LiveData<PuntosUsuario> = _puntosUsuario

    // LiveData para número de tours completados
    private val _toursCompletados = MutableLiveData<Int>()
    val toursCompletados: LiveData<Int> = _toursCompletados

    /**
     * Actualiza los puntos de un usuario después de completar una reserva.
     * Equivalente a actualizarPuntos(usuarioId, reservaId) del diagrama UML.
     * 
     * Nota: Los puntos ya fueron sumados en PagoActivity, este método solo actualiza
     * los LiveData y verifica logros.
     */
    fun actualizarPuntos(usuarioId: Int, reservaId: String) {
        viewModelScope.launch {
            try {
                // Verificar que la reserva está confirmada
                val reserva = repositorioReservas.find(reservaId)
                if (reserva != null && reserva.estaConfirmado()) {
                    // Actualizar LiveData (los puntos ya fueron sumados en PagoActivity)
                    val nuevosPuntos = repositorioRecompensas.obtenerPuntos(usuarioId)
                    _puntos.postValue(nuevosPuntos)
                    
                    // Actualizar información completa de puntos
                    val puntosUsuarioInfo = repositorioRecompensas.obtenerPuntosUsuario(usuarioId)
                    _puntosUsuario.postValue(puntosUsuarioInfo)
                    
                    // Actualizar número de tours completados
                    val numToursCompletados = repositorioRecompensas.obtenerNumeroReservasConfirmadas(usuarioId)
                    _toursCompletados.postValue(numToursCompletados)
                    
                    // Verificar y desbloquear logros
                    verificarYDesbloquearLogros(usuarioId)
                }
            } catch (e: Exception) {
                android.util.Log.e("RecompensasViewModel", "Error al actualizar puntos: ${e.message}", e)
            }
        }
    }

    /**
     * Carga los logros y puntos de un usuario.
     * Equivalente a cargarLogros(usuarioId) del diagrama UML.
     */
    fun cargarLogros(usuarioId: Int) {
        viewModelScope.launch {
            try {
                // Cargar puntos
                val puntos = repositorioRecompensas.obtenerPuntos(usuarioId)
                _puntos.postValue(puntos)
                
                // Cargar información completa de puntos
                val puntosUsuarioInfo = repositorioRecompensas.obtenerPuntosUsuario(usuarioId)
                _puntosUsuario.postValue(puntosUsuarioInfo)
                
                // Cargar número de tours completados
                val numToursCompletados = repositorioRecompensas.obtenerNumeroReservasConfirmadas(usuarioId)
                _toursCompletados.postValue(numToursCompletados)
                
                // Generar y cargar logros
                generarLogrosSiNoExisten(usuarioId)
                val logrosUsuario = repositorioRecompensas.obtenerLogros(usuarioId)
                _logros.postValue(logrosUsuario)
                
                // Verificar y desbloquear logros
                verificarYDesbloquearLogros(usuarioId)
            } catch (e: Exception) {
                android.util.Log.e("RecompensasViewModel", "Error al cargar logros: ${e.message}", e)
            }
        }
    }

    /**
     * Genera los logros predeterminados para un usuario si no existen.
     */
    private fun generarLogrosSiNoExisten(usuarioId: Int) {
        val logrosPredeterminados = listOf(
            Logro(
                id = "LOGRO_PRIMER_VIAJE_$usuarioId",
                nombre = "Primer Viaje",
                descripcion = "Completaste tu primera reserva",
                icono = "ic_achievement_first_trip",
                puntosRequeridos = 0,
                tipo = TipoLogro.PRIMER_VIAJE,
                criterio = CriterioLogro(TipoCriterio.PRIMERA_RESERVA, 1),
                fechaDesbloqueo = null,
                desbloqueado = false
            ),
            Logro(
                id = "LOGRO_5_TOURS_$usuarioId",
                nombre = "5 Tours Completados",
                descripcion = "Completaste 5 tours",
                icono = "ic_achievement_5_tours",
                puntosRequeridos = 0,
                tipo = TipoLogro.TOURS_COMPLETADOS,
                criterio = CriterioLogro(TipoCriterio.TOURS_COMPLETADOS, 5),
                fechaDesbloqueo = null,
                desbloqueado = false
            ),
            Logro(
                id = "LOGRO_EXPLORADOR_SEMANA_$usuarioId",
                nombre = "Explorador de la Semana",
                descripcion = "Realizaste 3 tours en una semana",
                icono = "ic_achievement_week_explorer",
                puntosRequeridos = 0,
                tipo = TipoLogro.EXPLORADOR_SEMANA,
                criterio = CriterioLogro(TipoCriterio.TOURS_EN_SEMANA, 3),
                fechaDesbloqueo = null,
                desbloqueado = false
            ),
            Logro(
                id = "LOGRO_PUNTOS_1000_$usuarioId",
                nombre = "1000 Puntos",
                descripcion = "Acumulaste 1000 puntos",
                icono = "ic_achievement_1000_points",
                puntosRequeridos = 1000,
                tipo = TipoLogro.PUNTOS_ACUMULADOS,
                criterio = CriterioLogro(TipoCriterio.PUNTOS_ACUMULADOS, 1000),
                fechaDesbloqueo = null,
                desbloqueado = false
            )
        )

        logrosPredeterminados.forEach { logro ->
            if (!repositorioRecompensas.existeLogro(usuarioId, logro.id)) {
                repositorioRecompensas.insertarLogro(usuarioId, logro)
            }
        }
    }

    /**
     * Verifica si se deben desbloquear logros y los desbloquea.
     */
    private fun verificarYDesbloquearLogros(usuarioId: Int) {
        viewModelScope.launch {
            try {
                val puntos = repositorioRecompensas.obtenerPuntos(usuarioId)
                val numToursCompletados = repositorioRecompensas.obtenerNumeroReservasConfirmadas(usuarioId)
                val logros = repositorioRecompensas.obtenerLogros(usuarioId)

                logros.forEach { logro ->
                    if (!logro.desbloqueado) {
                        var debeDesbloquear = false

                        when (logro.criterio.tipo) {
                            TipoCriterio.PRIMERA_RESERVA -> {
                                debeDesbloquear = numToursCompletados >= 1
                            }
                            TipoCriterio.TOURS_COMPLETADOS -> {
                                debeDesbloquear = numToursCompletados >= logro.criterio.valor
                            }
                            TipoCriterio.PUNTOS_ACUMULADOS -> {
                                debeDesbloquear = puntos >= logro.criterio.valor
                            }
                            TipoCriterio.TOURS_EN_SEMANA -> {
                                // Verificar tours en la última semana
                                val reservasConfirmadas = repositorioReservas.obtenerReservasConfirmadas(usuarioId)
                                val fechaHaceUnaSemana = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)
                                val toursEnSemana = reservasConfirmadas.count { reserva ->
                                    reserva.fecha.after(fechaHaceUnaSemana)
                                }
                                debeDesbloquear = toursEnSemana >= logro.criterio.valor
                            }
                            TipoCriterio.TOURS_EN_MES -> {
                                // Verificar tours en el último mes
                                val reservasConfirmadas = repositorioReservas.obtenerReservasConfirmadas(usuarioId)
                                val fechaHaceUnMes = Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L)
                                val toursEnMes = reservasConfirmadas.count { reserva ->
                                    reserva.fecha.after(fechaHaceUnMes)
                                }
                                debeDesbloquear = toursEnMes >= logro.criterio.valor
                            }
                        }

                        if (debeDesbloquear) {
                            val logroDesbloqueado = logro.copy(
                                desbloqueado = true,
                                fechaDesbloqueo = Date()
                            )
                            repositorioRecompensas.insertarLogro(usuarioId, logroDesbloqueado)
                            
                            // Actualizar lista de logros
                            val logrosActualizados = repositorioRecompensas.obtenerLogros(usuarioId)
                            _logros.postValue(logrosActualizados)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("RecompensasViewModel", "Error al verificar logros: ${e.message}", e)
            }
        }
    }
}

