package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.Rol
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.AutenticacionViewModel
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
import java.security.MessageDigest

/**
 * Pruebas de integración para HU-004: Registro e Inicio de Sesión
 * 
 * Escenarios a probar:
 * 1. Registro de cuenta de usuario: El usuario completa el formulario de registro con datos válidos.
 *    Si no viene un rol en el body, le asigna el rol por defecto que es el de turista.
 * 2. Inicio de sesión: El usuario ingresa credenciales correctas y se redirige según su rol.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntegracionAutenticacionTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var viewModel: AutenticacionViewModel
    private lateinit var repository: PeruvianServiceRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        application = mockk(relaxed = true)
        every { application.applicationContext } returns context

        // Mock DatabaseHelper - IMPORTANTE: configurar mocks ANTES de instanciar el repositorio
        mockkConstructor(DatabaseHelper::class)
        
        // Mock de roles
        every { anyConstructed<DatabaseHelper>().obtenerRol(1) } returns Rol(1, "Administrador")
        every { anyConstructed<DatabaseHelper>().obtenerRol(2) } returns Rol(2, "Turista")
        
        // Mock de búsqueda de usuario por correo - debe estar mockeado para evitar acceso a BD
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(any()) } returns null
        
        // Mock de inserción de usuario - debe estar mockeado para evitar acceso a BD
        var usuarioIdCounter = 1
        every { anyConstructed<DatabaseHelper>().insertarUsuario(any()) } answers {
            val usuario = firstArg<Usuario>()
            usuarioIdCounter++
            usuarioIdCounter.toLong()
        }
        
        // Mock de búsqueda de usuario por ID
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(any()) } answers {
            val id = firstArg<Int>()
            Usuario(
                usuarioId = id,
                nombreCompleto = "Usuario Test",
                correo = "test@example.com",
                contrasena = hashSHA256("password123"),
                rolId = 2,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        // Instanciar repositorio DESPUÉS de configurar todos los mocks
        repository = PeruvianServiceRepository.getInstance(context)
        viewModel = AutenticacionViewModel(repository)
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
    fun `test HU-004 Escenario 1 - Registro de usuario sin rol asigna rol turista por defecto`() = runTest {
        // Arrange: Datos de registro sin especificar rol
        val nombreCompleto = "Juan Pérez"
        val nombreUsuario = "juan@example.com"
        val contrasena = "password123"

        // Mock: Usuario no existe
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) } returns null
        
        // Mock: Insertar usuario retorna ID
        var usuarioInsertado: Usuario? = null
        every { anyConstructed<DatabaseHelper>().insertarUsuario(any()) } answers {
            val usuario = firstArg<Usuario>()
            usuarioInsertado = usuario.copy(usuarioId = 1)
            1L
        }
        
        // Mock: Buscar usuario por ID después de insertar
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(1) } answers {
            usuarioInsertado ?: Usuario(
                usuarioId = 1,
                nombreCompleto = nombreCompleto,
                correo = nombreUsuario,
                contrasena = hashSHA256(contrasena),
                rolId = 2, // Rol por defecto (turista)
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }
        
        // Mock: Obtener rol
        every { anyConstructed<DatabaseHelper>().obtenerRol(2) } returns Rol(2, "Turista")

        // Observador para LiveData
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuarioAutenticado.observeForever(usuarioObserver)

        // Act: Registrar usuario sin especificar rol (debe asignar turista por defecto)
        viewModel.registrarUsuario(nombreCompleto, nombreUsuario, contrasena)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se creó el usuario
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarUsuario(any()) }
        verify { usuarioObserver.onChanged(any()) }
        
        // Verificar que el usuario tiene el rol por defecto (turista)
        assertNotNull(usuarioInsertado)
        assertEquals(2, usuarioInsertado?.rolId) // Debe ser turista (rol por defecto)
        val rol = repository.obtenerRol(2)
        assertEquals("Turista", rol?.nombreRol)
    }

    @Test
    fun `test HU-004 Escenario 2 - Inicio de sesión con credenciales correctas redirige según rol`() = runTest {
        // Arrange: Usuario existente
        val nombreUsuario = "test@example.com"
        val contrasena = "password123"
        val contrasenaHash = hashSHA256(contrasena)
        val usuario = Usuario(
            usuarioId = 1,
            nombreCompleto = "Usuario Test",
            correo = nombreUsuario,
            contrasena = contrasenaHash,
            rolId = 2, // Turista
            fechaCreacion = "2025-01-01 00:00:00"
        )

        // Mock: Buscar usuario por correo (nombreUsuario se almacena como correo)
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) } returns usuario
        every { anyConstructed<DatabaseHelper>().obtenerRol(2) } returns Rol(2, "Turista")
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(1) } returns usuario

        // Observador para LiveData
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuarioAutenticado.observeForever(usuarioObserver)

        // Act: Iniciar sesión
        viewModel.iniciarSesion(nombreUsuario, contrasena)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se validaron las credenciales
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) }
        verify { usuarioObserver.onChanged(any()) }
        
        // Verificar que se puede obtener el rol
        val rol = repository.obtenerRol(usuario.usuarioId)
        assertNotNull(rol)
        assertEquals("Turista", rol?.nombreRol)
    }

    @Test
    fun `test inicio de sesión con credenciales incorrectas muestra mensaje de error`() = runTest {
        // Arrange: Usuario existente con contraseña diferente
        val nombreUsuario = "test@example.com"
        val contrasenaCorrecta = "password123"
        val contrasenaIncorrecta = "wrongpassword"
        val usuario = Usuario(
            usuarioId = 1,
            nombreCompleto = "Usuario Test",
            correo = nombreUsuario,
            contrasena = hashSHA256(contrasenaCorrecta),
            rolId = 2,
            fechaCreacion = "2025-01-01 00:00:00"
        )

        // Mock: Buscar usuario por correo
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) } returns usuario

        // Observador para LiveData
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        val mensajeObserver = mockk<Observer<String?>>(relaxed = true)
        viewModel.usuarioAutenticado.observeForever(usuarioObserver)
        viewModel.mensajeEstado.observeForever(mensajeObserver)

        // Act: Iniciar sesión con contraseña incorrecta
        viewModel.iniciarSesion(nombreUsuario, contrasenaIncorrecta)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que no se autenticó
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) }
        // El usuario no debe ser autenticado (debe ser null)
        assertNull(viewModel.usuarioAutenticado.value)
        // Debe mostrar mensaje de error
        verify { mensajeObserver.onChanged(any()) }
    }

    @Test
    fun `test registro con rol específico asigna el rol correcto`() = runTest {
        // Arrange: Registrar con rol específico
        val nombreCompleto = "Admin Test"
        val nombreUsuario = "admin@example.com"
        val contrasena = "password123"
        val rolId = 1 // Administrador

        // Mock: Usuario no existe
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) } returns null
        
        // Mock: Insertar usuario
        var usuarioInsertado: Usuario? = null
        every { anyConstructed<DatabaseHelper>().insertarUsuario(any()) } answers {
            val usuario = firstArg<Usuario>()
            usuarioInsertado = usuario.copy(usuarioId = 1)
            1L
        }
        
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(1) } answers {
            usuarioInsertado ?: Usuario(
                usuarioId = 1,
                nombreCompleto = nombreCompleto,
                correo = nombreUsuario,
                contrasena = hashSHA256(contrasena),
                rolId = rolId,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        // Observador
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuarioAutenticado.observeForever(usuarioObserver)

        // Act: Registrar con rol específico
        viewModel.registrarUsuario(nombreCompleto, nombreUsuario, contrasena, rolId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que se creó con el rol especificado
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarUsuario(any()) }
        assertNotNull(usuarioInsertado)
        assertEquals(rolId, usuarioInsertado?.rolId)
    }

    @Test
    fun `test contraseña se hashea correctamente con SHA-256`() = runTest {
        // Arrange
        val contrasena = "password123"
        val hashEsperado = hashSHA256(contrasena)
        
        // Mock: Usuario no existe
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo("test@example.com") } returns null
        
        // Mock: Insertar usuario retorna ID
        every { anyConstructed<DatabaseHelper>().insertarUsuario(any()) } returns 1L
        
        // Mock: Buscar usuario por ID después de insertar
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(1) } answers {
            Usuario(
                usuarioId = 1,
                nombreCompleto = "Test",
                correo = "test@example.com",
                contrasena = hashEsperado,
                rolId = 2,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        // Act: Crear usuario (esto hashea la contraseña)
        val usuario = repository.crearUsuario("Test", "test@example.com", contrasena)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que la contraseña está hasheada
        assertNotEquals(contrasena, usuario.contrasena)
        assertEquals(hashEsperado, usuario.contrasena)
    }

    @Test
    fun `test flujo completo de registro e inicio de sesión`() = runTest {
        // Arrange: Datos de registro
        val nombreCompleto = "Nuevo Usuario"
        val nombreUsuario = "nuevo@example.com"
        val contrasena = "password123"

        // Mock: Usuario no existe inicialmente
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) } returns null
        
        // Mock: Insertar usuario
        var usuarioId = 2L
        every { anyConstructed<DatabaseHelper>().insertarUsuario(any()) } answers {
            usuarioId
        }
        
        // Mock: Buscar usuario después de insertar
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(2) } answers {
            Usuario(
                usuarioId = 2,
                nombreCompleto = nombreCompleto,
                correo = nombreUsuario,
                contrasena = hashSHA256(contrasena),
                rolId = 2,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        // Observadores
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuarioAutenticado.observeForever(usuarioObserver)

        // Act 1: Registrar
        viewModel.registrarUsuario(nombreCompleto, nombreUsuario, contrasena)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert 1: Verificar registro
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarUsuario(any()) }

        // Act 2: Iniciar sesión con las mismas credenciales
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) } answers {
            Usuario(
                usuarioId = 2,
                nombreCompleto = nombreCompleto,
                correo = nombreUsuario,
                contrasena = hashSHA256(contrasena),
                rolId = 2,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        viewModel.iniciarSesion(nombreUsuario, contrasena)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert 2: Verificar inicio de sesión
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) }
        verify { usuarioObserver.onChanged(any()) }
    }

    @Test
    fun `test obtenerUsuarioActual retorna usuario autenticado`() = runTest {
        // Arrange: Usuario autenticado
        val nombreUsuario = "test@example.com"
        val contrasena = "password123"
        val usuario = Usuario(
            usuarioId = 1,
            nombreCompleto = "Usuario Test",
            correo = nombreUsuario,
            contrasena = hashSHA256(contrasena),
            rolId = 2,
            fechaCreacion = "2025-01-01 00:00:00"
        )

        // Mock: Buscar usuario
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(nombreUsuario) } returns usuario

        // Act: Iniciar sesión
        viewModel.iniciarSesion(nombreUsuario, contrasena)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verificar que obtenerUsuarioActual retorna el usuario
        val usuarioActual = viewModel.obtenerUsuarioActual()
        assertNotNull(usuarioActual)
        assertEquals(usuario.usuarioId, usuarioActual?.usuarioId)
    }

    private fun hashSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
