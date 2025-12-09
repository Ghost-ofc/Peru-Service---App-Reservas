package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.PuntosUsuario
import com.grupo4.appreservas.modelos.Recibo
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para mostrar el voucher/comprobante de pago.
 * Equivalente a ReservaConfirmadaActivity del diagrama UML.
 * Corresponde a la HU: Pago exitoso - mostrar comprobante.
 */
class VoucherActivity : AppCompatActivity() {

    private lateinit var comprobante: Recibo
    private var usuarioId: Int = 0
    private var puntosGanados: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)

        val comprobanteExtra = intent.getSerializableExtra("COMPROBANTE") as? Recibo
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        puntosGanados = intent.getIntExtra("PUNTOS_GANADOS", PuntosUsuario.PUNTOS_POR_RESERVA)

        if (comprobanteExtra == null) {
            finish()
            return
        }

        comprobante = comprobanteExtra

        inicializarVistas()
        mostrarResumenReserva(comprobante)
        mostrarMensajePuntos(puntosGanados)
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

    /**
     * Muestra el resumen de la reserva.
     * Equivalente a mostrarResumenReserva(reserva) del diagrama UML.
     */
    private fun mostrarResumenReserva(recibo: Recibo) {
        // Información del destino
        findViewById<TextView>(R.id.txtDestinoNombre).text = recibo.destinoNombre

        // Fecha y hora
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.txtFecha).text = dateFormat.format(recibo.fecha)
        findViewById<TextView>(R.id.txtHora).text = recibo.horaInicio

        // Personas
        findViewById<TextView>(R.id.txtNumPersonas).text = "${recibo.numPersonas} persona${if (recibo.numPersonas > 1) "s" else ""}"

        // Método de pago
        findViewById<TextView>(R.id.txtMetodoPago).text = recibo.metodoPago

        // Monto total
        findViewById<TextView>(R.id.txtMontoTotal).text = "S/ ${String.format("%.2f", recibo.montoTotal)}"

        // Código de confirmación
        findViewById<TextView>(R.id.txtCodigoConfirmacion).text = recibo.codigoConfirmacion

        // QR Code (por ahora solo mostramos un placeholder)
        // En una implementación real, usarías una librería como ZXing para generar el QR
        val imgQR = findViewById<ImageView>(R.id.imgQR)
        // imgQR.setImageBitmap(generarQRCode(recibo.qrCode))
    }

    /**
     * Muestra el mensaje de puntos ganados.
     * Equivalente a mostrarMensajePuntos(puntosGanados) del diagrama UML.
     */
    private fun mostrarMensajePuntos(puntosGanados: Int) {
        val cardPuntos = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardPuntosGanados)
        if (cardPuntos != null && puntosGanados > 0) {
            cardPuntos.visibility = android.view.View.VISIBLE
            // Actualizar el TextView que muestra los puntos
            val tvPuntos = findViewById<TextView>(R.id.txtPuntosGanados)
            tvPuntos?.text = "+$puntosGanados"
        }
    }

    /**
     * Navega al perfil del usuario.
     * Equivalente a irAPerfilUsuario() del diagrama UML.
     */
    private fun irAPerfilUsuario() {
        if (usuarioId > 0) {
            val intent = Intent(this, RecompensasActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        }
    }

    private fun configurarListeners() {
        // Ya configurado en inicializarVistas
    }
}

