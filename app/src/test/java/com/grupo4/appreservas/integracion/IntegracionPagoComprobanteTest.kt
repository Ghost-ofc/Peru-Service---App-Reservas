package com.grupo4.appreservas.integracion

import android.content.Context
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
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.PagoService
import com.grupo4.appreservas.service.PaymentGateway
import com.grupo4.appreservas.service.QRService
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
    private lateinit var context: Context

    @Before
    fun setUp() {
        pagoRepository = mockk()
        reservasRepository = mockk()
        destinoRepository = mockk()
        context = mockk(relaxed = true)

        // Mock DatabaseHelper constructor para que no falle cuando se crea
        mockkConstructor(DatabaseHelper::class)
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(any()) } answers {
            val userId = firstArg<Int>()
            if (userId == 1 || userId > 0) {
                Usuario(
                    usuarioId = userId,
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

        val paymentGateway = mockk<PaymentGateway>()
        coEvery { paymentGateway.charge(any()) } answers {
            val req = firstArg<Map<String, Any>>()
            Pago(
                id = "",
                bookingId = req["bookingId"] as String,
                monto = req["monto"] as Double,
                metodoPago = req["metodoPago"] as com.grupo4.appreservas.modelos.MetodoPago,
                estado = com.grupo4.appreservas.modelos.EstadoPago.APROBADO,
                transaccionId = "TXN123456"
            )
        }
        
        pagoService = PagoService(pagoRepository, reservasRepository, paymentGateway)
        val availabilityService = mockk<AvailabilityService>()
        reservasService = ReservasService(reservasRepository, destinoRepository, availabilityService, context)
        
        val qrService = mockk<QRService>()
        every { qrService.generate(any()) } answers { "QR_CODE_BASE64_${firstArg<String>()}" }
        reciboService = ReciboService(reservasRepository, qrService)

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
            userId = "1",
            usuarioId = 1,
            destinoId = "dest_001",
            destino = Destino(id = "dest_001", nombre = "Tour Test", precio = 450.0),
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.PENDIENTE,
            nombreTurista = "Usuario Test",
            documento = "test@example.com"
        )
        val bookingPagado = reserva.copy(
            estado = EstadoReserva.CONFIRMADO,
            codigoConfirmacion = "QR12345678",
            codigoQR = "QR12345678",
            metodoPago = "YAPE"
        )

        every { reservasRepository.find(bookingId) } returns reserva andThen bookingPagado
        every { pagoRepository.save(any()) } answers {
            firstArg<Pago>().copy(
                id = "PAY12345678"
            )
        }
        every { reservasRepository.save(any()) } answers { firstArg() }
        coEvery { pagoService.payYape(any()) } answers {
            val req = firstArg<Map<String, Any>>()
            Pago(
                id = "PAY12345678",
                bookingId = req["bookingId"] as String,
                monto = req["monto"] as Double,
                metodoPago = com.grupo4.appreservas.modelos.MetodoPago.YAPE,
                estado = com.grupo4.appreservas.modelos.EstadoPago.APROBADO,
                transaccionId = "TXN123456"
            )
        }
        every { reservasService.confirmarPago(bookingId, "YAPE") } returns bookingPagado

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
        assertTrue(recibo.codigoConfirmacion.isNotEmpty())
        assertTrue(recibo.codigoConfirmacion.startsWith("QR"))
        assertEquals(900.0, recibo.montoTotal, 0.01)
    }
}