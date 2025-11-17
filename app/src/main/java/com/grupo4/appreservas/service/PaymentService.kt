package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import kotlinx.coroutines.runBlocking

/**
 * Servicio para procesamiento de pagos.
 * Equivalente a PaymentService del diagrama UML.
 */
class PaymentService(
    private val repository: PeruvianServiceRepository,
    private val paymentGateway: PaymentGateway
) {

    /**
     * Procesa un pago con Yape.
     * Equivalente a payYapePlin(req): Payment del diagrama UML.
     */
    suspend fun payYapePlin(request: Map<String, Any>): Pago {
        val requestWithMethod = request.toMutableMap()
        requestWithMethod["metodoPago"] = MetodoPago.YAPE
        
        val pago = paymentGateway.charge(requestWithMethod)
        
        // Generar ID para el pago
        val pagoConId = pago.copy(id = "PAY_${System.currentTimeMillis()}")
        
        // Guardar el pago
        repository.guardarPago(pagoConId)
        
        return pagoConId
    }

    /**
     * Procesa un pago con Plin.
     * Equivalente a payYapePlin(req): Payment del diagrama UML.
     */
    suspend fun payPlin(request: Map<String, Any>): Pago {
        val requestWithMethod = request.toMutableMap()
        requestWithMethod["metodoPago"] = MetodoPago.PLIN
        
        val pago = paymentGateway.charge(requestWithMethod)
        
        // Generar ID para el pago
        val pagoConId = pago.copy(id = "PAY_${System.currentTimeMillis()}")
        
        // Guardar el pago
        repository.guardarPago(pagoConId)
        
        return pagoConId
    }

    /**
     * Procesa un pago con tarjeta.
     * Equivalente a payCard(req): Payment del diagrama UML.
     */
    suspend fun payCard(request: Map<String, Any>): Pago {
        val requestWithMethod = request.toMutableMap()
        requestWithMethod["metodoPago"] = MetodoPago.TARJETA
        
        val pago = paymentGateway.charge(requestWithMethod)
        
        // Generar ID para el pago
        val pagoConId = pago.copy(id = "PAY_${System.currentTimeMillis()}")
        
        // Guardar el pago
        repository.guardarPago(pagoConId)
        
        return pagoConId
    }
}

