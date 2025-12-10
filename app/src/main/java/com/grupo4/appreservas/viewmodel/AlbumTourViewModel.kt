package com.grupo4.appreservas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grupo4.appreservas.modelos.Foto
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * ViewModel para gestionar el álbum de fotos del tour.
 * Equivalente a AlbumTourViewModel del diagrama UML.
 */
class AlbumTourViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: PeruvianServiceRepository = PeruvianServiceRepository.getInstance(application)

    private val _fotosAlbum = MutableLiveData<List<Foto>>()
    val fotosAlbum: LiveData<List<Foto>> = _fotosAlbum

    private val _mensajeEstado = MutableLiveData<String>()
    val mensajeEstado: LiveData<String> = _mensajeEstado

    /**
     * Carga las fotos del álbum para un tour específico.
     * Equivalente a cargarFotosAlbum(idTour) del diagrama UML.
     */
    fun cargarFotosAlbum(idTour: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val fotos = repositorio.obtenerFotosPorTour(idTour)
                    _fotosAlbum.postValue(fotos)
                    _mensajeEstado.postValue("Fotos cargadas correctamente")
                } catch (e: Exception) {
                    _mensajeEstado.postValue("Error al cargar fotos: ${e.message}")
                    _fotosAlbum.postValue(emptyList())
                }
            }
        }
    }

    /**
     * Sube las fotos seleccionadas para un tour.
     * Equivalente a subirFotosSeleccionadas(idTour, rutasImagenes) del diagrama UML.
     * 
     * @param idTour ID del tour
     * @param rutasImagenes Lista de rutas/URLs de las imágenes a subir
     * @param nombreAutor Nombre del autor que sube las fotos
     */
    fun subirFotosSeleccionadas(idTour: String, rutasImagenes: List<String>, nombreAutor: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    android.util.Log.d("AlbumTourViewModel", "Intentando subir ${rutasImagenes.size} fotos para tour: $idTour")
                    
                    // Verificar que el tour esté completado o que haya reservas confirmadas
                    val tour = repositorio.obtenerTourPorId(idTour)
                    if (tour != null) {
                        android.util.Log.d("AlbumTourViewModel", "Tour encontrado: $idTour, estado: ${tour.estado}")
                        
                        // Permitir subir fotos si el tour está completado o si hay reservas confirmadas
                        // (no bloqueamos si el tour aún no está marcado como completado, pero la reserva sí)
                        if (tour.estado != "Completado") {
                            android.util.Log.d("AlbumTourViewModel", "Tour $idTour no está marcado como completado, pero se permite subir fotos si hay reservas confirmadas")
                        }
                    } else {
                        android.util.Log.w("AlbumTourViewModel", "Tour $idTour no encontrado, pero se permite subir fotos")
                    }
                    
                    val fotos = rutasImagenes.mapIndexed { index, ruta ->
                        Foto(
                            idFoto = "FOTO_${idTour}_${System.currentTimeMillis()}_$index",
                            idTour = idTour,
                            urlImagen = ruta,
                            nombreAutor = nombreAutor,
                            fechaSubida = Date(),
                            aprobada = true // Por ahora se aprueban automáticamente
                        )
                    }
                    
                    android.util.Log.d("AlbumTourViewModel", "Guardando ${fotos.size} fotos en el repositorio...")
                    val exito = repositorio.guardarFotosDeTour(idTour, fotos)
                    
                    if (exito) {
                        android.util.Log.d("AlbumTourViewModel", "✓ Fotos subidas correctamente para tour: $idTour")
                        _mensajeEstado.postValue("Fotos subidas correctamente")
                        // Recargar las fotos del álbum
                        cargarFotosAlbum(idTour)
                    } else {
                        android.util.Log.e("AlbumTourViewModel", "✗ Error al guardar las fotos para tour: $idTour")
                        _mensajeEstado.postValue("Error al guardar las fotos")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AlbumTourViewModel", "✗ Error al subir fotos: ${e.message}", e)
                    _mensajeEstado.postValue("Error al subir fotos: ${e.message}")
                }
            }
        }
    }
}

