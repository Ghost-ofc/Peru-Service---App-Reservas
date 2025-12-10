package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.controller.CatalogoController
import com.grupo4.appreservas.controller.ControlDetalleDestino
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.service.DestinoService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas de integración para HU-001: Visualización del catálogo de destinos turísticos
 * 
 * Escenarios a probar:
 * 1. Visualización del catálogo: El turista accede a la opción "Destinos" desde el menú principal,
 *    la app carga la lista de destinos disponibles, se muestran los destinos con su foto, nombre, 
 *    precio y breve descripción
 * 2. Detalle del destino: El turista selecciona un destino del listado, se abre la vista de detalle 
 *    del destino, se muestran información ampliada, duración, itinerario y fotos del lugar
 */
@OptIn(ExperimentalCoroutinesApi::class)
class   IntegracionCatalogoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var repository: PeruvianServiceRepository
    private lateinit var catalogoController: CatalogoController
    private lateinit var controlDetalleDestino: ControlDetalleDestino
    private lateinit var destinoService: DestinoService

    private val testDispatcher = StandardTestDispatcher()
    private val destinoId = "dest_001"

    private val destinosMock = listOf(
        Destino(
            id = "dest_001",
            nombre = "Tour Machu Picchu Clásico",
            ubicacion = "Cusco, Perú",
            descripcion = "Descubre la majestuosa ciudadela inca de Machu Picchu",
            precio = 450.0,
            duracionHoras = 12,
            maxPersonas = 15,
            categorias = listOf("Cultura", "Arqueología", "Naturaleza"),
            imagenUrl = "https://example.com/machu.jpg",
            calificacion = 4.8,
            numReseñas = 124,
            disponibleTodosDias = true,
            incluye = listOf("Transporte", "Guía", "Almuerzo")
        ),
        Destino(
            id = "dest_002",
            nombre = "Líneas de Nazca Tour Aéreo",
            ubicacion = "Ica, Perú",
            descripcion = "Sobrevuela las misteriosas líneas de Nazca",
            precio = 380.0,
            duracionHoras = 6,
            maxPersonas = 8,
            categorias = listOf("Aventura", "Arqueología", "Aéreo"),
            imagenUrl = "https://example.com/nazca.jpg",
            calificacion = 4.6,
            numReseñas = 87,
            disponibleTodosDias = false,
            incluye = listOf("Vuelo", "Guía")
        )
    )

    @Before
    fun setUp() {
        // Mock del Looper principal para evitar "Method getMainLooper not mocked"
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)

        // Forzar a ArchTaskExecutor a ejecutar todo en el mismo hilo (sin Looper)
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) { runnable.run() }
            override fun postToMainThread(runnable: Runnable) { runnable.run() }
            override fun isMainThread(): Boolean = true
        })

        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper
        mockkConstructor(DatabaseHelper::class)
        every { anyConstructed<DatabaseHelper>().obtenerTodosLosDestinos() } returns destinosMock
        every { anyConstructed<DatabaseHelper>().obtenerDestinoPorId(destinoId) } returns destinosMock.first()

        repository = PeruvianServiceRepository.getInstance(context)
        destinoService = DestinoService(repository)
        catalogoController = CatalogoController(destinoService)
        controlDetalleDestino = ControlDetalleDestino(destinoService)
    }

    @After
    fun tearDown() {
        // Restaurar ejecutor y mocks
        ArchTaskExecutor.getInstance().setDelegate(null)
        unmockkStatic(Looper::class)
        Dispatchers.resetMain()
        clearAllMocks()
        val field = PeruvianServiceRepository::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun `test HU-001 Escenario 1 - Visualización del catálogo con destinos disponibles`() = runTest(testDispatcher) {
        // Arrange: Ya configurado en setUp con destinosMock

        // Act: Solicitar lista de destinos
        val destinosObtenidos = catalogoController.solicitarDestinos()
        advanceUntilIdle()

        // Assert: Verificar que se obtuvieron los destinos correctamente
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerTodosLosDestinos() }
        
        assertEquals(2, destinosObtenidos.size)
        
        // Verificar primer destino
        val destino1 = destinosObtenidos[0]
        assertEquals("dest_001", destino1.id)
        assertEquals("Tour Machu Picchu Clásico", destino1.nombre)
        assertEquals("Cusco, Perú", destino1.ubicacion)
        assertEquals("Descubre la majestuosa ciudadela inca de Machu Picchu", destino1.descripcion)
        assertEquals(450.0, destino1.precio, 0.01)
        assertNotNull(destino1.imagenUrl)
        assertTrue(destino1.imagenUrl.isNotEmpty())
        
        // Verificar segundo destino
        val destino2 = destinosObtenidos[1]
        assertEquals("dest_002", destino2.id)
        assertEquals("Líneas de Nazca Tour Aéreo", destino2.nombre)
        assertEquals(380.0, destino2.precio, 0.01)
    }

    @Test
    fun `test HU-001 Escenario 1 - Catálogo vacío muestra lista vacía`() = runTest(testDispatcher) {
        // Arrange: Configurar para que no haya destinos
        every { anyConstructed<DatabaseHelper>().obtenerTodosLosDestinos() } returns emptyList()

        // Act: Solicitar lista de destinos
        val destinosObtenidos = catalogoController.solicitarDestinos()
        advanceUntilIdle()

        // Assert: Verificar que se obtiene lista vacía
        assertTrue(destinosObtenidos.isEmpty())
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerTodosLosDestinos() }
    }

    @Test
    fun `test HU-001 Escenario 2 - Detalle del destino muestra información completa`() = runTest(testDispatcher) {
        // Arrange: Ya configurado en setUp con destino detallado

        // Act: Cargar detalle del destino
        val destinoDetalle = controlDetalleDestino.cargarDetalle(destinoId)
        advanceUntilIdle()

        // Assert: Verificar que se obtuvo el detalle correctamente
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerDestinoPorId(destinoId) }
        
        assertNotNull(destinoDetalle)
        destinoDetalle?.let { destino ->
            assertEquals("dest_001", destino.id)
            assertEquals("Tour Machu Picchu Clásico", destino.nombre)
            assertEquals("Cusco, Perú", destino.ubicacion)
            assertEquals(450.0, destino.precio, 0.01)
            assertEquals(12, destino.duracionHoras)
            assertNotNull(destino.incluye)
            assertTrue(destino.incluye.isNotEmpty())
            assertNotNull(destino.imagenUrl)
            assertTrue(destino.imagenUrl.isNotEmpty())
            assertEquals(4.8, destino.calificacion, 0.1)
            assertEquals(124, destino.numReseñas)
        }
    }

    @Test
    fun `test HU-001 Escenario 2 - Detalle de destino inexistente retorna null`() = runTest(testDispatcher) {
        // Arrange: Configurar para que no exista el destino
        val destinoInexistenteId = "dest_999"
        every { anyConstructed<DatabaseHelper>().obtenerDestinoPorId(destinoInexistenteId) } returns null

        // Act: Cargar detalle del destino inexistente
        val destinoDetalle = controlDetalleDestino.cargarDetalle(destinoInexistenteId)
        advanceUntilIdle()

        // Assert: Verificar que retorna null
        assertNull(destinoDetalle)
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerDestinoPorId(destinoInexistenteId) }
    }

    @Test
    fun `test catálogo muestra destinos con todas las propiedades requeridas`() = runTest(testDispatcher) {
        // Arrange: Ya configurado en setUp

        // Act: Solicitar lista de destinos
        val destinosObtenidos = catalogoController.solicitarDestinos()
        advanceUntilIdle()

        // Assert: Verificar que todos los destinos tienen las propiedades requeridas
        destinosObtenidos.forEach { destino ->
            assertNotNull("El destino debe tener ID", destino.id)
            assertNotNull("El destino debe tener nombre", destino.nombre)
            assertNotNull("El destino debe tener ubicación", destino.ubicacion)
            assertNotNull("El destino debe tener descripción", destino.descripcion)
            assertTrue("El destino debe tener precio mayor a 0", destino.precio > 0)
            assertNotNull("El destino debe tener URL de imagen", destino.imagenUrl)
            assertTrue("El destino debe tener URL de imagen no vacía", destino.imagenUrl.isNotEmpty())
        }
    }
}

