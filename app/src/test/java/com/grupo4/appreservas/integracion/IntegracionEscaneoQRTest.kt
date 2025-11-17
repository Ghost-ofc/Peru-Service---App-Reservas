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
 * 1. Escaneo de QR válido: El guía escanea el código del turista, el sistema valida y marca asistencia
 * 2. Escaneo de QR inválido: El código ya fue usado o no corresponde, el sistema muestra mensaje de error
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

        // Mock DatabaseHelper
        mockkConstructor(DatabaseHelper::class)

        repository = PeruvianServiceRepository.getInstance(context)
        checkInViewModel = CheckInViewModel(application)
        tourDelDiaViewModel = TourDelDiaViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
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

        val tour = Tour(
            tourId = tourId,
            nombre = "Machu Picchu Tour",
            fecha = fechaHoy,
            hora = "08:00",
            puntoEncuentro = "Plaza de Armas",
            capacidad = 15,
            participantesConfirmados = 0,
            estado = "Pendiente"
        )

        // Mock: Buscar reserva por QR
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQR) } returns null
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) } returns reserva
        
        // Mock: Verificar si está usada (no está usada)
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns false
        
        // Mock: Registrar check-in
        every { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) } returns 1L

        // Observador para LiveData
        val resultadoObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.resultadoEscaneo.observeForever(resultadoObserver)

        // Act: Procesar escaneo QR
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se validó el QR
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) }
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) }
        
        // Verificar que se registró el check-in
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
        
        // Verificar que se actualizó el resultado
        verify { resultadoObserver.onChanged(any()) }
    }

    @Test
    fun `test HU-005 Escenario 2 - Escaneo de QR inválido muestra mensaje de error`() = runTest {
        // Arrange: QR que no existe
        val codigoQRInvalido = "QR_INVALIDO"

        // Mock: No se encuentra la reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQRInvalido) } returns null
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(codigoQRInvalido) } returns null

        // Observador para LiveData
        val resultadoObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.resultadoEscaneo.observeForever(resultadoObserver)

        // Act: Procesar escaneo QR inválido
        checkInViewModel.procesarEscaneoQR(codigoQRInvalido, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se intentó buscar la reserva
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQRInvalido) }
        
        // Verificar que NO se registró check-in
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
        
        // Verificar que se mostró mensaje de error
        verify { resultadoObserver.onChanged(any()) }
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

        // Mock: Buscar reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) } returns reserva
        
        // Mock: Ya está usada
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns true

        // Observador
        val resultadoObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.resultadoEscaneo.observeForever(resultadoObserver)

        // Act: Procesar escaneo QR ya usado
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se detectó que está usada
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) }
        
        // Verificar que NO se registró otro check-in
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
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
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) } returns reserva
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns false

        // Observador
        val resultadoObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.resultadoEscaneo.observeForever(resultadoObserver)

        // Act: Procesar escaneo QR de otro tour
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId) // tourId diferente
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se detectó que no pertenece al tour
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) }
        
        // Verificar que NO se registró check-in
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
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

        // Mock: Buscar reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) } returns reserva
        every { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) } returns false
        every { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) } returns 1L

        // Observador
        val resultadoObserver = mockk<Observer<String?>>(relaxed = true)
        checkInViewModel.resultadoEscaneo.observeForever(resultadoObserver)

        // Act: Procesar escaneo
        checkInViewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar flujo completo
        verify { anyConstructed<DatabaseHelper>().obtenerReservaPorQR(codigoQR) }
        verify { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) }
        verify { anyConstructed<DatabaseHelper>().estaReservaUsada(codigoQR) }
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().registrarCheckIn(any()) }
    }
}

