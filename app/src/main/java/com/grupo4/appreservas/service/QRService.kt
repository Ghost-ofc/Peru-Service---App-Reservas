package com.grupo4.appreservas.service

class QRService {

    fun generate(data: String): String {
        // Simulación de generación de QR
        // En producción usar una librería como ZXing
        return "QR_CODE_BASE64_$data"
    }
}