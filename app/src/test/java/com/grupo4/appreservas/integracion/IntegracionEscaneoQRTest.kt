package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.*
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.CheckInViewModel
import com.grupo4.appreservas.viewmodel.TourDelDiaViewModel
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
 * Pruebas de integración para HU-005: Escaneo de QR
 * 
 * Escenarios a probar:
 * 1. Escaneo de QR válido: El guía escanea el código del turista, el sistema valida el QR 
 *    y marca la asistencia como "Confirmada"
 * 2. Escaneo de QR inválido: El código ya fue usado o no corresponde, el sistema muestra 
 *    mensaje "QR no válido o ya registrado"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionEscaneoQRTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var checkInViewModel: CheckInViewModel
    private lateinit var tourDelDiaViewModel: TourDelDiaViewModel
    private lateinit var repository: PeruvianServiceRepository

    private val testDispatcher = StandardTestDispatcher()
    private val usuarioId = 1 // Guía
    private val tourId = "tour_001"
    private val reservaId = "RES_123456"
    private val codigoQR = "QR_123456"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper - IMPORTANTE: configurar mocks ANTES de instanciar el repositorio
        mockkConstructor(DatabaseHelper::class)
        // Mockear métodos básicos para evitar errores de base de datos
        // Estos mocks por defecto se pueden sobrescribir en cada test
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(any()) } returns null
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(any()) } returns null
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(any()) } returns false
        every { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().obtenerCheckInPorReserva(any()) } returns null
        every { anyConstructed<DatabaseHelper>().obtenerToursDelGuia(any(), any()) } returns emptyList()

        // Instanciar repositorio DESPUÉS de configurar todos los mocks
        repository = PeruvianServiceRepository.getInstance(context)
        // Los ViewModels se instanciarán en cada test después de configurar los mocks específicos
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
    fun `test HU-005 Escenario 1 - Escaneo de QR válido marca asistencia como confirmada`() = runTest {
        // Arrange: Crear reserva confirmada que pertenece al tour
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = "2", // Usuario turista
            usuarioId = 2,
            destinoId = "dest_001",
            tourId = tourId,
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            codigoQR = codigoQR,
            codigoConfirmacion = codigoQR
        )

        val checkInRegistrado = CheckIn(
            checkInId = 1,
            reservaId = reservaId,
            guiaId = usuarioId,
            horaRegistro = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            estado = "Confirmado"
        )

        // Mock: Buscar reserva por QR (usado por validarCodigoQR)
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQR) } returns null
        
        // Mock: Verificar si está usada (no está usada) - usado por validarCodigoQR
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns false
        
        // Mock: Registrar check-in - usado por registrarCheckIn
        every { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().obtenerCheckInPorReserva(reservaId) } returns checkInRegistrado

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        checkInViewModel = CheckInViewModel(application)

        // Observador para LiveData
        val resultadoObserver = mockk<Observer<CheckIn?>>(relaxed = true)
        checkInViewModel.resultadoCheckin.observeForever(resultadoObserver)

        // Act: Procesar escaneo QR
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se validó el QR (validarCodigoQR llama a obtenerReservaPorQR)
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) }
        // Verificar que se verificó si está usado (validarCodigoQR llama a estaReservaUsada)
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) }
        // Verificar que se registró el check-in
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
        
        // Verificar que se actualizó el resultado con el CheckIn
        verify { resultadoObserver.onChanged(any()) }
        
        // Verificar que el resultado es un CheckIn válido
        val resultado = checkInViewModel.resultadoCheckin.value
        assertNotNull(resultado)
        assertEquals(reservaId, resultado?.reservaId)
        assertEquals(usuarioId, resultado?.guiaId)
        assertEquals("Confirmado", resultado?.estado)
    }

    @Test
    fun `test HU-005 Escenario 2 - Escaneo de QR inválido muestra mensaje de error`() = runTest {
        // Arrange: QR que no existe
        val codigoQRInvalido = "QR_INVALIDO"

        // Mock: No se encuentra la reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQRInvalido) } returns null
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQRInvalido) } returns null

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        checkInViewModel = CheckInViewModel(application)

        // Observador para LiveData
        val mensajeObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.mensajeEstado.observeForever(mensajeObserver)

        // Act: Procesar escaneo QR inválido
        checkInViewModel.procesarEscaneoQR(codigoQRInvalido, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se intentó buscar la reserva (validarCodigoQR llama a obtenerReservaPorQR)
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQRInvalido) }
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQRInvalido) }
        
        // Verificar que NO se registró check-in
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
        
        // Verificar que se mostró mensaje de error
        verify { mensajeObserver.onChanged(any()) }
        
        // Verificar que el mensaje indica que el QR no es válido
        val mensaje = checkInViewModel.mensajeEstado.value
        assertNotNull(mensaje)
        assertTrue(mensaje!!.contains("no válido") || mensaje.contains("ya registrado"))
    }

    @Test
    fun `test escaneo de QR ya usado muestra mensaje de error`() = runTest {
        // Arrange: Reserva que ya tiene check-in
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = "2",
            usuarioId = 2,
            destinoId = "dest_001",
            tourId = tourId,
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            codigoQR = codigoQR,
            codigoConfirmacion = codigoQR
        )

        // IMPORTANTE: Resetear el repositorio singleton antes de configurar mocks específicos
        // para asegurar que use los nuevos mocks
        val field = PeruvianServiceRepository::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
        
        // Mock: Buscar reserva por QR (usado por validarCodigoQR)
        // Estos mocks deben configurarse ANTES de instanciar el ViewModel
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQR) } returns null
        
        // Mock: Verificar si está usada (ya está usada) - usado por validarCodigoQR
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns true

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        // Esto creará un nuevo repositorio con los mocks configurados
        repository = PeruvianServiceRepository.getInstance(context)
        checkInViewModel = CheckInViewModel(application)

        // Observador
        val mensajeObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.mensajeEstado.observeForever(mensajeObserver)

        // Act: Procesar escaneo QR ya usado
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se intentó validar el QR
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) }
        
        // Verificar que se detectó que está usada (validarCodigoQR llama a estaReservaUsada)
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) }
        
        // Verificar que NO se registró otro check-in
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
        
        // Verificar que se mostró un mensaje de error
        verify { mensajeObserver.onChanged(any()) }
        
        // Verificar que el mensaje indica que el QR ya fue usado
        val mensaje = checkInViewModel.mensajeEstado.value
        assertNotNull(mensaje)
        assertTrue(mensaje!!.contains("no válido") || mensaje.contains("ya registrado"))
    }

    @Test
    fun `test escaneo de QR que no pertenece al tour muestra mensaje de error`() = runTest {
        // Arrange: Reserva que pertenece a otro tour
        val otroTourId = "tour_002"
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = "2",
            usuarioId = 2,
            destinoId = "dest_001",
            tourId = otroTourId, // Tour diferente
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            codigoQR = codigoQR,
            codigoConfirmacion = codigoQR
        )

        // Mock: Buscar reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQR) } returns null
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns false

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        checkInViewModel = CheckInViewModel(application)

        // Observador
        val mensajeObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.mensajeEstado.observeForever(mensajeObserver)

        // Act: Procesar escaneo QR de otro tour
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId) // tourId diferente
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se intentó validar el QR
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) }
        
        // Verificar que NO se registró check-in (porque no pertenece al tour)
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
        
        // Verificar que se mostró mensaje de error
        verify { mensajeObserver.onChanged(any()) }
        
        // Verificar que el mensaje indica que el QR no es válido
        val mensaje = checkInViewModel.mensajeEstado.value
        assertNotNull(mensaje)
        assertTrue(mensaje!!.contains("no válido") || mensaje.contains("ya registrado"))
    }

    @Test
    fun `test cargar tours del día muestra lista de tours asignados al guía`() = runTest {
        // Arrange: Tours del día para el guía
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tours = listOf(
            Tour(
                tourId = "tour_001",
                nombre = "Machu Picchu Tour",
                fecha = fechaHoy,
                hora = "08:00",
                puntoEncuentro = "Plaza de Armas",
                capacidad = 15,
                participantesConfirmados = 5,
                estado = "Pendiente"
            ),
            Tour(
                tourId = "tour_002",
                nombre = "Valle Sagrado",
                fecha = fechaHoy,
                hora = "14:00",
                puntoEncuentro = "Hotel Plaza",
                capacidad = 20,
                participantesConfirmados = 10,
                estado = "Pendiente"
            )
        )

        // Mock: Obtener tours del guía
        every { anyConstructed<DatabaseHelper>().obtenerToursDelGuia(usuarioId, fechaHoy) } returns tours

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        tourDelDiaViewModel = TourDelDiaViewModel(application)

        // Observador
        val toursObserver = mockk<Observer<List<Tour>>>(relaxed = true)
        tourDelDiaViewModel.toursDelDia.observeForever(toursObserver)

        // Act: Cargar tours del día
        tourDelDiaViewModel.cargarToursDelDia(usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtuvieron los tours
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().obtenerToursDelGuia(usuarioId, fechaHoy) }
        verify { toursObserver.onChanged(any()) }
    }

    @Test
    fun `test flujo completo desde escaneo QR hasta confirmación de asistencia`() = runTest {
        // Arrange: Reserva válida
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = "2",
            usuarioId = 2,
            destinoId = "dest_001",
            tourId = tourId,
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            codigoQR = codigoQR,
            codigoConfirmacion = codigoQR
        )

        val checkInRegistrado = CheckIn(
            checkInId = 1,
            reservaId = reservaId,
            guiaId = usuarioId,
            horaRegistro = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            estado = "Confirmado"
        )

        // Mock: Buscar reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQR) } returns null
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns false
        every { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().obtenerCheckInPorReserva(reservaId) } returns checkInRegistrado

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        checkInViewModel = CheckInViewModel(application)

        // Observador
        val resultadoObserver = mockk<Observer<CheckIn?>>(relaxed = true)
        checkInViewModel.resultadoCheckin.observeForever(resultadoObserver)

        // Act: Procesar escaneo
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar flujo completo
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) }
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) }
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
        
        // Verificar que se retornó el CheckIn
        val resultado = checkInViewModel.resultadoCheckin.value
        assertNotNull(resultado)
        assertEquals(reservaId, resultado?.reservaId)
        assertEquals(usuarioId, resultado?.guiaId)
    }
}
