package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.*
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.ReservaViewModel
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pruebas de integración para HU-002: Reservar un tour seleccionando fecha, hora y número de personas
 * 
 * Escenarios a probar:
 * 1. Selección de tour: El turista visualiza el detalle de un destino, selecciona "Reservar" e indica 
 *    fecha, hora y cantidad de personas, la app muestra la disponibilidad y permite confirmar la reserva
 * 2. Confirmación de reserva: El turista confirma los datos de la reserva, da clic en "Confirmar", 
 *    la app genera un resumen de lo que el usuario ha elegido junto con los distintos métodos de pago
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionReservaTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var viewModel: ReservaViewModel
    private lateinit var repository: PeruvianServiceRepository

    private val testDispatcher = StandardTestDispatcher()
    private val usuarioId = 1
    private val destinoId = "dest_001"
    private val fechaSeleccionada = "2024-12-15"
    private val horaSeleccionada = "09:00"
    private val cantidadPersonas = 2
    private val tourSlotId = "${destinoId}_${fechaSeleccionada}"

    private val destinoMock = Destino(
        id = destinoId,
        nombre = "Tour Machu Picchu Clásico",
        ubicacion = "Cusco, Perú",
        descripcion = "Descubre la majestuosa ciudadela inca",
        precio = 450.0,
        duracionHoras = 12,
        maxPersonas = 15,
        categorias = listOf("Cultura", "Arqueología"),
        imagenUrl = "https://example.com/machu.jpg",
        calificacion = 4.8,
        numReseñas = 124,
        disponibleTodosDias = true
    )

    private val tourMock = Tour(
        tourId = "tour_001",
        nombre = "Tour Machu Picchu Clásico",
        fecha = fechaSeleccionada,
        hora = horaSeleccionada,
        puntoEncuentro = "Plaza de Armas, Cusco",
        capacidad = 15,
        participantesConfirmados = 5
    )

    @Before
    fun setUp() {
        // Mock del Looper principal
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)

        // Forzar a ArchTaskExecutor a ejecutar todo en el mismo hilo
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
        every { anyConstructed<DatabaseHelper>().obtenerDestinoPorId(destinoId) } returns destinoMock
        every { anyConstructed<DatabaseHelper>().obtenerToursPorDestino(destinoId) } returns listOf(tourMock)
        every { anyConstructed<DatabaseHelper>().obtenerFechasDisponiblesPorDestino(destinoId) } returns listOf(fechaSeleccionada, "2024-12-16")
        every { anyConstructed<DatabaseHelper>().obtenerHorasDisponiblesPorDestinoYFecha(destinoId, fechaSeleccionada) } returns listOf(horaSeleccionada, "14:00")
        
        // Mock TourSlot para consultar cupos y bloquear asientos
        val tourSlotMock = com.grupo4.appreservas.modelos.TourSlot(
            tourSlotId = tourSlotId,
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaSeleccionada) ?: Date(),
            capacidad = 15,
            ocupados = 5
        )
        every { anyConstructed<DatabaseHelper>().obtenerTourSlotPorId(tourSlotId) } returns tourSlotMock
        every { anyConstructed<DatabaseHelper>().insertarTourSlot(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().actualizarTourSlot(any()) } returns 1
        
        every { anyConstructed<DatabaseHelper>().insertarReserva(any()) } returns 1L

        repository = PeruvianServiceRepository.getInstance(context)
        viewModel = ReservaViewModel(repository)
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        unmockkStatic(Looper::class)
        Dispatchers.resetMain()
        clearAllMocks()
        val field = PeruvianServiceRepository::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun `test HU-002 Escenario 1 - Consultar disponibilidad de asientos para un tour`() = runTest(testDispatcher) {
        // Arrange: Observador para cupos disponibles
        val cuposObserver = mockk<Observer<Int>>(relaxed = true)
        val disponibilidadObserver = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.cuposDisponibles.observeForever(cuposObserver)
        viewModel.disponibilidad.observeForever(disponibilidadObserver)

        // Act: Consultar disponibilidad
        viewModel.consultarDisponibilidadAsientos(tourSlotId)
        advanceUntilIdle()
        runBlocking { kotlinx.coroutines.delay(100) }

        // Assert: Verificar que se consultó la disponibilidad
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerTourSlotPorId(tourSlotId) }
        
        // Verificar que se actualizó el LiveData
        verify { cuposObserver.onChanged(any()) }
        verify { disponibilidadObserver.onChanged(any()) }
        
        // Verificar valores
        val cuposDisponibles = repository.consultarCuposDisponibles(tourSlotId)
        assertEquals(10, cuposDisponibles) // capacidad (15) - ocupados (5) = 10
        assertTrue(cuposDisponibles > 0)
    }

    @Test
    fun `test HU-002 Escenario 1 - Sin cupos disponibles muestra disponibilidad false`() = runTest(testDispatcher) {
        // Arrange: Configurar sin cupos disponibles (capacidad = ocupados)
        val tourSlotLleno = com.grupo4.appreservas.modelos.TourSlot(
            tourSlotId = tourSlotId,
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaSeleccionada) ?: Date(),
            capacidad = 15,
            ocupados = 15
        )
        every { anyConstructed<DatabaseHelper>().obtenerTourSlotPorId(tourSlotId) } returns tourSlotLleno

        val disponibilidadObserver = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.disponibilidad.observeForever(disponibilidadObserver)

        // Act: Consultar disponibilidad
        viewModel.consultarDisponibilidadAsientos(tourSlotId)
        advanceUntilIdle()
        runBlocking { kotlinx.coroutines.delay(100) }

        // Assert: Verificar que no hay disponibilidad
        val cuposDisponibles = repository.consultarCuposDisponibles(tourSlotId)
        assertEquals(0, cuposDisponibles)
        verify { disponibilidadObserver.onChanged(false) }
    }

    @Test
    fun `test HU-002 Escenario 2 - Crear reserva exitosamente con fecha, hora y cantidad de personas`() = runTest(testDispatcher) {
        // Arrange: Ya configurado en setUp

        // Act: Crear reserva
        val reserva = viewModel.crearReserva(usuarioId, tourSlotId, cantidadPersonas)
        advanceUntilIdle()
        runBlocking { kotlinx.coroutines.delay(100) }

        // Assert: Verificar que se creó la reserva
        assertNotNull(reserva)
        reserva?.let { r ->
            assertEquals(usuarioId, r.usuarioId)
            assertEquals(destinoId, r.destinoId)
            assertEquals(tourSlotId, r.tourSlotId)
            assertEquals(cantidadPersonas, r.numPersonas)
            assertEquals(EstadoReserva.PENDIENTE, r.estado)
            assertEquals(900.0, r.precioTotal, 0.01) // 450.0 * 2 personas
            assertNotNull(r.codigoConfirmacion)
            assertNotNull(r.codigoQR)
            assertTrue(r.codigoConfirmacion.isNotEmpty())
            assertTrue(r.codigoQR.isNotEmpty())
        }

        // Verificar que se bloqueó los asientos (actualizando el tour slot)
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerTourSlotPorId(tourSlotId) }
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().actualizarTourSlot(any()) }
        
        // Verificar que se guardó la reserva
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarReserva(any()) }
    }

    @Test
    fun `test HU-002 Escenario 2 - No se puede crear reserva si no hay cupos suficientes`() = runTest(testDispatcher) {
        // Arrange: Configurar tour slot sin cupos suficientes
        val tourSlotSinCupos = com.grupo4.appreservas.modelos.TourSlot(
            tourSlotId = tourSlotId,
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaSeleccionada) ?: Date(),
            capacidad = 15,
            ocupados = 14 // Solo queda 1 cupo, pero necesitamos 2
        )
        every { anyConstructed<DatabaseHelper>().obtenerTourSlotPorId(tourSlotId) } returns tourSlotSinCupos

        // Act: Intentar crear reserva
        val reserva = viewModel.crearReserva(usuarioId, tourSlotId, cantidadPersonas)
        advanceUntilIdle()
        runBlocking { kotlinx.coroutines.delay(100) }

        // Assert: Verificar que no se creó la reserva
        assertNull(reserva)
        
        // Verificar que se intentó consultar cupos
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerTourSlotPorId(tourSlotId) }
        
        // Verificar que NO se guardó la reserva
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().insertarReserva(any()) }
    }

    @Test
    fun `test obtener fechas disponibles para un destino`() = runTest(testDispatcher) {
        // Arrange: Ya configurado en setUp

        // Act: Obtener fechas disponibles
        val fechasDisponibles = repository.obtenerFechasDisponibles(destinoId)
        advanceUntilIdle()

        // Assert: Verificar que se obtuvieron las fechas
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerFechasDisponiblesPorDestino(destinoId) }
        assertEquals(2, fechasDisponibles.size)
        assertTrue(fechasDisponibles.contains(fechaSeleccionada))
    }

    @Test
    fun `test obtener horas disponibles para un destino y fecha`() = runTest(testDispatcher) {
        // Arrange: Ya configurado en setUp

        // Act: Obtener horas disponibles
        val horasDisponibles = repository.obtenerHorasDisponibles(destinoId, fechaSeleccionada)
        advanceUntilIdle()

        // Assert: Verificar que se obtuvieron las horas
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerHorasDisponiblesPorDestinoYFecha(destinoId, fechaSeleccionada) }
        assertEquals(2, horasDisponibles.size)
        assertTrue(horasDisponibles.contains(horaSeleccionada))
    }

    @Test
    fun `test reserva contiene resumen completo con precio total calculado`() = runTest(testDispatcher) {
        // Arrange: Ya configurado en setUp

        // Act: Crear reserva
        val reserva = viewModel.crearReserva(usuarioId, tourSlotId, cantidadPersonas)
        advanceUntilIdle()
        runBlocking { kotlinx.coroutines.delay(100) }

        // Assert: Verificar que la reserva contiene el resumen completo
        assertNotNull(reserva)
        reserva?.let { r ->
            // Verificar información del destino
            assertNotNull(r.destino)
            assertEquals(destinoMock.nombre, r.destino?.nombre)
            
            // Verificar información de fecha y hora
            assertNotNull(r.fecha)
            assertEquals(horaSeleccionada, r.horaInicio)
            
            // Verificar cálculo de precio total
            val precioEsperado = destinoMock.precio * cantidadPersonas
            assertEquals(precioEsperado, r.precioTotal, 0.01)
            
            // Verificar que tiene código de confirmación para el pago
            assertNotNull(r.codigoConfirmacion)
            assertTrue(r.codigoConfirmacion.isNotEmpty())
        }
    }
}

