package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.EncuestaRespuesta
import com.grupo4.appreservas.modelos.TipoNotificacion
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.EncuestaViewModel
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
 * Pruebas de integración para HU-009: Encuestas de Satisfacción
 * 
 * Escenarios a probar:
 * 1. Envío automático de encuesta: El tour ha finalizado, el sistema envía encuesta al turista,
 *    el turista recibe notificación con enlace o formulario
 * 2. Registro de respuesta: El turista completa la encuesta, envía su calificación y comentario,
 *    la app registra la respuesta y genera una métrica de satisfacción
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionEncuestaTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var viewModel: EncuestaViewModel
    private lateinit var repository: PeruvianServiceRepository

    private val testDispatcher = StandardTestDispatcher()
    private val tourId = "dest_001_2024-11-20"
    private val usuarioId = 1

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper
        mockkConstructor(DatabaseHelper::class)
        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(any(), any()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().obtenerCalificacionPromedioTour(any()) } returns 0.0
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().sumarPuntos(any(), any()) } returns true
        every { anyConstructed<DatabaseHelper>().obtenerTourPorId(any()) } returns null

        // Instanciar repositorio DESPUÉS de configurar todos los mocks
        repository = PeruvianServiceRepository.getInstance(context)
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
    fun `test HU-009 Escenario 1 - Envío automático de encuesta después de finalizar tour`() = runTest {
        // Arrange: Tour finalizado
        val tour = com.grupo4.appreservas.modelos.Tour(
            tourId = tourId,
            nombre = "Tour a Machu Picchu",
            fecha = "2024-11-19", // Tour de ayer (ya finalizó)
            hora = "09:00",
            puntoEncuentro = "Cusco",
            capacidad = 15,
            participantesConfirmados = 10,
            estado = "Completado"
        )

        // Mock: Tour existe y usuario no ha respondido
        every { anyConstructed<DatabaseHelper>().obtenerTourPorId(tourId) } returns tour
        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(tourId, usuarioId.toString()) } returns false
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()

        // Act: Enviar encuesta automática
        val exito = repository.enviarEncuestaAutomatica(tourId, usuarioId)

        // Assert: Verificar que se creó la notificación
        assertTrue("La encuesta debe enviarse correctamente", exito)
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) }
        
        // Verificar que la notificación es de tipo ENCUESTA_SATISFACCION
        val capturaNotificacion = slot<com.grupo4.appreservas.modelos.Notificacion>()
        verify { anyConstructed<DatabaseHelper>().insertarNotificacion(capture(capturaNotificacion)) }
        assertEquals(TipoNotificacion.ENCUESTA_SATISFACCION, capturaNotificacion.captured.tipo)
        assertEquals(tourId, capturaNotificacion.captured.tourId)
        assertEquals(usuarioId, capturaNotificacion.captured.usuarioId)
    }

    @Test
    fun `test HU-009 Escenario 2 - Registro de respuesta de encuesta`() = runTest {
        // Arrange: Usuario completa la encuesta
        val calificacion = 4
        val comentario = "Excelente experiencia, muy recomendado"

        // Mock: Usuario no ha respondido aún
        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(tourId, usuarioId.toString()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, 50) } returns true

        // Instanciar ViewModel DESPUÉS de configurar los mocks específicos
        viewModel = EncuestaViewModel(application)

        // Act: Registrar respuesta
        viewModel.registrarRespuesta(tourId, usuarioId, calificacion, comentario)
        
        // Avanzar el scheduler para ejecutar todas las coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se guardó la respuesta
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) }
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, 50) }
        
        // Verificar que la respuesta se guardó correctamente
        val capturaEncuesta = slot<EncuestaRespuesta>()
        verify { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(capture(capturaEncuesta)) }
        assertEquals(tourId, capturaEncuesta.captured.idTour)
        assertEquals(usuarioId.toString(), capturaEncuesta.captured.usuarioId)
        assertEquals(calificacion, capturaEncuesta.captured.calificacion)
        assertEquals(comentario, capturaEncuesta.captured.comentario)
        
        // Verificar que se actualizó el LiveData verificando los valores directamente
        // Nota: Debido a que viewModelScope puede no usar el test dispatcher correctamente,
        // verificamos los valores del LiveData en lugar de los observers
        val mensajeEstado = viewModel.mensajeEstado.value
        val encuestaEnviada = viewModel.encuestaEnviada.value
        val respuestaEncuesta = viewModel.respuestaEncuesta.value
        
        assertNotNull("El mensaje de estado debe actualizarse", mensajeEstado)
        assertTrue("La encuesta debe marcarse como enviada", encuestaEnviada == true)
        assertNotNull("La respuesta de encuesta debe guardarse", respuestaEncuesta)
        assertEquals(tourId, respuestaEncuesta?.idTour)
        assertEquals(usuarioId.toString(), respuestaEncuesta?.usuarioId)
        assertEquals(calificacion, respuestaEncuesta?.calificacion)
        assertEquals(comentario, respuestaEncuesta?.comentario)
    }

    @Test
    fun `test no se envía encuesta si el usuario ya respondió`() = runTest {
        // Arrange: Usuario ya respondió la encuesta
        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(tourId, usuarioId.toString()) } returns true

        // Act: Intentar enviar encuesta automática
        val exito = repository.enviarEncuestaAutomatica(tourId, usuarioId)

        // Assert: No debe enviarse
        assertFalse("No debe enviarse si ya respondió", exito)
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) }
    }

    @Test
    fun `test no se registra respuesta duplicada`() = runTest {
        // Arrange: Usuario ya respondió
        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(tourId, usuarioId.toString()) } returns true

        // Instanciar ViewModel
        viewModel = EncuestaViewModel(application)

        // Observador
        val mensajeObserver = mockk<Observer<String>>(relaxed = true)
        viewModel.mensajeEstado.observeForever(mensajeObserver)

        // Act: Intentar registrar respuesta duplicada
        viewModel.registrarRespuesta(tourId, usuarioId, 5, "Comentario")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: No debe guardarse
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) }
        verify { mensajeObserver.onChanged(any()) }
    }

    @Test
    fun `test validación de calificación debe estar entre 1 y 5`() = runTest {
        // Arrange: Calificación inválida
        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(any(), any()) } returns false

        // Instanciar ViewModel
        viewModel = EncuestaViewModel(application)

        // Observador
        val mensajeObserver = mockk<Observer<String>>(relaxed = true)
        viewModel.mensajeEstado.observeForever(mensajeObserver)

        // Act: Intentar registrar con calificación inválida
        viewModel.registrarRespuesta(tourId, usuarioId, 6, "Comentario")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: No debe guardarse
        verify(exactly = 0) { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) }
        verify { mensajeObserver.onChanged(any()) }
    }

    @Test
    fun `test puntos se suman al completar encuesta`() = runTest {
        // Arrange: Usuario completa encuesta
        val calificacion = 5
        val comentario = "Excelente"

        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(tourId, usuarioId.toString()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, 50) } returns true

        // Act: Registrar respuesta
        val respuesta = repository.guardarRespuestaEncuesta(tourId, usuarioId, calificacion, comentario)

        // Assert: Verificar que se sumaron los puntos
        assertNotNull("Debe guardarse correctamente", respuesta)
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, 50) }
    }

    @Test
    fun `test calificación promedio se calcula correctamente`() = runTest {
        // Arrange: Múltiples respuestas de encuesta
        val calificaciones = listOf(5, 4, 5, 3, 4)
        val promedioEsperado = calificaciones.average()

        every { anyConstructed<DatabaseHelper>().obtenerCalificacionPromedioTour(tourId) } returns promedioEsperado

        // Act: Obtener calificación promedio
        val promedio = repository.obtenerCalificacionPromedioTour(tourId)

        // Assert: Verificar que el promedio es correcto
        assertEquals(promedioEsperado, promedio, 0.01)
    }

    @Test
    fun `test flujo completo desde notificación hasta respuesta`() = runTest {
        // Arrange: Tour finalizado, usuario no ha respondido
        val tour = com.grupo4.appreservas.modelos.Tour(
            tourId = tourId,
            nombre = "Tour a Machu Picchu",
            fecha = "2024-11-19",
            hora = "09:00",
            puntoEncuentro = "Cusco",
            capacidad = 15,
            participantesConfirmados = 10,
            estado = "Completado"
        )

        every { anyConstructed<DatabaseHelper>().obtenerTourPorId(tourId) } returns tour
        every { anyConstructed<DatabaseHelper>().existeEncuestaRespuesta(tourId, usuarioId.toString()) } returns false
        every { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) } returns 1L
        every { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, 50) } returns true
        every { anyConstructed<DatabaseHelper>().obtenerNotificacionesPorUsuario(usuarioId) } returns emptyList()

        // Act 1: Enviar encuesta automática
        val encuestaEnviada = repository.enviarEncuestaAutomatica(tourId, usuarioId)

        // Assert 1: Verificar que se envió
        assertTrue(encuestaEnviada)
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarNotificacion(any()) }

        // Act 2: Usuario responde la encuesta
        val calificacion = 4
        val comentario = "Muy buena experiencia"
        val respuestaGuardada = repository.guardarRespuestaEncuesta(tourId, usuarioId, calificacion, comentario)

        // Assert 2: Verificar que se guardó y se sumaron puntos
        assertNotNull("La respuesta debe guardarse correctamente", respuestaGuardada)
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarEncuestaRespuesta(any()) }
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().sumarPuntos(usuarioId, 50) }
    }
}

