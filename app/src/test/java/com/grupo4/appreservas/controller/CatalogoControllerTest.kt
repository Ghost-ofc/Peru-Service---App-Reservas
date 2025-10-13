package com.grupo4.appreservas.controller

import com.grupo4.appreservas.controller.CatalogoController
import com.grupo4.appreservas.controller.FiltrosController
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinoService
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CatalogoControllerTest {

    private lateinit var catalogoController: CatalogoController
    private lateinit var destinationService: DestinoService
    private lateinit var availabilityService: AvailabilityService

    private val destinosMock = listOf(
        Destino(
            id = "dest_001",
            nombre = "Tour Machu Picchu Clásico",
            ubicacion = "Cusco, Perú",
            descripcion = "Descubre la majestuosa ciudadela inca",
            precio = 450.0,
            duracionHoras = 12,
            maxPersonas = 15,
            categorias = listOf("Cultura", "Arqueología", "Naturaleza"),
            imagenUrl = "https://example.com/machu.jpg",
            calificacion = 4.8,
            numReseñas = 124,
            disponibleTodosDias = true
        ),
        Destino(
            id = "dest_002",
            nombre = "Líneas de Nazca Tour Aéreo",
            ubicacion = "Ica, Perú",
            descripcion = "Sobrevuela las misteriosas líneas",
            precio = 380.0,
            duracionHoras = 6,
            maxPersonas = 8,
            categorias = listOf("Aventura", "Arqueología", "Aéreo"),
            imagenUrl = "https://example.com/nazca.jpg",
            calificacion = 4.6,
            numReseñas = 87,
            disponibleTodosDias = false
        )
    )

    @Before
    fun setUp() {
        destinationService = mockk()
        availabilityService = mockk()
        catalogoController = CatalogoController(destinationService, availabilityService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test solicitarDestinos devuelve lista de destinos correctamente`() {
        // Arrange
        every { destinationService.listarDestinos() } returns destinosMock

        // Act
        val resultado = catalogoController.solicitarDestinos()

        // Assert
        assertEquals(2, resultado.size)
        assertEquals("Tour Machu Picchu Clásico", resultado[0].nombre)
        assertEquals("Líneas de Nazca Tour Aéreo", resultado[1].nombre)
        verify(exactly = 1) { destinationService.listarDestinos() }
    }

    @Test
    fun `test solicitarDestinos devuelve lista vacía cuando no hay destinos`() {
        // Arrange
        every { destinationService.listarDestinos() } returns emptyList()

        // Act
        val resultado = catalogoController.solicitarDestinos()

        // Assert
        assertTrue(resultado.isEmpty())
        verify(exactly = 1) { destinationService.listarDestinos() }
    }

    @Test
    fun `test solicitarDisponibilidad devuelve información correcta`() {
        // Arrange
        val destinoId = "dest_001"
        val fecha = "2025-10-14"
        every { destinationService.obtenerDetalle(destinoId) } returns destinosMock[0]

        // Act
        val resultado = catalogoController.solicitarDisponibilidad(destinoId, fecha)

        // Assert
        assertNotNull(resultado)
        assertEquals(destinoId, resultado?.get("destinoId"))
        assertEquals(fecha, resultado?.get("fecha"))
        assertEquals(6, resultado?.get("cuposDisponibles"))
    }

    @Test
    fun `test solicitarDisponibilidad devuelve null cuando destino no existe`() {
        // Arrange
        val destinoId = "dest_999"
        val fecha = "2025-10-14"
        every { destinationService.obtenerDetalle(destinoId) } returns null

        // Act
        val resultado = catalogoController.solicitarDisponibilidad(destinoId, fecha)

        // Assert
        assertNull(resultado)
    }

    @Test
    fun `test aplicarFiltros por categoría devuelve destinos filtrados`() {
        // Arrange
        val criterios = mapOf("categoria" to "Cultura")
        val destinosFiltrados = listOf(destinosMock[0])
        every { destinationService.filtrarDestinos(criterios) } returns destinosFiltrados

        // Act
        val resultado = catalogoController.aplicarFiltros(criterios)

        // Assert
        assertEquals(1, resultado.size)
        assertTrue(resultado[0].categorias.contains("Cultura"))
        verify(exactly = 1) { destinationService.filtrarDestinos(criterios) }
    }
}