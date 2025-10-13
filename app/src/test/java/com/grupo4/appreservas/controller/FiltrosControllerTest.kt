package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.DestinoService
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class FiltrosControllerTest {

    private lateinit var filtrosController: FiltrosController
    private lateinit var destinationService: DestinoService

    private val destinosMock = listOf(
        Destino(
            id = "dest_001",
            nombre = "Tour Económico",
            precio = 150.0,
            categorias = listOf("Cultura")
        ),
        Destino(
            id = "dest_002",
            nombre = "Tour Premium",
            precio = 450.0,
            categorias = listOf("Aventura")
        )
    )

    @Before
    fun setUp() {
        destinationService = mockk()
        filtrosController = FiltrosController(destinationService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test listarDestinos devuelve todos los destinos`() {
        // Arrange
        every { destinationService.listarDestinos() } returns destinosMock

        // Act
        val resultado = filtrosController.listarDestinos()

        // Assert
        assertEquals(2, resultado.size)
        verify(exactly = 1) { destinationService.listarDestinos() }
    }

    @Test
    fun `test filtrarDestinos por precio mínimo`() {
        // Arrange
        val criterios = mapOf("precioMin" to 200.0)
        val destinosFiltrados = listOf(destinosMock[1])
        every { destinationService.filtrarDestinos(criterios) } returns destinosFiltrados

        // Act
        val resultado = filtrosController.filtrarDestinos(criterios)

        // Assert
        assertEquals(1, resultado.size)
        assertTrue(resultado[0].precio >= 200.0)
    }

    @Test
    fun `test filtrarDestinos por precio máximo`() {
        // Arrange
        val criterios = mapOf("precioMax" to 300.0)
        val destinosFiltrados = listOf(destinosMock[0])
        every { destinationService.filtrarDestinos(criterios) } returns destinosFiltrados

        // Act
        val resultado = filtrosController.filtrarDestinos(criterios)

        // Assert
        assertEquals(1, resultado.size)
        assertTrue(resultado[0].precio <= 300.0)
    }

    @Test
    fun `test filtrarDestinos por categoría`() {
        // Arrange
        val criterios = mapOf("categoria" to "Aventura")
        val destinosFiltrados = listOf(destinosMock[1])
        every { destinationService.filtrarDestinos(criterios) } returns destinosFiltrados

        // Act
        val resultado = filtrosController.filtrarDestinos(criterios)

        // Assert
        assertEquals(1, resultado.size)
        assertTrue(resultado[0].categorias.contains("Aventura"))
    }
}