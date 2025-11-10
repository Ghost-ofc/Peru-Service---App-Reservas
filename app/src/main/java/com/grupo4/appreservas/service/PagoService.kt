package com.grupo4.appreservas.service

import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.PagoRepository

/**
 * PagoService maneja la lógica de negocio para procesar pagos.
 * Utiliza PaymentGateway para comunicarse con la pasarela externa
 * y PagoRepository para persistir los pagos.
 */
class PagoService(
    private val pagoRepository: PagoRepository,
    private val reservasRepository: ReservasRepository,
    private val paymentGateway: PaymentGateway
) {

    /**
     * Procesa un pago mediante Yape.
     * Equivalente a payYapePlin(req): Payment del diagrama UML.
     */
    suspend fun payYape(req: Map<String, Any>): Pago {
        val requestWithMethod = req.toMutableMap().apply {
            put("metodoPago", MetodoPago.YAPE)
        }
        
        // Usar PaymentGateway para realizar el cargo
        val payment = paymentGateway.charge(requestWithMethod)
        
        // Guardar el pago en el repositorio
        return pagoRepository.save(payment)
    }

    /**
     * Procesa un pago mediante Plin.
     * Equivalente a payYapePlin(req): Payment del diagrama UML.
     */
    suspend fun payPlin(req: Map<String, Any>): Pago {
        val requestWithMethod = req.toMutableMap().apply {
            put("metodoPago", MetodoPago.PLIN)
        }
        
        // Usar PaymentGateway para realizar el cargo
        val payment = paymentGateway.charge(requestWithMethod)
        
        // Guardar el pago en el repositorio
        return pagoRepository.save(payment)
    }

    /**
     * Procesa un pago mediante Tarjeta.
     * Equivalente a payCard(req): Payment del diagrama UML.
     */
    suspend fun payCard(req: Map<String, Any>): Pago {
        val requestWithMethod = req.toMutableMap().apply {
            put("metodoPago", MetodoPago.TARJETA)
        }
        
        // Usar PaymentGateway para realizar el cargo
        val payment = paymentGateway.charge(requestWithMethod)
        
        // Guardar el pago en el repositorio
        return pagoRepository.save(payment)
    }
}