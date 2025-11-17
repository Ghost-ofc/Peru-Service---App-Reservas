package com.grupo4.appreservas.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Recibo
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para mostrar el voucher/comprobante de pago.
 * Corresponde a la HU: Pago exitoso - mostrar comprobante.
 */
class VoucherActivity : AppCompatActivity() {

    private lateinit var comprobante: Recibo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)

        val comprobanteExtra = intent.getSerializableExtra("COMPROBANTE") as? Recibo

        if (comprobanteExtra == null) {
            finish()
            return
        }

        comprobante = comprobanteExtra

        inicializarVistas()
        mostrarComprobante()
        configurarListeners()
    }

    private fun inicializarVistas() {
        // Configurar botón back
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Configurar botón ver mis reservas
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnVerMisReservas)
            .setOnClickListener {
                // Volver al catálogo
                val intent = android.content.Intent(this, CatalogoActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
    }

    private fun mostrarComprobante() {
        // Información del destino
        findViewById<TextView>(R.id.txtDestinoNombre).text = comprobante.destinoNombre

        // Fecha y hora
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.txtFecha).text = dateFormat.format(comprobante.fecha)
        findViewById<TextView>(R.id.txtHora).text = comprobante.horaInicio

        // Personas
        findViewById<TextView>(R.id.txtNumPersonas).text = "${comprobante.numPersonas} persona${if (comprobante.numPersonas > 1) "s" else ""}"

        // Método de pago
        findViewById<TextView>(R.id.txtMetodoPago).text = comprobante.metodoPago

        // Monto total
        findViewById<TextView>(R.id.txtMontoTotal).text = "S/ ${String.format("%.2f", comprobante.montoTotal)}"

        // Código de confirmación
        findViewById<TextView>(R.id.txtCodigoConfirmacion).text = comprobante.codigoConfirmacion

        // QR Code (por ahora solo mostramos un placeholder)
        // En una implementación real, usarías una librería como ZXing para generar el QR
        val imgQR = findViewById<ImageView>(R.id.imgQR)
        // imgQR.setImageBitmap(generarQRCode(comprobante.qrCode))
        
        // Ocultar puntos ganados por ahora (es parte de otra HU)
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardPuntosGanados).visibility = android.view.View.GONE
    }

    private fun configurarListeners() {
        // Ya configurado en inicializarVistas
    }
}

