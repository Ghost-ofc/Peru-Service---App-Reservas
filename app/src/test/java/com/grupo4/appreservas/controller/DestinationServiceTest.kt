package com.grupo4.appreservas.controller

import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.service.DestinoService
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DestinationServiceTest {

    private lateinit var destinationService: DestinoService
    private lateinit var destinoRepository: DestinoRepository

    private val destinosMock = listOf(
        Destino(id = "dest_001", nombre = "Tour A", precio = 100.0, categorias = listOf("Cultura")),
        Destino(id = "dest_002", nombre = "Tour B", precio = 300.0, categorias = listOf("Aventura")),
        Destino(id = "dest_003", nombre = "Tour C", precio = 500.0, categorias = listOf("Cultura"))
    )

    @Before
    fun setUp() {
        destinoRepository = mockk()
        destinationService = DestinoService(destinoRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test listarDestinos devuelve todos los destinos del repositorio`() {
        // Arrange
        every { destinoRepository.getDestinos() } returns destinosMock

        // Act
        val resultado = destinationService.listarDestinos()

        // Assert
        assertEquals(3, resultado.size)
        verify(exactly = 1) { destinoRepository.getDestinos() }
    }

    @Test
    fun `test filtrarDestinos por categoría filtra correctamente`() {
        // Arrange
        every { destinoRepository.getDestinos() } returns destinosMock
        val criterios = mapOf("categoria" to "Cultura")

        // Act
        val resultado = destinationService.filtrarDestinos(criterios)

        // Assert
        assertEquals(2, resultado.size)
        assertTrue(resultado.all { it.categorias.contains("Cultura") })
    }

    @Test
    fun `test filtrarDestinos por precio mínimo filtra correctamente`() {
        // Arrange
        every { destinoRepository.getDestinos() } returns destinosMock
        val criterios = mapOf("precioMin" to 250.0)

        // Act
        val resultado = destinationService.filtrarDestinos(criterios)

        // Assert
        assertEquals(2, resultado.size)
        assertTrue(resultado.all { it.precio >= 250.0 })
    }

    @Test
    fun `test filtrarDestinos por precio máximo filtra correctamente`() {
        // Arrange
        every { destinoRepository.getDestinos() } returns destinosMock
        val criterios = mapOf("precioMax" to 350.0)

        // Act
        val resultado = destinationService.filtrarDestinos(criterios)

        // Assert
        assertEquals(2, resultado.size)
        assertTrue(resultado.all { it.precio <= 350.0 })
    }

    @Test
    fun `test filtrarDestinos con múltiples criterios`() {
        // Arrange
        every { destinoRepository.getDestinos() } returns destinosMock
        val criterios = mapOf(
            "categoria" to "Cultura",
            "precioMin" to 200.0
        )

        // Act
        val resultado = destinationService.filtrarDestinos(criterios)

        // Assert
        assertEquals(1, resultado.size)
        assertEquals("Tour C", resultado[0].nombre)
    }

    @Test
    fun `test obtenerDetalle devuelve destino existente`() {
        // Arrange
        val destinoId = "dest_001"
        every { destinoRepository.getDetalle(destinoId) } returns destinosMock[0]

        // Act
        val resultado = destinationService.obtenerDetalle(destinoId)

        // Assert
        assertNotNull(resultado)
        assertEquals(destinoId, resultado?.id)
    }

    @Test
    fun `test obtenerDetalle devuelve null para destino inexistente`() {
        // Arrange
        val destinoId = "dest_999"
        every { destinoRepository.getDetalle(destinoId) } returns null

        // Act
        val resultado = destinationService.obtenerDetalle(destinoId)

        // Assert
        assertNull(resultado)
    }
}