package com.grupo4.appreservas.service

import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class QRServiceTest {

    private lateinit var qrService: QRService

    @Before
    fun setUp() {
        qrService = QRService()
    }

    @Test
    fun `test generate crea código QR con formato correcto`() {
        // Arrange
        val data = "PS12345678"

        // Act
        val resultado = qrService.generate(data)

        // Assert
        assertEquals("QR_CODE_BASE64_PS12345678", resultado)
    }

    @Test
    fun `test generate maneja diferentes tipos de datos`() {
        // Arrange
        val data1 = "BOOKING_123"
        val data2 = "CONFIRMATION_456"

        // Act
        val resultado1 = qrService.generate(data1)
        val resultado2 = qrService.generate(data2)

        // Assert
        assertTrue(resultado1.startsWith("QR_CODE_BASE64_"))
        assertTrue(resultado2.startsWith("QR_CODE_BASE64_"))
        assertNotEquals(resultado1, resultado2)
    }
}
