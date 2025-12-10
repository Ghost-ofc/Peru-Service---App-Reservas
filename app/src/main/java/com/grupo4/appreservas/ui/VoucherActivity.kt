package com.grupo4.appreservas.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
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
                // Ir al perfil para ver las reservas
                val intent = android.content.Intent(this, RecompensasActivity::class.java)
                intent.putExtra("USUARIO_ID", usuarioId)
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

        // QR Code - Generar y mostrar el código QR
        val imgQR = findViewById<ImageView>(R.id.imgQR)
        // Usar el código QR o el código de confirmación como fallback
        val qrData = when {
            recibo.qrCode.isNotEmpty() -> {
                // Si el qrCode tiene el prefijo "QR_CODE_", removerlo para obtener el código real
                if (recibo.qrCode.startsWith("QR_CODE_")) {
                    recibo.qrCode.substring(8) // Remover "QR_CODE_"
                } else {
                    recibo.qrCode
                }
            }
            recibo.codigoConfirmacion.isNotEmpty() -> recibo.codigoConfirmacion
            else -> recibo.bookingId // Usar bookingId como último recurso
        }
        
        if (qrData.isNotEmpty()) {
            val qrBitmap = generarQRCode(qrData)
            if (qrBitmap != null) {
                imgQR.setImageBitmap(qrBitmap)
                imgQR.visibility = android.view.View.VISIBLE
            } else {
                android.util.Log.e("VoucherActivity", "No se pudo generar el QR para: $qrData")
                imgQR.visibility = android.view.View.GONE
            }
        } else {
            android.util.Log.w("VoucherActivity", "No hay datos para generar el QR")
            imgQR.visibility = android.view.View.GONE
        }
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

    /**
     * Genera un código QR como Bitmap a partir de un string.
     * Equivalente a generarQRCode(data): Bitmap del diagrama UML.
     */
    private fun generarQRCode(data: String): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            android.util.Log.e("VoucherActivity", "Error al generar QR: ${e.message}", e)
            null
        }
    }
}

