package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.repository.ReservasRepository
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class ReciboServiceTest {

    private lateinit var reciboService: ReciboService
    private lateinit var reservasRepository: ReservasRepository

    private val reservaMock = Reserva(
        id = "BK12345678",
        userId = "user_123",
        destinoId = "dest_001",
        destino = Destino(
            id = "dest_001",
            nombre = "Tour Machu Picchu",
            precio = 450.0
        ),
        fecha = Date(),
        horaInicio = "08:00",
        numPersonas = 2,
        precioTotal = 900.0,
        estado = EstadoReserva.PAGADA,
        codigoConfirmacion = "PS12345678",
        metodoPago = "YAPE"
    )

    @Before
    fun setUp() {
        reservasRepository = mockk()
        reciboService = ReciboService(reservasRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test emitir genera voucher correctamente`() {
        // Arrange
        val bookingId = "BK12345678"
        every { reservasRepository.find(bookingId) } returns reservaMock

        // Act
        val resultado = reciboService.emitir(bookingId)

        // Assert
        assertNotNull(resultado)
        assertEquals(bookingId, resultado?.bookingId)
        assertEquals("PS12345678", resultado?.codigoConfirmacion)
        assertEquals("Tour Machu Picchu", resultado?.destinoNombre)
        assertEquals(2, resultado?.numPersonas)
        assertEquals(900.0, resultado!!.montoTotal, 0.01)
        assertEquals("YAPE", resultado?.metodoPago)
        assertEquals("08:00", resultado?.horaInicio)
        assertTrue(resultado?.qrCode?.startsWith("QR_DATA_") ?: false)
    }

    @Test
    fun `test emitir devuelve null cuando booking no existe`() {
        // Arrange
        val bookingId = "BK_INEXISTENTE"
        every { reservasRepository.find(bookingId) } returns null

        // Act
        val resultado = reciboService.emitir(bookingId)

        // Assert
        assertNull(resultado)
    }

    @Test
    fun `test emitir devuelve null cuando booking no tiene destino`() {
        // Arrange
        val bookingId = "BK12345678"
        val bookingSinDestino = reservaMock.copy(destino = null)
        every { reservasRepository.find(bookingId) } returns bookingSinDestino

        // Act
        val resultado = reciboService.emitir(bookingId)

        // Assert
        assertNull(resultado)
    }

    @Test
    fun `test emitir genera QR con formato correcto`() {
        // Arrange
        val bookingId = "BK12345678"
        every { reservasRepository.find(bookingId) } returns reservaMock

        // Act
        val resultado = reciboService.emitir(bookingId)

        // Assert
        assertNotNull(resultado)
        assertEquals("QR_DATA_PS12345678", resultado?.qrCode)
    }
}
