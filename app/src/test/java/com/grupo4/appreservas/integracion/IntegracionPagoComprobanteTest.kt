package com.grupo4.appreservas.integracion

import com.grupo4.appreservas.controller.PagoController
import com.grupo4.appreservas.modelos.Booking
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.EstadoBooking
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Payment
import com.grupo4.appreservas.modelos.Voucher
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.PagoRepository
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.PagoService
import com.grupo4.appreservas.service.ReciboService
import com.grupo4.appreservas.service.ReservasService
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class IntegracionPagoComprobanteTest {

    private lateinit var pagoController: PagoController
    private lateinit var pagoService: PagoService
    private lateinit var reservasService: ReservasService
    private lateinit var reciboService: ReciboService
    private lateinit var pagoRepository: PagoRepository
    private lateinit var reservasRepository: ReservasRepository
    private lateinit var destinoRepository: DestinoRepository

    @Before
    fun setUp() {
        pagoRepository = mockk()
        reservasRepository = mockk()
        destinoRepository = mockk()

        pagoService = PagoService(pagoRepository, reservasRepository)
        val availabilityService = mockk<AvailabilityService>()
        reservasService = ReservasService(reservasRepository, destinoRepository, availabilityService)
        reciboService = ReciboService(reservasRepository)

        pagoController = PagoController(pagoService, reservasService, reciboService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test flujo completo de pago hasta generación de comprobante`() = runBlocking {
        // Arrange
        val bookingId = "BK12345678"
        val booking = Booking(
            id = bookingId,
            userId = "user_123",
            destinoId = "dest_001",
            destino = Destino(id = "dest_001", nombre = "Tour Test", precio = 450.0),
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoBooking.PENDIENTE_PAGO
        )
        val bookingPagado = booking.copy(
            estado = EstadoBooking.PAGADA,
            codigoConfirmacion = "PS12345678",
            metodoPago = "YAPE"
        )

        every { reservasRepository.find(bookingId) } returns booking andThen bookingPagado
        every { pagoRepository.save(any()) } answers {
            firstArg<Payment>().copy(
                id = "PAY12345678",
                transaccionId = "TXN123456"
            )
        }
        every { reservasRepository.save(any()) } answers { firstArg() }

        // Act
        // 1. Procesar pago
        val resultadoPago = pagoController.process(bookingId, MetodoPago.YAPE)

        // 2. Generar comprobante
        val comprobante = pagoController.generarComprobante(bookingId)

        // Assert
        assertEquals(true, resultadoPago["success"])
        assertNotNull(comprobante)
        assertNotNull(comprobante?.get("voucher"))
        assertNotNull(comprobante?.get("qrCode"))

        val voucher = comprobante?.get("voucher") as Voucher
        assertEquals("PS12345678", voucher.codigoConfirmacion)
        assertEquals(900.0, voucher.montoTotal, 0.01)
    }
}