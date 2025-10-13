package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.EstadoPago
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Payment
import com.grupo4.appreservas.repository.PagoRepository
import com.grupo4.appreservas.repository.ReservasRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PagoServiceTest {

    private lateinit var pagoService: PagoService
    private lateinit var pagoRepository: PagoRepository
    private lateinit var reservasRepository: ReservasRepository

    @Before
    fun setUp() {
        pagoRepository = mockk()
        reservasRepository = mockk()
        pagoService = PagoService(pagoRepository, reservasRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test payYape procesa pago correctamente`() = runBlocking {
        // Arrange
        val req = mapOf(
            "bookingId" to "BK12345678",
            "monto" to 900.0
        )
        every { pagoRepository.save(any()) } answers {
            firstArg<Payment>().copy(
                id = "PAY12345678",
                transaccionId = "TXN123456"
            )
        }

        // Act
        val resultado = pagoService.payYape(req)

        // Assert
        assertEquals("BK12345678", resultado.bookingId)
        assertEquals(900.0, resultado.monto, 0.01)
        assertEquals(MetodoPago.YAPE, resultado.metodoPago)
        assertEquals(EstadoPago.APROBADO, resultado.estado)
        assertNotEquals("", resultado.id)
        verify(exactly = 1) { pagoRepository.save(any()) }
    }

    @Test
    fun `test payPlin procesa pago correctamente`() = runBlocking {
        // Arrange
        val req = mapOf(
            "bookingId" to "BK12345678",
            "monto" to 900.0
        )
        every { pagoRepository.save(any()) } answers {
            firstArg<Payment>().copy(
                id = "PAY12345678",
                transaccionId = "TXN123456"
            )
        }

        // Act
        val resultado = pagoService.payPlin(req)

        // Assert
        assertEquals(MetodoPago.PLIN, resultado.metodoPago)
        assertEquals(EstadoPago.APROBADO, resultado.estado)
    }

    @Test
    fun `test payCard procesa pago correctamente`() = runBlocking {
        // Arrange
        val req = mapOf(
            "bookingId" to "BK12345678",
            "monto" to 900.0
        )
        every { pagoRepository.save(any()) } answers {
            firstArg<Payment>().copy(
                id = "PAY12345678",
                transaccionId = "TXN123456"
            )
        }

        // Act
        val resultado = pagoService.payCard(req)

        // Assert
        assertEquals(MetodoPago.TARJETA, resultado.metodoPago)
        assertEquals(EstadoPago.APROBADO, resultado.estado)
    }

    @Test
    fun `test payYape toma tiempo de procesamiento simulado`() = runBlocking {
        // Arrange
        val req = mapOf(
            "bookingId" to "BK12345678",
            "monto" to 900.0
        )
        every { pagoRepository.save(any()) } answers {
            firstArg<Payment>().copy(id = "PAY12345678")
        }

        val startTime = System.currentTimeMillis()

        // Act
        pagoService.payYape(req)

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Assert - Debería tomar al menos 1500ms
        assertTrue(duration >= 1400) // Margen de error
    }
}
