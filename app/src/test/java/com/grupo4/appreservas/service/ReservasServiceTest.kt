package com.grupo4.appreservas.service

import android.content.Context
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.modelos.Usuario
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class ReservasServiceTest {

    private lateinit var reservasService: ReservasService
    private lateinit var reservasRepository: ReservasRepository
    private lateinit var destinoRepository: DestinoRepository
    private lateinit var availabilityService: AvailabilityService
    private lateinit var context: Context

    private val destinoMock = Destino(
        id = "dest_001",
        nombre = "Tour Test",
        precio = 450.0,
        maxPersonas = 15
    )

    @Before
    fun setUp() {
        reservasRepository = mockk()
        destinoRepository = mockk()
        availabilityService = mockk()
        context = mockk(relaxed = true)
        
        // Mock DatabaseHelper constructor para que no falle cuando se crea
        mockkConstructor(DatabaseHelper::class)
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(any()) } answers {
            val userId = firstArg<Int>()
            if (userId == 1) {
                Usuario(
                    usuarioId = 1,
                    nombreCompleto = "Usuario Test",
                    correo = "test@example.com",
                    contrasena = "password",
                    rolId = 2,
                    fechaCreacion = "2025-01-01"
                )
            } else {
                null
            }
        }
        
        reservasService = ReservasService(reservasRepository, destinoRepository, availabilityService, context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test crear reserva exitosamente con cupos disponibles`() {
        // Arrange
        val userId = "1"
        val destinoId = "dest_001"
        val tourSlotId = "dest_001_2025-10-14"
        val fecha = Date()
        val horaInicio = "08:00"
        val pax = 2

        every { destinoRepository.getDetalle(destinoId) } returns destinoMock
        every { availabilityService.lockSeats(tourSlotId, pax) } returns true
        every { reservasRepository.save(any()) } answers {
            firstArg<Reserva>().copy(id = "BK12345678")
        }

        // Act
        val resultado = reservasService.crear(userId, destinoId, tourSlotId, fecha, horaInicio, pax)

        // Assert
        assertNotNull(resultado)
        assertEquals(userId, resultado?.userId)
        assertEquals(destinoId, resultado?.destinoId)
        assertEquals(pax, resultado?.numPersonas)
        assertEquals(900.0, resultado!!.precioTotal, 0.01)
        assertEquals(EstadoReserva.PENDIENTE, resultado?.estado)
        // Verificar que se obtuvieron los datos del usuario
        assertEquals("Usuario Test", resultado?.nombreTurista)
        verify(exactly = 1) { availabilityService.lockSeats(tourSlotId, pax) }
    }

    @Test
    fun `test crear reserva falla cuando destino no existe`() {
        // Arrange
        val userId = "user_123"
        val destinoId = "dest_999"
        val tourSlotId = "dest_999_2025-10-14"
        val fecha = Date()
        val horaInicio = "08:00"
        val pax = 2

        every { destinoRepository.getDetalle(destinoId) } returns null

        // Act
        val resultado = reservasService.crear(userId, destinoId, tourSlotId, fecha, horaInicio, pax)

        // Assert
        assertNull(resultado)
        verify(exactly = 0) { availabilityService.lockSeats(any(), any()) }
    }

    @Test
    fun `test crear reserva falla cuando no hay cupos disponibles`() {
        // Arrange
        val userId = "user_123"
        val destinoId = "dest_001"
        val tourSlotId = "dest_001_2025-10-14"
        val fecha = Date()
        val horaInicio = "08:00"
        val pax = 20

        every { destinoRepository.getDetalle(destinoId) } returns destinoMock
        every { availabilityService.lockSeats(tourSlotId, pax) } returns false

        // Act
        val resultado = reservasService.crear(userId, destinoId, tourSlotId, fecha, horaInicio, pax)

        // Assert
        assertNull(resultado)
        verify(exactly = 1) { availabilityService.lockSeats(tourSlotId, pax) }
        verify(exactly = 0) { reservasRepository.save(any()) }
    }

    @Test
    fun `test confirmarPago actualiza estado correctamente`() {
        // Arrange
        val bookingId = "BK12345678"
        val metodoPago = "YAPE"
        val reservaMock = Reserva(
            id = bookingId,
            userId = "user_123",
            usuarioId = 1,
            destinoId = "dest_001",
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.PENDIENTE,
            nombreTurista = "Usuario Test",
            documento = "test@example.com"
        )

        every { reservasRepository.find(bookingId) } returns reservaMock
        every { reservasRepository.save(any()) } answers { firstArg() }

        // Act
        val resultado = reservasService.confirmarPago(bookingId, metodoPago)

        // Assert
        assertNotNull(resultado)
        assertEquals(EstadoReserva.CONFIRMADO, resultado?.estado)
        assertEquals(metodoPago, resultado?.metodoPago)
        assertNotEquals("", resultado?.codigoConfirmacion)
        assertTrue(resultado?.codigoConfirmacion?.startsWith("QR") ?: false)
    }

    @Test
    fun `test confirmarPago devuelve null cuando booking no existe`() {
        // Arrange
        val bookingId = "BK_INEXISTENTE"
        val metodoPago = "YAPE"

        every { reservasRepository.find(bookingId) } returns null

        // Act
        val resultado = reservasService.confirmarPago(bookingId, metodoPago)

        // Assert
        assertNull(resultado)
        verify(exactly = 0) { reservasRepository.save(any()) }
    }

    @Test
    fun `test obtenerReserva devuelve booking existente`() {
        // Arrange
        val bookingId = "BK12345678"
        val reservaMock = Reserva(
            id = bookingId,
            userId = "user_123",
            usuarioId = 1,
            destinoId = "dest_001",
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.PENDIENTE
        )

        every { reservasRepository.find(bookingId) } returns reservaMock

        // Act
        val resultado = reservasService.obtenerReserva(bookingId)

        // Assert
        assertNotNull(resultado)
        assertEquals(bookingId, resultado?.id)
    }
}