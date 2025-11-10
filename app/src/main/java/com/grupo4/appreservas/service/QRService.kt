package com.grupo4.appreservas.service

/**
 * Servicio para generar códigos QR.
 * El QR contiene el reservaId para que el guía pueda registrar el check-in.
 */
class QRService {

    /**
     * Genera un código QR que contiene el reservaId.
     * El formato del QR es: "RESERVA:<reservaId>"
     * 
     * @param reservaId ID de la reserva a incluir en el QR
     * @return String con el formato del QR que será codificado visualmente
     */
    fun generate(reservaId: String): String {
        // Formato: "RESERVA:<reservaId>" para que sea fácil de parsear
        // El guía escaneará este código y extraerá el reservaId
        return "RESERVA:$reservaId"
    }
    
    /**
     * Extrae el reservaId de un código QR escaneado.
     * 
     * @param qrData Datos del QR escaneado
     * @return reservaId extraído o null si el formato no es válido
     */
    fun extraerReservaId(qrData: String): String? {
        return if (qrData.startsWith("RESERVA:")) {
            qrData.removePrefix("RESERVA:")
        } else {
            // Compatibilidad con formato anterior (código de confirmación)
            // Intentar buscar la reserva por código QR
            qrData
        }
    }
}