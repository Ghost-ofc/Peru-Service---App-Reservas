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
                    
                    val exito = repositorio.guardarFotosDeTour(idTour, fotos)
                    
                    if (exito) {
                        _mensajeEstado.postValue("Fotos subidas correctamente")
                        // Recargar las fotos del álbum
                        cargarFotosAlbum(idTour)
                    } else {
                        _mensajeEstado.postValue("Error al guardar las fotos")
                    }
                } catch (e: Exception) {
                    _mensajeEstado.postValue("Error al subir fotos: ${e.message}")
                }
            }
        }
    }
}

