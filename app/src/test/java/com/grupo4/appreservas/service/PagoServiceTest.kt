package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.EstadoPago
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.repository.PagoRepository
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.service.PaymentGateway
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
    private lateinit var paymentGateway: PaymentGateway

    @Before
    fun setUp() {
        pagoRepository = mockk()
        reservasRepository = mockk()
        paymentGateway = mockk()
        pagoService = PagoService(pagoRepository, reservasRepository, paymentGateway)
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
        val paymentFromGateway = Pago(
            id = "",
            bookingId = "BK12345678",
            monto = 900.0,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )
        
        coEvery { paymentGateway.charge(any()) } returns paymentFromGateway
        every { pagoRepository.save(any()) } answers {
            firstArg<Pago>().copy(
                id = "PAY12345678"
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
        coVerify(exactly = 1) { paymentGateway.charge(any()) }
        verify(exactly = 1) { pagoRepository.save(any()) }
    }

    @Test
    fun `test payPlin procesa pago correctamente`() = runBlocking {
        // Arrange
        val req = mapOf(
            "bookingId" to "BK12345678",
            "monto" to 900.0
        )
        val paymentFromGateway = Pago(
            id = "",
            bookingId = "BK12345678",
            monto = 900.0,
            metodoPago = MetodoPago.PLIN,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )
        
        coEvery { paymentGateway.charge(any()) } returns paymentFromGateway
        every { pagoRepository.save(any()) } answers {
            firstArg<Pago>().copy(id = "PAY12345678")
        }

        // Act
        val resultado = pagoService.payPlin(req)

        // Assert
        assertEquals(MetodoPago.PLIN, resultado.metodoPago)
        assertEquals(EstadoPago.APROBADO, resultado.estado)
        coVerify(exactly = 1) { paymentGateway.charge(any()) }
    }

    @Test
    fun `test payCard procesa pago correctamente`() = runBlocking {
        // Arrange
        val req = mapOf(
            "bookingId" to "BK12345678",
            "monto" to 900.0
        )
        val paymentFromGateway = Pago(
            id = "",
            bookingId = "BK12345678",
            monto = 900.0,
            metodoPago = MetodoPago.TARJETA,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )
        
        coEvery { paymentGateway.charge(any()) } returns paymentFromGateway
        every { pagoRepository.save(any()) } answers {
            firstArg<Pago>().copy(id = "PAY12345678")
        }

        // Act
        val resultado = pagoService.payCard(req)

        // Assert
        assertEquals(MetodoPago.TARJETA, resultado.metodoPago)
        assertEquals(EstadoPago.APROBADO, resultado.estado)
        coVerify(exactly = 1) { paymentGateway.charge(any()) }
    }

    @Test
    fun `test payYape usa PaymentGateway para procesar pago`() = runBlocking {
        // Arrange
        val req = mapOf(
            "bookingId" to "BK12345678",
            "monto" to 900.0
        )
        val paymentFromGateway = Pago(
            id = "",
            bookingId = "BK12345678",
            monto = 900.0,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )
        
        coEvery { paymentGateway.charge(any()) } returns paymentFromGateway
        every { pagoRepository.save(any()) } answers {
            firstArg<Pago>().copy(id = "PAY12345678")
        }

        // Act
        pagoService.payYape(req)

        // Assert - Verificar que PaymentGateway.charge fue llamado
        coVerify(exactly = 1) { paymentGateway.charge(any()) }
        verify(exactly = 1) { pagoRepository.save(any()) }
    }
}
