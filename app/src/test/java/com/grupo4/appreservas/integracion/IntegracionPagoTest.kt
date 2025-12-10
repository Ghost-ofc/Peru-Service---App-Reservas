package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.controller.PaymentController
import com.grupo4.appreservas.modelos.*
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.service.PaymentGateway
import com.grupo4.appreservas.service.PaymentService
import com.grupo4.appreservas.service.VoucherService
import io.mockk.*
import io.mockk.coEvery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Pruebas de integración para HU-003: Pagar mi reserva mediante Yape, Plin o tarjeta y recibir un comprobante digital con código QR
 * 
 * Escenarios a probar:
 * 1. Selección del método de pago: El turista está en la interfaz de resumen y pago, elige Yape, Plin 
 *    o tarjeta como método de pago, se muestra la pasarela correspondiente
 * 2. Pago exitoso: El turista completa el pago, presiona el botón de Pagar, lo redirige a la pantalla 
 *    de reserva confirmada en donde se muestra el resumen de lo que ha reservado y un código QR junto 
 *    con un botón de volver a inicio
 * 3. Pago fallido: La transacción no se completa, la pasarela devuelve error, la app notifica el error
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionPagoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var repository: PeruvianServiceRepository
    private lateinit var paymentController: PaymentController
    private lateinit var paymentService: PaymentService
    private lateinit var voucherService: VoucherService
    private lateinit var paymentGateway: PaymentGateway

    private val testDispatcher = StandardTestDispatcher()
    private val bookingId = "RES_1234567890"
    private val monto = 900.0
    private val usuarioId = 1

    private val reservaMock = Reserva(
        reservaId = bookingId,
        userId = usuarioId.toString(),
        usuarioId = usuarioId,
        destinoId = "dest_001",
        tourId = "tour_001",
        tourSlotId = "dest_001_2024-12-15",
        nombreTurista = "Juan Pérez",
        documento = "12345678",
        destino = Destino(
            id = "dest_001",
            nombre = "Tour Machu Picchu Clásico",
            ubicacion = "Cusco, Perú",
            descripcion = "Descubre la majestuosa ciudadela inca",
            precio = 450.0,
            duracionHoras = 12,
            maxPersonas = 15,
            categorias = listOf("Cultura"),
            imagenUrl = "https://example.com/machu.jpg",
            calificacion = 4.8,
            numReseñas = 124,
            disponibleTodosDias = true
        ),
        fecha = Date(),
        horaInicio = "09:00",
        numPersonas = 2,
        precioTotal = monto,
        estado = EstadoReserva.PENDIENTE,
        codigoConfirmacion = "CONF_123456",
        codigoQR = "QR_123456"
    )

    @Before
    fun setUp() {
        // Mock del Looper principal
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)

        // Forzar a ArchTaskExecutor a ejecutar todo en el mismo hilo
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) { runnable.run() }
            override fun postToMainThread(runnable: Runnable) { runnable.run() }
            override fun isMainThread(): Boolean = true
        })

        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper
        mockkConstructor(DatabaseHelper::class)
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(bookingId) } returns reservaMock
        every { anyConstructed<DatabaseHelper>().insertarReserva(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().insertarPago(any()) } returns 1L

        repository = PeruvianServiceRepository.getInstance(context)

        // Mock PaymentGateway
        paymentGateway = mockk(relaxed = true)
        
        // Mock PaymentService
        paymentService = PaymentService(repository, paymentGateway)
        
        // Mock VoucherService
        voucherService = mockk(relaxed = true)
        
        paymentController = PaymentController(paymentService, voucherService)
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        unmockkStatic(Looper::class)
        Dispatchers.resetMain()
        clearAllMocks()
        val field = PeruvianServiceRepository::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun `test HU-003 Escenario 1 - Procesar pago con Yape exitosamente`() = runTest(testDispatcher) {
        // Arrange: Configurar pago exitoso con Yape
        val pagoExitoso = Pago(
            id = "PAY_123456",
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO,
            fecha = Date(),
            transaccionId = "TXN_YAPE_123"
        )

        coEvery { paymentGateway.charge(any()) } returns pagoExitoso

        // Act: Procesar pago con Yape
        val pago = paymentController.pagar(bookingId, MetodoPago.YAPE, monto)
        advanceUntilIdle()

        // Assert: Verificar que se procesó el pago
        assertNotNull(pago)
        pago?.let { p ->
            assertEquals(bookingId, p.bookingId)
            assertEquals(monto, p.monto, 0.01)
            assertEquals(MetodoPago.YAPE, p.metodoPago)
            assertEquals(EstadoPago.APROBADO, p.estado)
            assertNotNull(p.id)
            assertTrue(p.id.isNotEmpty())
        }

        // Verificar que se guardó el pago
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarPago(any()) }
    }

    @Test
    fun `test HU-003 Escenario 1 - Procesar pago con Plin exitosamente`() = runTest(testDispatcher) {
        // Arrange: Configurar pago exitoso con Plin
        val pagoExitoso = Pago(
            id = "PAY_123456",
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.PLIN,
            estado = EstadoPago.APROBADO,
            fecha = Date(),
            transaccionId = "TXN_PLIN_123"
        )

        coEvery { paymentGateway.charge(any()) } returns pagoExitoso

        // Act: Procesar pago con Plin
        val pago = paymentController.pagar(bookingId, MetodoPago.PLIN, monto)
        advanceUntilIdle()

        // Assert: Verificar que se procesó el pago
        assertNotNull(pago)
        pago?.let { p ->
            assertEquals(MetodoPago.PLIN, p.metodoPago)
            assertEquals(EstadoPago.APROBADO, p.estado)
        }

        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarPago(any()) }
    }

    @Test
    fun `test HU-003 Escenario 1 - Procesar pago con Tarjeta exitosamente`() = runTest(testDispatcher) {
        // Arrange: Configurar pago exitoso con Tarjeta
        val pagoExitoso = Pago(
            id = "PAY_123456",
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.TARJETA,
            estado = EstadoPago.APROBADO,
            fecha = Date(),
            transaccionId = "TXN_CARD_123"
        )

        coEvery { paymentGateway.charge(any()) } returns pagoExitoso

        // Act: Procesar pago con Tarjeta
        val pago = paymentController.pagar(bookingId, MetodoPago.TARJETA, monto)
        advanceUntilIdle()

        // Assert: Verificar que se procesó el pago
        assertNotNull(pago)
        pago?.let { p ->
            assertEquals(MetodoPago.TARJETA, p.metodoPago)
            assertEquals(EstadoPago.APROBADO, p.estado)
        }

        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarPago(any()) }
    }

    @Test
    fun `test HU-003 Escenario 2 - Generar comprobante con código QR después de pago exitoso`() = runTest(testDispatcher) {
        // Arrange: Configurar pago exitoso y comprobante
        val pagoExitoso = Pago(
            id = "PAY_123456",
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO,
            fecha = Date()
        )

        val reciboMock = Recibo(
            bookingId = bookingId,
            codigoConfirmacion = reservaMock.codigoConfirmacion,
            qrCode = reservaMock.codigoQR,
            destinoNombre = reservaMock.destino?.nombre ?: "",
            fecha = reservaMock.fecha,
            numPersonas = reservaMock.numPersonas,
            montoTotal = reservaMock.precioTotal,
            metodoPago = "Yape",
            horaInicio = reservaMock.horaInicio
        )

        coEvery { paymentGateway.charge(any()) } returns pagoExitoso
        every { voucherService.emitir(bookingId) } returns reciboMock

        // Act: Procesar pago y generar comprobante
        val pago = paymentController.pagar(bookingId, MetodoPago.YAPE, monto)
        advanceUntilIdle()

        val recibo = paymentController.generarComprobante(bookingId)
        advanceUntilIdle()

        // Assert: Verificar que se generó el comprobante
        assertNotNull(recibo)
        recibo?.let { r ->
            assertEquals(bookingId, r.bookingId)
            assertEquals(reservaMock.codigoConfirmacion, r.codigoConfirmacion)
            assertNotNull(r.qrCode)
            assertTrue(r.qrCode.isNotEmpty())
            assertEquals(reservaMock.destino?.nombre, r.destinoNombre)
            assertEquals(reservaMock.numPersonas, r.numPersonas)
            assertEquals(reservaMock.precioTotal, r.montoTotal, 0.01)
            assertEquals("Yape", r.metodoPago)
            assertEquals(reservaMock.horaInicio, r.horaInicio)
        }

        verify(exactly = 1) { voucherService.emitir(bookingId) }
    }

    @Test
    fun `test HU-003 Escenario 3 - Pago fallido retorna pago con estado RECHAZADO`() = runTest(testDispatcher) {
        // Arrange: Configurar pago fallido
        val pagoFallido = Pago(
            id = "PAY_123456",
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.RECHAZADO,
            fecha = Date(),
            transaccionId = ""
        )

        coEvery { paymentGateway.charge(any()) } returns pagoFallido

        // Act: Procesar pago (que fallará)
        val pago = paymentController.pagar(bookingId, MetodoPago.YAPE, monto)
        advanceUntilIdle()

        // Assert: Verificar que el pago tiene estado RECHAZADO
        assertNotNull(pago)
        pago?.let { p ->
            assertEquals(EstadoPago.RECHAZADO, p.estado)
            assertEquals(bookingId, p.bookingId)
        }

        // Verificar que se guardó el pago (aunque haya fallado)
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarPago(any()) }
    }

    @Test
    fun `test confirmar pago actualiza estado de reserva`() = runTest(testDispatcher) {
        // Arrange: Configurar reserva pendiente y pago
        val reservaPendiente = reservaMock.copy(estado = EstadoReserva.PENDIENTE)
        val pagoExitoso = Pago(
            id = "PAY_123456",
            bookingId = bookingId,
            monto = monto,
            metodoPago = MetodoPago.YAPE,
            estado = EstadoPago.APROBADO,
            fecha = Date()
        )
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(bookingId) } returns reservaPendiente

        // Act: Confirmar pago
        val reservaActualizada = repository.confirmarPago(bookingId, pagoExitoso)
        advanceUntilIdle()

        // Assert: Verificar que se actualizó la reserva
        assertNotNull(reservaActualizada)
        assertEquals(bookingId, reservaActualizada?.reservaId)
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().insertarReserva(any()) }
    }

    @Test
    fun `test comprobante contiene toda la información requerida`() = runTest(testDispatcher) {
        // Arrange: Configurar comprobante
        val reciboMock = Recibo(
            bookingId = bookingId,
            codigoConfirmacion = "CONF_123456",
            qrCode = "QR_123456",
            destinoNombre = "Tour Machu Picchu Clásico",
            fecha = Date(),
            numPersonas = 2,
            montoTotal = 900.0,
            metodoPago = "Yape",
            horaInicio = "09:00"
        )

        every { voucherService.emitir(bookingId) } returns reciboMock

        // Act: Generar comprobante
        val recibo = paymentController.generarComprobante(bookingId)
        advanceUntilIdle()

        // Assert: Verificar que el comprobante contiene toda la información
        assertNotNull(recibo)
        recibo?.let { r ->
            assertNotNull("Debe tener bookingId", r.bookingId)
            assertNotNull("Debe tener código de confirmación", r.codigoConfirmacion)
            assertNotNull("Debe tener código QR", r.qrCode)
            assertTrue("Debe tener código QR no vacío", r.qrCode.isNotEmpty())
            assertNotNull("Debe tener nombre del destino", r.destinoNombre)
            assertNotNull("Debe tener fecha", r.fecha)
            assertTrue("Debe tener número de personas mayor a 0", r.numPersonas > 0)
            assertTrue("Debe tener monto total mayor a 0", r.montoTotal > 0)
            assertNotNull("Debe tener método de pago", r.metodoPago)
            assertNotNull("Debe tener hora de inicio", r.horaInicio)
        }
    }
}

