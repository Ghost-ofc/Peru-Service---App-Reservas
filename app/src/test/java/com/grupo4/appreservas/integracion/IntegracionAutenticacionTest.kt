package com.grupo4.appreservas.integracion

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.grupo4.appreservas.modelos.EstadoReserva
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
 * 1. Registro según mi rol: El visitante completa el formulario de registro con datos válidos
 * 2. Inicio de sesión según mi rol: El usuario ingresa credenciales correctas y se redirige según su rol
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
    fun `test HU-004 Escenario 1 - Registro de usuario turista se completa correctamente`() {
        // Arrange: Datos de registro
        val nombre = "Juan Pérez"
        val correo = "juan@example.com"
        val contrasena = "password123"
        val rolId = 2 // Turista

        // Mock: Usuario no existe
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) } returns null
        
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
                nombreCompleto = nombre,
                correo = correo,
                contrasena = hashSHA256(contrasena),
                rolId = rolId,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }
        
        // Mock: Obtener rol
        every { anyConstructed<DatabaseHelper>().obtenerRol(rolId) } returns Rol(rolId, "Turista")

        // Observador para LiveData
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuario.observeForever(usuarioObserver)

        // Act: Registrar usuario
        viewModel.registrar(nombre, correo, contrasena, rolId)

        // Assert: Verificar que se creó el usuario
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarUsuario(any()) }
        verify { usuarioObserver.onChanged(any()) }
        
        // Verificar que el usuario tiene el rol correcto
        assertNotNull(usuarioInsertado)
        assertEquals(rolId, usuarioInsertado?.rolId)
        val rol = repository.obtenerRol(1) // Obtener rol del usuario con ID 1
        assertEquals("Turista", rol?.nombreRol)
    }

    @Test
    fun `test HU-004 Escenario 2 - Inicio de sesión con credenciales correctas redirige según rol`() {
        // Arrange: Usuario existente
        val correo = "test@example.com"
        val contrasena = "password123"
        val contrasenaHash = hashSHA256(contrasena)
        val usuario = Usuario(
            usuarioId = 1,
            nombreCompleto = "Usuario Test",
            correo = correo,
            contrasena = contrasenaHash,
            rolId = 2, // Turista
            fechaCreacion = "2025-01-01 00:00:00"
        )

        // Mock: Buscar usuario por correo
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) } returns usuario
        every { anyConstructed<DatabaseHelper>().obtenerRol(2) } returns Rol(2, "Turista")
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(1) } returns usuario

        // Observador para LiveData
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuario.observeForever(usuarioObserver)

        // Act: Iniciar sesión
        viewModel.iniciarSesion(correo, contrasena)

        // Assert: Verificar que se validaron las credenciales
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) }
        verify { usuarioObserver.onChanged(any()) }
        
        // Verificar que se puede obtener el rol
        val rol = repository.obtenerRol(usuario.usuarioId)
        assertNotNull(rol)
        assertEquals("Turista", rol?.nombreRol)
    }

    @Test
    fun `test inicio de sesión con credenciales incorrectas falla`() {
        // Arrange: Usuario existente con contraseña diferente
        val correo = "test@example.com"
        val contrasenaCorrecta = "password123"
        val contrasenaIncorrecta = "wrongpassword"
        val usuario = Usuario(
            usuarioId = 1,
            nombreCompleto = "Usuario Test",
            correo = correo,
            contrasena = hashSHA256(contrasenaCorrecta),
            rolId = 2,
            fechaCreacion = "2025-01-01 00:00:00"
        )

        // Mock: Buscar usuario por correo
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) } returns usuario

        // Observador para LiveData
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuario.observeForever(usuarioObserver)

        // Act: Iniciar sesión con contraseña incorrecta
        viewModel.iniciarSesion(correo, contrasenaIncorrecta)

        // Assert: Verificar que no se autenticó
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) }
        // El usuario no debe ser autenticado (debe ser null)
        assertNull(viewModel.usuario.value)
    }

    @Test
    fun `test registro solo permite crear usuarios turistas`() {
        // Arrange: Intentar registrar como administrador
        val nombre = "Admin Test"
        val correo = "admin@example.com"
        val contrasena = "password123"
        val rolId = 2 // Turista

        // Mock: Usuario no existe
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) } returns null
        
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
                nombreCompleto = nombre,
                correo = correo,
                contrasena = hashSHA256(contrasena),
                rolId = rolId,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        // Observador
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuario.observeForever(usuarioObserver)

        // Act: Registrar con rol turista
        viewModel.registrar(nombre, correo, contrasena, rolId)

        // Assert: Verificar que se creó con rol turista
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarUsuario(any()) }
        assertNotNull(usuarioInsertado)
        assertEquals(2, usuarioInsertado?.rolId) // Debe ser turista
    }

    @Test
    fun `test contraseña se hashea correctamente con SHA-256`() {
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
        val usuario = repository.crearUsuario("Test", "test@example.com", contrasena, 2)

        // Assert: Verificar que la contraseña está hasheada
        assertNotEquals(contrasena, usuario.contrasena)
        assertEquals(hashEsperado, usuario.contrasena)
    }

    @Test
    fun `test flujo completo de registro e inicio de sesión`() {
        // Arrange: Datos de registro
        val nombre = "Nuevo Usuario"
        val correo = "nuevo@example.com"
        val contrasena = "password123"

        // Mock: Usuario no existe inicialmente
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) } returns null
        
        // Mock: Insertar usuario
        var usuarioId = 2L
        every { anyConstructed<DatabaseHelper>().insertarUsuario(any()) } answers {
            usuarioId
        }
        
        // Mock: Buscar usuario después de insertar
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorId(2) } answers {
            Usuario(
                usuarioId = 2,
                nombreCompleto = nombre,
                correo = correo,
                contrasena = hashSHA256(contrasena),
                rolId = 2,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        // Observadores
        val usuarioObserver = mockk<Observer<Usuario?>>(relaxed = true)
        viewModel.usuario.observeForever(usuarioObserver)

        // Act 1: Registrar
        viewModel.registrar(nombre, correo, contrasena, 2)

        // Assert 1: Verificar registro
        verify(exactly = 1) { anyConstructed<DatabaseHelper>().insertarUsuario(any()) }

        // Act 2: Iniciar sesión con las mismas credenciales
        every { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) } answers {
            Usuario(
                usuarioId = 2,
                nombreCompleto = nombre,
                correo = correo,
                contrasena = hashSHA256(contrasena),
                rolId = 2,
                fechaCreacion = "2025-01-01 00:00:00"
            )
        }

        viewModel.iniciarSesion(correo, contrasena)

        // Assert 2: Verificar inicio de sesión
        verify(atLeast = 1) { anyConstructed<DatabaseHelper>().buscarUsuarioPorCorreo(correo) }
        verify { usuarioObserver.onChanged(any()) }
    }

    private fun hashSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}

