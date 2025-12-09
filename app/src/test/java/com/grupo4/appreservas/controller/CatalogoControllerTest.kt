package com.grupo4.appreservas

import com.grupo4.appreservas.controller.CatalogoController
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.DestinoService
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CatalogoControllerTest {

    private lateinit var catalogoController: CatalogoController
    private lateinit var destinoService: DestinoService

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
        destinoService = mockk()
        catalogoController = CatalogoController(destinoService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test solicitarDestinos devuelve lista de destinos correctamente`() {
        // Arrange
        every { destinoService.listarDestinos() } returns destinosMock

        // Act
        val resultado = catalogoController.solicitarDestinos()

        // Assert
        assertEquals(2, resultado.size)
        assertEquals("Tour Machu Picchu Clásico", resultado[0].nombre)
        assertEquals("Líneas de Nazca Tour Aéreo", resultado[1].nombre)
        verify(exactly = 1) { destinoService.listarDestinos() }
    }

    @Test
    fun `test solicitarDestinos devuelve lista vacía cuando no hay destinos`() {
        // Arrange
        every { destinoService.listarDestinos() } returns emptyList()

        // Act
        val resultado = catalogoController.solicitarDestinos()

        // Assert
        assertTrue(resultado.isEmpty())
        verify(exactly = 1) { destinoService.listarDestinos() }
    }
}