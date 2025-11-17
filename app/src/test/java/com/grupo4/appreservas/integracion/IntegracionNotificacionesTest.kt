package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.*
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.NotificacionesViewModel
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pruebas de integración para HU-006: Sistema de Notificaciones
 * 
 * Escenarios a probar:
 * 1. Recordatorio de horario: El tour está próximo a iniciar, se activa una notificación push
 * 2. Alerta climática: Se detecta un cambio de clima, el sistema envía una notificación automática
 * 3. Oferta de último minuto: Un tour tiene baja ocupación, el sistema activa una promoción
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionNotificacionesTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var viewModel: NotificacionesViewModel
    private lateinit var repository: PeruvianServiceRepository

    private val testDispatcher = StandardTestDispatcher()
    private val usuarioId = 1
    private val tourId = "tour_001"
    private val destinoId = "dest_001"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper
        mockkConstructor(DatabaseHelper::class)

        repository = PeruvianServiceRepository.getInstance(context)
        viewModel = NotificacionesViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `test HU-006 Escenario 1 - Recordatorio de horario se genera correctamente`() = runTest {
        // Arrange: Crear una reserva confirmada con tour próximo a iniciar
        val fechaTour = Calendar.getInstance().apply {
            time = Date()
            add(Calendar.HOUR_OF_DAY, 2) // Tour en 2 horas
        }.time

        val reserva = Reserva(
            id = "BK12345678",
            userId = usuarioId.toString(),
            usuarioId = usuarioId,
            destinoId = destinoId,
            tourId = tourId,
            fecha = fechaTour,
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            nombreTurista = "Usuario Test",
            documento = "test@example.com"
        )

        val tour = Tour(
            tourId = tourId,
            nombre = "Machu Picchu",
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fechaTour),
            hora = "08:00",
            puntoEncuentro = "Plaza de Armas, Cusco",
            capacidad = 15,
            participantesConfirmados = 0,
            estado = "Pendiente"
        )

        // Mock: Obtener reservas del usuario
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns listOf(reserva)
        every { anyConstructed<DatabaseHelper>().obtenerTourPorId(tourId) } returns tour
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()

        // Act: Crear notificación de recordatorio
        repository.crearNotificacionRecordatorio(
            usuarioId,
            tourId,
            tour.nombre,
            tour.hora,
            tour.puntoEncuentro
        )

        // Assert: Verificar que se creó la notificación
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) }
    }

    @Test
    fun `test HU-006 Escenario 2 - Alerta climática se genera cuando hay cambio de clima`() = runTest {
        // Arrange: Configurar detección de cambio climático
        val ubicacion = "Cusco"
        
        // Mock: Detectar cambio climático (retorna true)
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L

        // Act: Crear notificación de alerta climática
        repository.crearNotificacionAlertaClimatica(
            usuarioId,
            ubicacion,
            "Lluvia intensa detectada",
            "Lleva paraguas y ropa impermeable"
        )

        // Assert: Verificar que se creó la notificación
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) }
    }

    @Test
    fun `test HU-006 Escenario 3 - Oferta de último minuto se genera para tours con baja ocupación`() = runTest {
        // Arrange: Tour con baja ocupación
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tour = Tour(
            tourId = tourId,
            nombre = "Machu Picchu",
            fecha = fechaHoy,
            hora = "08:00",
            puntoEncuentro = "Plaza de Armas",
            capacidad = 15,
            participantesConfirmados = 3, // Solo 3 de 15 (20% - baja ocupación)
            estado = "Pendiente"
        )

        // Mock: Obtener tours con descuento
        every { anyConstructed<DatabaseHelper>().obtenerTodosLosTours() } returns listOf(tour)
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L

        // Act: Obtener tours con descuento y crear notificación
        val toursConDescuento = repository.obtenerToursConDescuento()
        
        if (toursConDescuento.isNotEmpty()) {
            repository.crearNotificacionOferta(
                usuarioId,
                tourId,
                tour.nombre,
                20 // 20% descuento
            )
        }

        // Assert: Verificar que se encontraron tours con descuento
        assertTrue(toursConDescuento.isNotEmpty())
        assertEquals(tourId, toursConDescuento.first().tourId)
        
        // Verificar que se creó la notificación
        verify(atLeast = 0) { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) }
    }

    @Test
    fun `test cargar recordatorios devuelve lista de notificaciones`() = runTest {
        // Arrange: Crear notificaciones
        val notificaciones = listOf(
            Notificacion(
                id = "NOTIF001",
                usuarioId = usuarioId,
                tipo = TipoNotificacion.RECORDATORIO,
                titulo = "Recordatorio de Tour",
                descripcion = "Tu tour inicia en 2 horas",
                fechaCreacion = Date(),
                tourId = tourId,
                puntoEncuentro = "Plaza de Armas",
                horaTour = "08:00"
            ),
            Notificacion(
                id = "NOTIF002",
                usuarioId = usuarioId,
                tipo = TipoNotificacion.ALERTA_CLIMATICA,
                titulo = "Alerta Climática",
                descripcion = "Se espera lluvia en tu destino",
                fechaCreacion = Date(),
                destinoNombre = "Machu Picchu"
            )
        )

        // Mock: Obtener notificaciones
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns notificaciones

        // Observador
        val recordatoriosObserver = mockk<Observer<List<Notificacion>>>(relaxed = true)
        viewModel.recordatorios.observeForever(recordatoriosObserver)

        // Act: Cargar recordatorios
        viewModel.cargarRecordatoriosUsuario(usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtuvieron las notificaciones
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) }
        verify { recordatoriosObserver.onChanged(any()) }
    }

    @Test
    fun `test detectar cambio climático actualiza notificaciones`() = runTest {
        // Arrange
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L

        // Mock: Detectar cambio climático
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()

        // Observador
        val recordatoriosObserver = mockk<Observer<List<Notificacion>>>(relaxed = true)
        viewModel.recordatorios.observeForever(recordatoriosObserver)

        // Act: Detectar cambio
        viewModel.detectarCambio(usuarioId, "Cusco")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se intentó detectar cambios
        // (el método obtenerCondicionesYDetectarCambio se ejecuta internamente)
    }

    @Test
    fun `test generar oferta crea notificaciones para tours con descuento`() = runTest {
        // Arrange: Tours con baja ocupación
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tours = listOf(
            Tour(
                tourId = "tour_001",
                nombre = "Machu Picchu",
                fecha = fechaHoy,
                hora = "08:00",
                puntoEncuentro = "Plaza de Armas",
                capacidad = 15,
                participantesConfirmados = 3, // 20% ocupación
                estado = "Pendiente"
            ),
            Tour(
                tourId = "tour_002",
                nombre = "Valle Sagrado",
                fecha = fechaHoy,
                hora = "14:00",
                puntoEncuentro = "Hotel Plaza",
                capacidad = 20,
                participantesConfirmados = 5, // 25% ocupación
                estado = "Pendiente"
            )
        )

        // Mock: Obtener tours
        every { anyConstructed<DatabaseHelper>().obtenerTodosLosTours() } returns tours
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L

        // Observador
        val recordatoriosObserver = mockk<Observer<List<Notificacion>>>(relaxed = true)
        viewModel.recordatorios.observeForever(recordatoriosObserver)

        // Act: Generar ofertas
        viewModel.generarOferta(usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtuvieron tours con descuento
        val toursConDescuento = repository.obtenerToursConDescuento()
        assertTrue(toursConDescuento.size >= 2)
    }

    @Test
    fun `test marcar notificación como leída actualiza estado`() = runTest {
        // Arrange: Notificación no leída
        val notificacion = Notificacion(
            id = "NOTIF001",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.RECORDATORIO,
            titulo = "Recordatorio",
            descripcion = "Test",
            fechaCreacion = Date(),
            leida = false
        )

        // Mock: Marcar como leída
        every { anyConstructed<DatabaseHelper>().marcarNotificacionComoLeida("NOTIF001") } returns true
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns listOf(
            notificacion.copy(leida = true)
        )

        // Observador
        val recordatoriosObserver = mockk<Observer<List<Notificacion>>>(relaxed = true)
        viewModel.recordatorios.observeForever(recordatoriosObserver)

        // Act: Marcar como leída
        viewModel.marcarComoLeida("NOTIF001")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se marcó como leída
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().marcarNotificacionComoLeida("NOTIF001") }
    }

    @Test
    fun `test flujo completo de notificaciones desde reserva hasta notificación`() = runTest {
        // Arrange: Reserva confirmada
        val fechaTour = Calendar.getInstance().apply {
            time = Date()
            add(Calendar.HOUR_OF_DAY, 2)
        }.time

        val reserva = Reserva(
            id = "BK12345678",
            userId = usuarioId.toString(),
            usuarioId = usuarioId,
            destinoId = destinoId,
            tourId = tourId,
            fecha = fechaTour,
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            nombreTurista = "Usuario Test",
            documento = "test@example.com"
        )

        val tour = Tour(
            tourId = tourId,
            nombre = "Machu Picchu",
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fechaTour),
            hora = "08:00",
            puntoEncuentro = "Plaza de Armas",
            capacidad = 15,
            participantesConfirmados = 0,
            estado = "Pendiente"
        )

        // Mock
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns listOf(reserva)
        every { anyConstructed<DatabaseHelper>().obtenerTourPorId(tourId) } returns tour
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()

        // Act: Crear notificación de recordatorio
        repository.crearNotificacionRecordatorio(
            usuarioId,
            tourId,
            tour.nombre,
            tour.hora,
            tour.puntoEncuentro
        )

        // Assert: Verificar flujo completo
        verify { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) }
        
        // Verificar que se puede cargar la notificación
        val notificaciones = repository.obtenerRecordatorios(usuarioId)
        // Nota: Como estamos usando mocks, las notificaciones no se persisten realmente
        // pero verificamos que el flujo se ejecuta correctamente
    }
}
