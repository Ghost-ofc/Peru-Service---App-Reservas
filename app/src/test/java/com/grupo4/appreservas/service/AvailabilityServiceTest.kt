package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.TourSlot
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.ReservasRepository
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class AvailabilityServiceTest {

    private lateinit var availabilityService: AvailabilityService
    private lateinit var destinoRepository: DestinoRepository
    private lateinit var reservasRepository: ReservasRepository

    private val destinoMock = Destino(
        id = "dest_001",
        nombre = "Tour Test",
        maxPersonas = 15
    )

    @Before
    fun setUp() {
        destinoRepository = mockk()
        reservasRepository = mockk()
        availabilityService = AvailabilityService(destinoRepository, reservasRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test consultarDisponibilidad devuelve información cuando slot existe`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14"
        val tourSlot = TourSlot(
            tourSlotId = tourSlotId,
            fecha = Date(),
            capacidad = 15,
            ocupados = 5
        )
        every { destinoRepository.getDetalle("dest_001") } returns destinoMock
        every { reservasRepository.findTourSlot(tourSlotId) } returns tourSlot

        // Act
        val resultado = availabilityService.consultarDisponibilidad(tourSlotId)

        // Assert
        assertEquals(10, resultado["cuposDisponibles"])
        assertEquals(15, resultado["cuposTotales"])
        assertEquals(5, resultado["ocupados"])
        assertEquals(true, resultado["disponible"])
    }

    @Test
    fun `test consultarDisponibilidad crea slot nuevo cuando no existe`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14"
        val tourSlotNuevo = TourSlot(
            tourSlotId = tourSlotId,
            fecha = Date(),
            capacidad = 15,
            ocupados = 0
        )
        every { destinoRepository.getDetalle("dest_001") } returns destinoMock
        every { reservasRepository.findTourSlot(tourSlotId) } returns null
        every { reservasRepository.crearTourSlotSiNoExiste(any(), any(), any()) } returns tourSlotNuevo

        // Act
        val resultado = availabilityService.consultarDisponibilidad(tourSlotId)

        // Assert
        assertEquals(15, resultado["cuposDisponibles"])
        assertEquals(0, resultado["ocupados"])
        verify(exactly = 1) { reservasRepository.crearTourSlotSiNoExiste(any(), any(), any()) }
    }

    @Test
    fun `test consultarDisponibilidad devuelve no disponible cuando destino no existe`() {
        // Arrange
        val tourSlotId = "dest_999_2025-10-14"
        every { destinoRepository.getDetalle("dest_999") } returns null

        // Act
        val resultado = availabilityService.consultarDisponibilidad(tourSlotId)

        // Assert
        assertEquals(0, resultado["cuposDisponibles"])
        assertEquals(false, resultado["disponible"])
    }

    @Test
    fun `test verificarYBloquearCupos bloquea exitosamente cuando hay capacidad`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14"
        val numPersonas = 5
        val tourSlot = TourSlot(
            tourSlotId = tourSlotId,
            fecha = Date(),
            capacidad = 15,
            ocupados = 5
        )
        every { reservasRepository.findTourSlot(tourSlotId) } returns tourSlot
        every { reservasRepository.saveTourSlot(any()) } just Runs

        // Act
        val resultado = availabilityService.verificarYBloquearCupos(tourSlotId, numPersonas)

        // Assert
        assertTrue(resultado)
        verify(exactly = 1) { reservasRepository.saveTourSlot(match { it.ocupados == 10 }) }
    }

    @Test
    fun `test verificarYBloquearCupos falla cuando no hay capacidad`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14"
        val numPersonas = 15
        val tourSlot = TourSlot(
            tourSlotId = tourSlotId,
            fecha = Date(),
            capacidad = 15,
            ocupados = 10
        )
        every { reservasRepository.findTourSlot(tourSlotId) } returns tourSlot

        // Act
        val resultado = availabilityService.verificarYBloquearCupos(tourSlotId, numPersonas)

        // Assert
        assertFalse(resultado)
        verify(exactly = 0) { reservasRepository.saveTourSlot(any()) }
    }

    @Test
    fun `test liberarCupos libera cupos correctamente`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14"
        val numPersonas = 3
        val tourSlot = TourSlot(
            tourSlotId = tourSlotId,
            fecha = Date(),
            capacidad = 15,
            ocupados = 8
        )
        every { reservasRepository.findTourSlot(tourSlotId) } returns tourSlot
        every { reservasRepository.saveTourSlot(any()) } just Runs

        // Act
        availabilityService.liberarCupos(tourSlotId, numPersonas)

        // Assert
        verify(exactly = 1) { reservasRepository.saveTourSlot(match { it.ocupados == 5 }) }
    }

    @Test
    fun `test liberarCupos no baja de cero ocupados`() {
        // Arrange
        val tourSlotId = "dest_001_2025-10-14"
        val numPersonas = 10
        val tourSlot = TourSlot(
            tourSlotId = tourSlotId,
            fecha = Date(),
            capacidad = 15,
            ocupados = 3
        )
        every { reservasRepository.findTourSlot(tourSlotId) } returns tourSlot
        every { reservasRepository.saveTourSlot(any()) } just Runs

        // Act
        availabilityService.liberarCupos(tourSlotId, numPersonas)

        // Assert
        verify(exactly = 1) { reservasRepository.saveTourSlot(match { it.ocupados == 0 }) }
    }
}