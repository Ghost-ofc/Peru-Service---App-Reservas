package com.grupo4.appreservas.integracion

import com.grupo4.appreservas.controller.CatalogoController
import com.grupo4.appreservas.controller.ReservasController
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.modelos.TourSlot
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinoService
import com.grupo4.appreservas.service.ReservasService
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class IntegracionCatalogoReservaTest {

    private lateinit var catalogoController: CatalogoController
    private lateinit var reservasController: ReservasController
    private lateinit var destinoRepository: DestinoRepository
    private lateinit var reservasRepository: ReservasRepository
    private lateinit var destinationService: DestinoService
    private lateinit var availabilityService: AvailabilityService
    private lateinit var reservasService: ReservasService

    @Before
    fun setUp() {
        destinoRepository = mockk()
        reservasRepository = mockk()

        destinationService = DestinoService(destinoRepository)
        availabilityService = AvailabilityService(destinoRepository, reservasRepository)
        reservasService = ReservasService(reservasRepository, destinoRepository, availabilityService)

        catalogoController = CatalogoController(destinationService, availabilityService)
        reservasController = ReservasController(reservasService, availabilityService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test flujo completo desde catálogo hasta reserva creada`() {
        // Arrange
        val destino = Destino(
            id = "dest_001",
            nombre = "Tour Test",
            precio = 450.0,
            maxPersonas = 15
        )
        val tourSlotId = "dest_001_2025-10-14"
        val tourSlot = TourSlot(
            tourSlotId = tourSlotId,
            fecha = Date(),
            capacidad = 15,
            ocupados = 0
        )

        every { destinationService.listarDestinos() } returns listOf(destino)
        every { destinoRepository.getDestinos() } returns listOf(destino)
        every { destinoRepository.getDetalle("dest_001") } returns destino
        every { reservasRepository.findTourSlot(tourSlotId) } returns tourSlot
        every { reservasRepository.saveTourSlot(any()) } just Runs
        every { reservasRepository.save(any()) } answers {
            firstArg<Reserva>().copy(id = "BK12345678")
        }

        // Act
        // 1. Usuario busca destinos
        val destinos = catalogoController.solicitarDestinos()

        // 2. Usuario consulta disponibilidad
        val disponibilidad = reservasController.consultarDisponibilidad(tourSlotId)

        // 3. Usuario bloquea cupos
        val seatsLocked = reservasController.lockSeats(tourSlotId, 2)

        // 4. Usuario crea reserva
        val booking = reservasController.crearReservaCmd("user_123", tourSlotId, 2)

        // Assert
        assertNotNull(destinos)
        assertEquals(1, destinos.size)
        assertEquals(15, disponibilidad["cuposDisponibles"])
        assertTrue(seatsLocked)
        assertNotNull(booking)
        assertEquals(EstadoReserva.PENDIENTE_PAGO, booking?.estado)
    }
}