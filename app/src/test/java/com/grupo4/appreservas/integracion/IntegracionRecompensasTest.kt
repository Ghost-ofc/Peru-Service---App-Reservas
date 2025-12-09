package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.*
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.RecompensasViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
 * Pruebas de integración para HU-007: Sistema de Recompensas y Logros
 * 
 * Escenarios a probar:
 * 1. Acumulación de puntos: El turista completa una reserva, se suman puntos automáticamente
 * 2. Visualización de logros: El turista accede a su perfil, se muestran puntos y logros desbloqueados
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionRecompensasTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var viewModel: RecompensasViewModel
    private lateinit var repository: PeruvianServiceRepository

    private val testDispatcher = StandardTestDispatcher()
    private val usuarioId = 1
    private val reservaId = "BK12345678"
    private val destinoId = "dest_001"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper - IMPORTANTE: configurar mocks ANTES de instanciar el repositorio
        mockkConstructor(DatabaseHelper::class)
        // Mockear métodos básicos para evitar errores de base de datos
        // Estos mocks por defecto se pueden sobrescribir en cada test
        every { anyConstructed<DatabaseHelper>().obtenerPuntos(any()) } returns 0
        every { anyConstructed<DatabaseHelper>().inicializarPuntos(any()) } just Runs
        every { anyConstructed<DatabaseHelper>().sumarPuntos(any(), any()) } returns true
        every { anyConstructed<DatabaseHelper>().obtenerLogros(any()) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(any(), any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(any()) } returns emptyList()

        // Instanciar repositorio DESPUÉS de configurar todos los mocks
        repository = PeruvianServiceRepository.getInstance(context)
        // El ViewModel se instanciará en cada test después de configurar los mocks específicos
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
        // Resetear la instancia singleton del repositorio para cada test
        val field = PeruvianServiceRepository::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun `test HU-007 Escenario 1 - Puntos se suman automáticamente al completar reserva`() = runTest {
        // Arrange: Crear una reserva confirmada
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = usuarioId.toString(),
            usuarioId = usuarioId,
            destinoId = destinoId,
            destino = Destino(id = destinoId, nombre = "Machu Picchu", precio = 450.0),
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            nombreTurista = "Usuario Test",
            documento = "test@example.com"
        )

        // Mock: Puntos iniciales
        var puntosSimulados = 0
        every { anyConstructed<DatabaseHelper>().obtenerPuntos(usuarioId) } answers { puntosSimulados }
        every { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) } just Runs
        every { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, PuntosUsuario.PUNTOS_POR_RESERVA) } answers {
            puntosSimulados += PuntosUsuario.PUNTOS_POR_RESERVA
            true
        }
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns listOf(reserva)
        // Mock para verificarYDesbloquearLogros
        every { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().existeLogro(usuarioId, any()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) } returns 1L

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = RecompensasViewModel(application)

        // Observador
        val puntosObserver = mockk<Observer<Int>>(relaxed = true)
        viewModel.puntos.observeForever(puntosObserver)

        // Act: Sumar puntos por reserva
        repository.sumarPuntosPorReserva(usuarioId, reservaId)

        // Assert: Verificar que se sumaron los puntos
        // inicializarPuntos se llama desde obtenerPuntos (que se llama en verificarYDesbloquearLogros)
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) }
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, PuntosUsuario.PUNTOS_POR_RESERVA) }
        
        // Verificar puntos (esto también llama a inicializarPuntos)
        val puntos = repository.obtenerPuntos(usuarioId)
        assertEquals(PuntosUsuario.PUNTOS_POR_RESERVA, puntos)
    }

    @Test
    fun `test HU-007 Escenario 2 - Logro Primer Viaje se desbloquea al completar primera reserva`() = runTest {
        // Arrange: Usuario con una reserva confirmada
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = usuarioId.toString(),
            usuarioId = usuarioId,
            destinoId = destinoId,
            destino = Destino(id = destinoId, nombre = "Machu Picchu", precio = 450.0),
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            nombreTurista = "Usuario Test",
            documento = "test@example.com"
        )

        // Mock: Puntos iniciales
        var puntosSimulados = PuntosUsuario.PUNTOS_POR_RESERVA
        every { anyConstructed<DatabaseHelper>().obtenerPuntos(usuarioId) } answers { puntosSimulados }
        every { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) } just Runs
        every { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, any()) } answers {
            puntosSimulados += PuntosUsuario.PUNTOS_POR_RESERVA
            true
        }
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns listOf(reserva)

        // Mock: Logro Primer Viaje
        var logroPrimerViaje: Logro? = null
        every { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) } answers {
            if (logroPrimerViaje != null) {
                listOf(logroPrimerViaje!!)
            } else {
                emptyList()
            }
        }
        every { anyConstructed<DatabaseHelper>().existeLogro(usuarioId, any()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) } answers {
            val logro = secondArg<Logro>()
            if (logro.tipo == TipoLogro.PRIMER_VIAJE) {
                logroPrimerViaje = logro
            }
            1L
        }

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = RecompensasViewModel(application)

        // Observador
        val logrosObserver = mockk<Observer<List<Logro>>>(relaxed = true)
        viewModel.logros.observeForever(logrosObserver)

        // Act: Actualizar puntos (esto debería desbloquear el logro)
        viewModel.actualizarPuntos(usuarioId, reservaId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se creó el logro
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) }
        
        // Verificar que el logro se desbloqueó
        assertNotNull("El logro debe existir", logroPrimerViaje)
        assertEquals("Debe ser logro de primer viaje", TipoLogro.PRIMER_VIAJE, logroPrimerViaje?.tipo)
        assertTrue("El logro debe estar desbloqueado", logroPrimerViaje?.desbloqueado == true)
    }

    @Test
    fun `test puntos acumulados se muestran correctamente en el perfil`() = runTest {
        // Arrange: Usuario con puntos acumulados
        val puntosAcumulados = 600
        every { anyConstructed<DatabaseHelper>().obtenerPuntos(usuarioId) } returns puntosAcumulados
        every { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) } just Runs
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) } returns emptyList()
        every { anyConstructed<DatabaseHelper>().existeLogro(usuarioId, any()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) } returns 1L

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = RecompensasViewModel(application)

        // Observador
        val puntosObserver = mockk<Observer<Int>>(relaxed = true)
        viewModel.puntos.observeForever(puntosObserver)

        // Act: Cargar puntos
        viewModel.cargarPuntos(usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtienen los puntos correctamente
        val puntos = repository.obtenerPuntos(usuarioId)
        assertEquals(puntosAcumulados, puntos)
        
        // Verificar nivel
        val nivel = PuntosUsuario.calcularNivel(puntos)
        assertEquals("Explorador Experto", nivel) // 600 puntos = Explorador Experto
        
        // Verificar que se actualizó el LiveData
        verify { puntosObserver.onChanged(any()) }
    }

    @Test
    fun `test logro de 5 tours se desbloquea al completar 5 reservas`() = runTest {
        // Arrange: Usuario con 5 reservas confirmadas
        val reservas = (1..5).map { i ->
            Reserva(
                id = "BK$i",
                reservaId = "BK$i",
                userId = usuarioId.toString(),
                usuarioId = usuarioId,
                destinoId = destinoId,
                destino = Destino(id = destinoId, nombre = "Tour $i", precio = 100.0),
                fecha = Date(),
                horaInicio = "08:00",
                numPersonas = 1,
                precioTotal = 100.0,
                estado = EstadoReserva.CONFIRMADO,
                nombreTurista = "Usuario Test",
                documento = "test@example.com"
            )
        }

        // Mock: 5 reservas confirmadas
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns reservas
        every { anyConstructed<DatabaseHelper>().obtenerPuntos(usuarioId) } returns 1000
        every { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) } just Runs

        // Mock: Logros
        var logro5Tours: Logro? = null
        every { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) } answers {
            if (logro5Tours != null) {
                listOf(logro5Tours!!)
            } else {
                emptyList()
            }
        }
        every { anyConstructed<DatabaseHelper>().existeLogro(usuarioId, any()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) } answers {
            val logro = secondArg<Logro>()
            if (logro.tipo == TipoLogro.VIAJERO_FRECUENTE) {
                logro5Tours = logro
            }
            1L
        }

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = RecompensasViewModel(application)

        // Observador
        val logrosObserver = mockk<Observer<List<Logro>>>(relaxed = true)
        viewModel.logros.observeForever(logrosObserver)

        // Act: Actualizar puntos (esto debería desbloquear el logro)
        viewModel.actualizarPuntos(usuarioId, reservas.first().reservaId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se desbloqueó el logro de 5 tours
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) }
    }

    @Test
    fun `test nivel del usuario se calcula correctamente basado en puntos`() {
        // Arrange: Diferentes cantidades de puntos
        val casos = listOf(
            0 to "Explorador",
            250 to "Explorador",
            500 to "Explorador",
            501 to "Explorador Experto",
            1000 to "Explorador Experto",
            1500 to "Explorador Experto",
            1501 to "Viajero Profesional",
            2000 to "Viajero Profesional",
            3000 to "Viajero Profesional",
            3001 to "Maestro Viajero",
            5000 to "Maestro Viajero"
        )

        casos.forEach { (puntos, nivelEsperado) ->
            // Act: Calcular nivel
            val nivel = PuntosUsuario.calcularNivel(puntos)

            // Assert: Verificar que el nivel es correcto
            assertEquals("Con $puntos puntos, el nivel debe ser $nivelEsperado", nivelEsperado, nivel)
        }
    }

    @Test
    fun `test puntos para siguiente nivel se calculan correctamente`() {
        // Arrange: Diferentes cantidades de puntos
        val casos = listOf(
            0 to 501,      // Explorador -> Explorador Experto (501 puntos)
            250 to 251,    // Explorador -> Explorador Experto (501 - 250 = 251)
            500 to 1,      // Explorador -> Explorador Experto (501 - 500 = 1)
            501 to 1000,   // Explorador Experto -> Viajero Profesional (1501 - 501 = 1000)
            1000 to 501,   // Explorador Experto -> Viajero Profesional (1501 - 1000 = 501)
            1500 to 1,     // Explorador Experto -> Viajero Profesional (1501 - 1500 = 1)
            1501 to 1500,  // Viajero Profesional -> Maestro Viajero (3001 - 1501 = 1500)
            3000 to 1,     // Viajero Profesional -> Maestro Viajero (3001 - 3000 = 1)
            3001 to 0,     // Maestro Viajero -> Nivel máximo (0 puntos restantes)
            5000 to 0      // Maestro Viajero -> Nivel máximo (0 puntos restantes)
        )

        casos.forEach { (puntos, puntosEsperados) ->
            // Act: Calcular puntos para siguiente nivel
            val puntosParaSiguiente = PuntosUsuario.calcularPuntosParaSiguienteNivel(puntos)

            // Assert: Verificar que los puntos son correctos
            assertEquals(
                "Con $puntos puntos, faltan $puntosEsperados para el siguiente nivel",
                puntosEsperados,
                puntosParaSiguiente
            )
        }
    }

    @Test
    fun `test flujo completo desde reserva hasta desbloqueo de logro`() = runTest {
        // Arrange: Usuario nuevo sin puntos ni logros
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = usuarioId.toString(),
            usuarioId = usuarioId,
            destinoId = destinoId,
            destino = Destino(id = destinoId, nombre = "Machu Picchu", precio = 450.0),
            fecha = Date(),
            horaInicio = "08:00",
            numPersonas = 2,
            precioTotal = 900.0,
            estado = EstadoReserva.CONFIRMADO,
            nombreTurista = "Usuario Test",
            documento = "test@example.com"
        )

        // Mock: Estado inicial (sin puntos, sin reservas)
        var puntosSimulados = 0
        every { anyConstructed<DatabaseHelper>().obtenerPuntos(usuarioId) } answers { puntosSimulados }
        every { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) } just Runs
        every { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, PuntosUsuario.PUNTOS_POR_RESERVA) } answers {
            puntosSimulados += PuntosUsuario.PUNTOS_POR_RESERVA
            true
        }
        every { anyConstructed<DatabaseHelper>().obtenerReservaPorId(reservaId) } returns reserva
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns listOf(reserva)

        // Mock: Logros (inicialmente no existen, luego se crean)
        var logrosCreados = mutableListOf<Logro>()
        every { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) } answers { logrosCreados.toList() }
        every { anyConstructed<DatabaseHelper>().existeLogro(usuarioId, any()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) } answers {
            val logro = secondArg<Logro>()
            logrosCreados.add(logro)
            1L
        }

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = RecompensasViewModel(application)

        // Observadores
        val puntosObserver = mockk<Observer<Int>>(relaxed = true)
        val logrosObserver = mockk<Observer<List<Logro>>>(relaxed = true)
        viewModel.puntos.observeForever(puntosObserver)
        viewModel.logros.observeForever(logrosObserver)

        // Act: Actualizar puntos (simula confirmación de pago)
        viewModel.actualizarPuntos(usuarioId, reservaId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se sumaron los puntos
        // Nota: El mock retorna puntosSimulados que se actualiza cuando se llama a sumarPuntos
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) }
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, PuntosUsuario.PUNTOS_POR_RESERVA) }
        
        // Verificar puntos después de la suma
        val puntos = repository.obtenerPuntos(usuarioId)
        assertEquals(PuntosUsuario.PUNTOS_POR_RESERVA, puntos)

        // Verificar que se crearon los logros
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) }

        // Verificar que se puede obtener la información de puntos del usuario
        val nivel = PuntosUsuario.calcularNivel(puntos)
        assertEquals("Explorador", nivel)
    }

    @Test
    fun `test logros se generan automáticamente al cargar perfil por primera vez`() = runTest {
        // Arrange: Usuario sin logros
        every { anyConstructed<DatabaseHelper>().obtenerPuntos(usuarioId) } returns 0
        every { anyConstructed<DatabaseHelper>().inicializarPuntos(usuarioId) } just Runs
        every { anyConstructed<DatabaseHelper>().obtenerReservasPorUsuario(usuarioId) } returns emptyList()
        // Primera llamada retorna lista vacía, luego retorna los logros creados
        var logrosCreados = mutableListOf<Logro>()
        every { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) } answers { 
            logrosCreados.toList() 
        }
        every { anyConstructed<DatabaseHelper>().existeLogro(usuarioId, any()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) } answers {
            val logro = secondArg<Logro>()
            logrosCreados.add(logro)
            1L
        }

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = RecompensasViewModel(application)

        // Observador
        val logrosObserver = mockk<Observer<List<Logro>>>(relaxed = true)
        viewModel.logros.observeForever(logrosObserver)

        // Act: Cargar logros (primera vez)
        viewModel.cargarLogros(usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtuvieron los logros
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) }
        // Verificar que se crearon los logros base (inicializarLogrosBase crea 5 logros)
        verify(atLeast = 5) { anyConstructed<DatabaseHelper>().insertarLogroParaUsuario(usuarioId, any()) }
    }

    @Test
    fun `test obtener logros devuelve lista de logros del usuario`() = runTest {
        // Arrange: Logros existentes
        val logros = listOf(
            Logro(
                id = "${usuarioId}_PRIMER_VIAJE",
                nombre = "Primer Viaje",
                descripcion = "Completa tu primera reserva",
                icono = "ic_trophy",
                tipo = TipoLogro.PRIMER_VIAJE,
                criterio = CriterioLogro(TipoCriterio.PRIMERA_RESERVA, 1),
                fechaDesbloqueo = Date(),
                desbloqueado = true
            ),
            Logro(
                id = "${usuarioId}_VIAJERO_FRECUENTE",
                nombre = "Viajero Frecuente",
                descripcion = "Completa 5 reservas",
                icono = "ic_trophy",
                tipo = TipoLogro.VIAJERO_FRECUENTE,
                criterio = CriterioLogro(TipoCriterio.TOURS_COMPLETADOS, 5),
                fechaDesbloqueo = null,
                desbloqueado = false
            )
        )

        // Mock: Obtener logros
        every { anyConstructed<DatabaseHelper>().obtenerLogros(usuarioId) } returns logros

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = RecompensasViewModel(application)

        // Observador
        val logrosObserver = mockk<Observer<List<Logro>>>(relaxed = true)
        viewModel.logros.observeForever(logrosObserver)

        // Act: Cargar logros
        viewModel.cargarLogros(usuarioId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se obtuvieron los logros
        val logrosObtenidos = repository.obtenerLogros(usuarioId)
        assertEquals(2, logrosObtenidos.size)
        assertTrue(logrosObtenidos.any { it.tipo == TipoLogro.PRIMER_VIAJE && it.desbloqueado })
        assertTrue(logrosObtenidos.any { it.tipo == TipoLogro.VIAJERO_FRECUENTE && !it.desbloqueado })
    }
}
