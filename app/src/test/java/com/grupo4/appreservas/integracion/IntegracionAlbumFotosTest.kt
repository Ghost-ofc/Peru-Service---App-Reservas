package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.Foto
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.AlbumTourViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Pruebas de integración para HU-008: Subida y Visualización de Fotos del Álbum Grupal
 * 
 * Escenarios a probar:
 * 1. Subida de fotos: El turista finaliza su tour, selecciona opción "Subir fotos", 
 *    la app permite elegir imágenes y subirlas al álbum grupal
 * 2. Visualización de álbum: El turista entra al álbum del tour, 
 *    la app carga las fotos compartidas, se muestran todas las imágenes aprobadas del grupo
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionAlbumFotosTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var viewModel: AlbumTourViewModel
    private lateinit var repository: PeruvianServiceRepository

    private val testDispatcher = StandardTestDispatcher()
    private val tourId = "dest_001_2024-11-20"
    private val nombreAutor = "Usuario Test"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper
        mockkConstructor(DatabaseHelper::class)
        every { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(any()) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().insertarFoto(any()) } returns 1L

        // Instanciar repositorio DESPUÉS de configurar todos los mocks
        repository = PeruvianServiceRepository.getInstance(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
        // Resetear la instancia singleton del repositorio para cada test
        val field = PeruvianServiceRepository::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun `test HU-008 Escenario 1 - Subida de fotos al álbum grupal`() = runTest {
        // Arrange: Fotos a subir
        val rutasImagenes = listOf(
            "file:///storage/emulated/0/DCIM/Camera/foto1.jpg",
            "file:///storage/emulated/0/DCIM/Camera/foto2.jpg"
        )

        // Mock: Insertar fotos
        var fotosGuardadas = mutableListOf<Foto>()
        every { anyConstructed<DatabaseHelper>().insertarFoto(any()) } answers {
            val foto = firstArg<Foto>()
            fotosGuardadas.add(foto)
            1L
        }
        every { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(tourId) } answers {
            fotosGuardadas.filter { it.aprobada }
        }

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = AlbumTourViewModel(application)

        // Observador
        val fotosObserver = mockk<Observer<List<Foto>>>(relaxed = true)
        val mensajeObserver = mockk<Observer<String>>(relaxed = true)
        viewModel.fotosAlbum.observeForever(fotosObserver)
        viewModel.mensajeEstado.observeForever(mensajeObserver)

        // Act: Subir fotos
        viewModel.subirFotosSeleccionadas(tourId, rutasImagenes, nombreAutor)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se guardaron las fotos
        verify(atLeast = 2) { anyConstructed<DatabaseHelper>().insertarFoto(any()) }
        
        // Verificar que las fotos se guardaron correctamente
        assertEquals(2, fotosGuardadas.size)
        assertTrue(fotosGuardadas.all { it.idTour == tourId })
        assertTrue(fotosGuardadas.all { it.nombreAutor == nombreAutor })
        assertTrue(fotosGuardadas.all { it.aprobada })
        
        // Verificar que se actualizó el mensaje de estado
        verify { mensajeObserver.onChanged(any()) }
    }

    @Test
    fun `test HU-008 Escenario 2 - Visualización de álbum con fotos aprobadas`() = runTest {
        // Arrange: Fotos existentes en el álbum
        val fotosExistentes = listOf(
            Foto(
                idFoto = "FOTO_1",
                idTour = tourId,
                urlImagen = "https://example.com/foto1.jpg",
                nombreAutor = "Usuario 1",
                fechaSubida = Date(),
                aprobada = true
            ),
            Foto(
                idFoto = "FOTO_2",
                idTour = tourId,
                urlImagen = "https://example.com/foto2.jpg",
                nombreAutor = "Usuario 2",
                fechaSubida = Date(),
                aprobada = true
            ),
            Foto(
                idFoto = "FOTO_3",
                idTour = tourId,
                urlImagen = "https://example.com/foto3.jpg",
                nombreAutor = "Usuario 3",
                fechaSubida = Date(),
                aprobada = false // No aprobada, no debe mostrarse
            )
        )

        // Mock: Obtener solo fotos aprobadas
        every { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(tourId) } returns 
            fotosExistentes.filter { it.aprobada }

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = AlbumTourViewModel(application)

        // Observador
        val fotosObserver = mockk<Observer<List<Foto>>>(relaxed = true)
        viewModel.fotosAlbum.observeForever(fotosObserver)

        // Act: Cargar fotos del álbum
        viewModel.cargarFotosAlbum(tourId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtuvieron solo las fotos aprobadas
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(tourId) }
        
        // Verificar que se actualizó el LiveData
        verify { fotosObserver.onChanged(any()) }
        
        // Verificar que solo se muestran fotos aprobadas
        val fotosObtenidas = repository.obtenerFotosPorTour(tourId)
        assertEquals(2, fotosObtenidas.size)
        assertTrue(fotosObtenidas.all { it.aprobada })
        assertTrue(fotosObtenidas.all { it.idTour == tourId })
    }

    @Test
    fun `test cargar fotos de álbum vacío muestra lista vacía`() = runTest {
        // Arrange: Álbum sin fotos
        every { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(tourId) } returns emptyList()

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = AlbumTourViewModel(application)

        // Observador
        val fotosObserver = mockk<Observer<List<Foto>>>(relaxed = true)
        viewModel.fotosAlbum.observeForever(fotosObserver)

        // Act: Cargar fotos del álbum
        viewModel.cargarFotosAlbum(tourId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtiene lista vacía
        val fotosObtenidas = repository.obtenerFotosPorTour(tourId)
        assertTrue(fotosObtenidas.isEmpty())
        
        // Verificar que se actualizó el LiveData con lista vacía
        verify { fotosObserver.onChanged(any()) }
    }

    @Test
    fun `test guardar múltiples fotos para un tour`() = runTest {
        // Arrange: Múltiples fotos a subir
        val rutasImagenes = (1..5).map { 
            "file:///storage/emulated/0/DCIM/Camera/foto$it.jpg" 
        }

        // Mock: Insertar fotos
        var fotosGuardadas = mutableListOf<Foto>()
        every { anyConstructed<DatabaseHelper>().insertarFoto(any()) } answers {
            val foto = firstArg<Foto>()
            fotosGuardadas.add(foto)
            1L
        }
        every { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(tourId) } answers {
            fotosGuardadas.filter { it.aprobada }
        }

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = AlbumTourViewModel(application)

        // Act: Subir múltiples fotos
        viewModel.subirFotosSeleccionadas(tourId, rutasImagenes, nombreAutor)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se guardaron todas las fotos
        assertEquals(5, fotosGuardadas.size)
        assertTrue(fotosGuardadas.all { it.idTour == tourId })
        assertTrue(fotosGuardadas.all { it.nombreAutor == nombreAutor })
        
        // Verificar que todas tienen IDs únicos
        val ids = fotosGuardadas.map { it.idFoto }.toSet()
        assertEquals(5, ids.size)
    }

    @Test
    fun `test fotos de diferentes tours no se mezclan`() = runTest {
        // Arrange: Fotos de diferentes tours
        val tourId1 = "dest_001_2024-11-20"
        val tourId2 = "dest_002_2024-11-21"
        
        val fotosTour1 = listOf(
            Foto(
                idFoto = "FOTO_1",
                idTour = tourId1,
                urlImagen = "https://example.com/foto1.jpg",
                nombreAutor = "Usuario 1",
                fechaSubida = Date(),
                aprobada = true
            )
        )
        
        val fotosTour2 = listOf(
            Foto(
                idFoto = "FOTO_2",
                idTour = tourId2,
                urlImagen = "https://example.com/foto2.jpg",
                nombreAutor = "Usuario 2",
                fechaSubida = Date(),
                aprobada = true
            )
        )

        // Mock: Obtener fotos según el tour
        every { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(tourId1) } returns fotosTour1
        every { anyConstructed<DatabaseHelper>().obtenerFotosPorTour(tourId2) } returns fotosTour2

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = AlbumTourViewModel(application)

        // Act: Cargar fotos de cada tour
        viewModel.cargarFotosAlbum(tourId1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val fotosTour1Obtenidas = repository.obtenerFotosPorTour(tourId1)
        
        viewModel.cargarFotosAlbum(tourId2)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val fotosTour2Obtenidas = repository.obtenerFotosPorTour(tourId2)

        // Assert: Verificar que cada tour tiene sus propias fotos
        assertEquals(1, fotosTour1Obtenidas.size)
        assertEquals(1, fotosTour2Obtenidas.size)
        assertTrue(fotosTour1Obtenidas.all { it.idTour == tourId1 })
        assertTrue(fotosTour2Obtenidas.all { it.idTour == tourId2 })
    }
}

