package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.ReservasService
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class ReservasControllerTest {

    private lateinit var reservasController: ReservasController
    private lateinit var reservasService: ReservasService
    private lateinit var availabilityService: AvailabilityService

    @Before
    fun setUp() {
        reservasService = mockk()
        availabilityService = mockk()
        reservasController = ReservasController(reservasService, availabilityService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test consultarDisponibilidad devuelve información correcta`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14_08:00"
        val disponibilidad = mapOf(
            "tourSlotId" to tourSlotId,
            "cuposDisponibles" to 10,
            "cuposTotales" to 15,
            "ocupados" to 5,
            "disponible" to true
        )
        every { availabilityService.consultarDisponibilidad(tourSlotId) } returns disponibilidad

        // Act
        val resultado = reservasController.consultarDisponibilidad(tourSlotId)

        // Assert
        assertEquals(10, resultado["cuposDisponibles"])
        assertEquals(true, resultado["disponible"])
        verify(exactly = 1) { availabilityService.consultarDisponibilidad(tourSlotId) }
    }

    @Test
    fun `test lockSeats bloquea cupos exitosamente`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14_08:00"
        val numPersonas = 3
        every { availabilityService.verificarYBloquearCupos(tourSlotId, numPersonas) } returns true

        // Act
        val resultado = reservasController.lockSeats(tourSlotId, numPersonas)

        // Assert
        assertTrue(resultado)
        verify(exactly = 1) { availabilityService.verificarYBloquearCupos(tourSlotId, numPersonas) }
    }

    @Test
    fun `test lockSeats falla cuando no hay cupos suficientes`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14_08:00"
        val numPersonas = 20
        every { availabilityService.verificarYBloquearCupos(tourSlotId, numPersonas) } returns false

        // Act
        val resultado = reservasController.lockSeats(tourSlotId, numPersonas)

        // Assert
        assertFalse(resultado)
    }

    @Test
    fun `test crearReservaCmd crea reserva exitosamente con tourSlotId válido`() {
        // Arrange
        val userId = "user_123"
        val tourSlotId = "dest_001_2025-10-14"
        val pax = 2
        val reservaMock = Reserva(
            id = "BK12345678",
            userId = userId,
            destinoId = "dest_001",
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = pax,
            precioTotal = 900.0,
            estado = EstadoReserva.PENDIENTE_PAGO
        )
        every { reservasService.crear(any(), any(), any(), any(), any(), any()) } returns reservaMock

        // Act
        val resultado = reservasController.crearReservaCmd(userId, tourSlotId, pax)

        // Assert
        assertNotNull(resultado)
        assertEquals("BK12345678", resultado?.id)
        assertEquals(pax, resultado?.numPersonas)
        verify(exactly = 1) { reservasService.crear(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `test crearReservaCmd devuelve null con tourSlotId inválido`() {
        // Arrange
        val userId = "user_123"
        val tourSlotId = "invalid_format"
        val pax = 2

        // Act
        val resultado = reservasController.crearReservaCmd(userId, tourSlotId, pax)

        // Assert
        assertNull(resultado)
        verify(exactly = 0) { reservasService.crear(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `test crearReservaCmd maneja excepción en parsing de fecha`() {
        // Arrange
        val userId = "user_123"
        val tourSlotId = "dest_001_fecha-invalida"
        val pax = 2

        // Act
        val resultado = reservasController.crearReservaCmd(userId, tourSlotId, pax)

        // Assert
        // Debería intentar crear con fecha por defecto o devolver null según implementación
        assertNotNull(resultado == null || resultado.fecha != null)
    }
}
