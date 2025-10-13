package com.grupo4.appreservas.controller

import com.grupo4.appreservas.controller.PagoController
import com.grupo4.appreservas.modelos.Booking
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.EstadoBooking
import com.grupo4.appreservas.modelos.EstadoPago
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Payment
import com.grupo4.appreservas.modelos.Voucher
import com.grupo4.appreservas.service.PagoService
import com.grupo4.appreservas.service.ReciboService
import com.grupo4.appreservas.service.ReservasService
import kotlinx.coroutines.runBlocking
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class PagoControllerTest {

    private lateinit var pagoController: PagoController
    private lateinit var pagoService: PagoService
    private lateinit var reservasService: ReservasService
    private lateinit var reciboService: ReciboService

    private val bookingMock = Booking(
        id = "BK12345678",
        userId = "user_123",
        destinoId = "dest_001",
        destino = Destino(
            id = "dest_001",
            nombre = "Tour Test",
            precio = 450.0
        ),
        fecha = Date(),
        horaInicio = "08:00",
        numPersonas = 2,
        precioTotal = 900.0,
        estado = EstadoBooking.PENDIENTE_PAGO
    )

    @Before
    fun setUp() {
        pagoService = mockk()
        reservasService = mockk()
        reciboService = mockk()
        pagoController = PagoController(pagoService, reservasService, reciboService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test pagar con YAPE procesa pago exitosamente`() = runBlocking {
        // Arrange
        val bookingId = "BK12345678"
        val metodo = MetodoPago.YAPE
        val requestData = mapOf(
            "bookingId" to bookingId,
            "monto" to 900.0
        )
        val paymentMock = Payment(
            id = "PAY12345678",
            bookingId = bookingId,
            monto = 900.0,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )

        every { reservasService.obtenerReserva(bookingId) } returns bookingMock
        coEvery { pagoService.payYape(requestData) } returns paymentMock

        // Act
        val resultado = pagoController.pagar(bookingId, metodo)

        // Assert
        assertNotNull(resultado)
        assertEquals(MetodoPago.YAPE, resultado?.metodoPago)
        assertEquals(EstadoPago.APROBADO, resultado?.estado)
        coVerify(exactly = 1) { pagoService.payYape(requestData) }
    }

    @Test
    fun `test pagar con PLIN procesa pago exitosamente`() = runBlocking {
        // Arrange
        val bookingId = "BK12345678"
        val metodo = MetodoPago.PLIN
        val requestData = mapOf(
            "bookingId" to bookingId,
            "monto" to 900.0
        )
        val paymentMock = Payment(
            id = "PAY12345678",
            bookingId = bookingId,
            monto = 900.0,
            metodoPago = MetodoPago.PLIN,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )

        every { reservasService.obtenerReserva(bookingId) } returns bookingMock
        coEvery { pagoService.payPlin(requestData) } returns paymentMock

        // Act
        val resultado = pagoController.pagar(bookingId, metodo)

        // Assert
        assertNotNull(resultado)
        assertEquals(MetodoPago.PLIN, resultado?.metodoPago)
        assertEquals(EstadoPago.APROBADO, resultado?.estado)
    }

    @Test
    fun `test pagar con TARJETA procesa pago exitosamente`() = runBlocking {
        // Arrange
        val bookingId = "BK12345678"
        val metodo = MetodoPago.TARJETA
        val requestData = mapOf(
            "bookingId" to bookingId,
            "monto" to 900.0
        )
        val paymentMock = Payment(
            id = "PAY12345678",
            bookingId = bookingId,
            monto = 900.0,
            metodoPago = MetodoPago.TARJETA,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )

        every { reservasService.obtenerReserva(bookingId) } returns bookingMock
        coEvery { pagoService.payCard(requestData) } returns paymentMock

        // Act
        val resultado = pagoController.pagar(bookingId, metodo)

        // Assert
        assertNotNull(resultado)
        assertEquals(MetodoPago.TARJETA, resultado?.metodoPago)
    }

    @Test
    fun `test pagar devuelve null cuando booking no existe`() = runBlocking {
        // Arrange
        val bookingId = "BK_INEXISTENTE"
        val metodo = MetodoPago.YAPE

        every { reservasService.obtenerReserva(bookingId) } returns null

        // Act
        val resultado = pagoController.pagar(bookingId, metodo)

        // Assert
        assertNull(resultado)
        coVerify(exactly = 0) { pagoService.payYape(any()) }
    }

    @Test
    fun `test process procesa pago y confirma booking exitosamente`() = runBlocking {
        // Arrange
        val bookingId = "BK12345678"
        val metodo = MetodoPago.YAPE
        val paymentMock = Payment(
            id = "PAY12345678",
            bookingId = bookingId,
            monto = 900.0,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO,
            transaccionId = "TXN123456"
        )
        val bookingActualizado = bookingMock.copy(
            estado = EstadoBooking.PAGADA,
            codigoConfirmacion = "PS12345678",
            metodoPago = "YAPE"
        )

        every { reservasService.obtenerReserva(bookingId) } returns bookingMock
        coEvery { pagoService.payYape(any()) } returns paymentMock
        every { reservasService.confirmarPago(bookingId, "YAPE") } returns bookingActualizado

        // Act
        val resultado = pagoController.process(bookingId, metodo)

        // Assert
        assertEquals(true, resultado["success"])
        assertEquals("PAY12345678", resultado["paymentId"])
        assertEquals(bookingId, resultado["bookingId"])
        assertEquals("APROBADO", resultado["estado"])
        verify(exactly = 1) { reservasService.confirmarPago(bookingId, "YAPE") }
    }

    @Test
    fun `test process devuelve error cuando pago falla`() = runBlocking {
        // Arrange
        val bookingId = "BK12345678"
        val metodo = MetodoPago.YAPE

        every { reservasService.obtenerReserva(bookingId) } returns bookingMock
        coEvery { pagoService.payYape(any()) } throws RuntimeException("Fallo en pasarela de pago")

        // Act
        val resultado = pagoController.process(bookingId, metodo)

        // Assert
        assertEquals(false, resultado["success"])
        assertEquals("No se pudo procesar el pago", resultado["error"])
        verify(exactly = 0) { reservasService.confirmarPago(any(), any()) }
    }

    @Test
    fun `test generarComprobante genera voucher exitosamente`() {
        // Arrange
        val bookingId = "BK12345678"
        val voucherMock = Voucher(
            bookingId = bookingId,
            codigoConfirmacion = "PS12345678",
            qrCode = "QR_DATA_PS12345678",
            destinoNombre = "Tour Test",
            fecha = Date(),
            numPersonas = 2,
            montoTotal = 900.0,
            metodoPago = "YAPE",
            horaInicio = "08:00"
        )

        every { reciboService.emitir(bookingId) } returns voucherMock

        // Act
        val resultado = pagoController.generarComprobante(bookingId)

        // Assert
        assertNotNull(resultado)
        assertEquals(voucherMock, resultado?.get("voucher"))
        assertEquals("QR_DATA_PS12345678", resultado?.get("qrCode"))
        verify(exactly = 1) { reciboService.emitir(bookingId) }
    }

    @Test
    fun `test generarComprobante devuelve null cuando no se puede generar`() {
        // Arrange
        val bookingId = "BK_INEXISTENTE"

        every { reciboService.emitir(bookingId) } returns null

        // Act
        val resultado = pagoController.generarComprobante(bookingId)

        // Assert
        assertNull(resultado)
    }
}