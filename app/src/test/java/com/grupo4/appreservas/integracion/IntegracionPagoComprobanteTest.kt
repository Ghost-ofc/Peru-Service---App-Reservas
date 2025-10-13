package com.grupo4.appreservas.integracion

import com.grupo4.appreservas.controller.PagoController
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.modelos.Recibo
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
        val reserva = Reserva(
            id = bookingId,
            userId = "user_123",
            destinoId = "dest_001",
            destino = Destino(id = "dest_001", nombre = "Tour Test", precio = 450.0),
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.PENDIENTE_PAGO
        )
        val bookingPagado = reserva.copy(
            estado = EstadoReserva.PAGADA,
            codigoConfirmacion = "PS12345678",
            metodoPago = "YAPE"
        )

        every { reservasRepository.find(bookingId) } returns reserva andThen bookingPagado
        every { pagoRepository.save(any()) } answers {
            firstArg<Pago>().copy(
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

        val recibo = comprobante?.get("voucher") as Recibo
        assertEquals("PS12345678", recibo.codigoConfirmacion)
        assertEquals(900.0, recibo.montoTotal, 0.01)
    }
}