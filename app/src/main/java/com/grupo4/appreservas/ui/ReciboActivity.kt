package com.grupo4.appreservas.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.grupo4.appreservas.R
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.RepositorioRecompensas
import com.grupo4.appreservas.modelos.Recibo
import com.grupo4.appreservas.modelos.PuntosUsuario
import com.grupo4.appreservas.service.QRService
import com.grupo4.appreservas.service.ReciboService
import java.text.SimpleDateFormat
import java.util.*

class ReciboActivity : AppCompatActivity() {

    private lateinit var reciboService: ReciboService
    private lateinit var qrService: QRService

    private lateinit var btnBack: ImageView
    private lateinit var txtCodigoConfirmacion: TextView
    private lateinit var txtDestinoNombre: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtHora: TextView
    private lateinit var txtNumPersonas: TextView
    private lateinit var txtMontoTotal: TextView
    private lateinit var txtMetodoPago: TextView
    private lateinit var imgQR: ImageView
    private lateinit var btnVerMisReservas: Button
    private lateinit var txtPuntosGanados: TextView
    private lateinit var cardPuntosGanados: com.google.android.material.card.MaterialCardView

    private var bookingId: String = ""
    private var usuarioId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)

        obtenerBookingId()
        inicializarDependencias()
        inicializarVistas()
        cargarVoucher()
    }

    private fun obtenerBookingId() {
        bookingId = intent.getStringExtra("BOOKING_ID") ?: ""

        if (bookingId.isEmpty()) {
            Toast.makeText(this, "Error al cargar comprobante", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun inicializarDependencias() {
        val bookingRepo = ReservasRepository.getInstance(this)
        qrService = QRService()
        reciboService = ReciboService(bookingRepo, qrService)
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBack)
        txtCodigoConfirmacion = findViewById(R.id.txtCodigoConfirmacion)
        txtDestinoNombre = findViewById(R.id.txtDestinoNombre)
        txtFecha = findViewById(R.id.txtFecha)
        txtHora = findViewById(R.id.txtHora)
        txtNumPersonas = findViewById(R.id.txtNumPersonas)
        txtMontoTotal = findViewById(R.id.txtMontoTotal)
        txtMetodoPago = findViewById(R.id.txtMetodoPago)
        imgQR = findViewById(R.id.imgQR)
        btnVerMisReservas = findViewById(R.id.btnVerMisReservas)
        txtPuntosGanados = findViewById(R.id.txtPuntosGanados)
        cardPuntosGanados = findViewById(R.id.cardPuntosGanados)

        btnBack.setOnClickListener {
            finish()
        }

        btnVerMisReservas.setOnClickListener {
            // Navegar a mis reservas (perfil)
            if (usuarioId > 0) {
                val intent = Intent(this, RecompensasActivity::class.java)
                intent.putExtra("USUARIO_ID", usuarioId)
                startActivity(intent)
            }
            finish()
        }
    }

    private fun cargarVoucher() {
        try {
            val voucher = reciboService.emitir(bookingId)

            if (voucher != null) {
                // Obtener usuarioId de la reserva
                val reserva = ReservasRepository.getInstance(this).find(bookingId)
                usuarioId = reserva?.usuarioId ?: 0
                
                mostrarVoucher(voucher)
                mostrarPuntosGanados()
            } else {
                Toast.makeText(this, "Error al generar comprobante", Toast.LENGTH_SHORT).show()
                finish()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Muestra los puntos ganados por completar la reserva (HU-007).
     */
    private fun mostrarPuntosGanados() {
        try {
            val puntosGanados = PuntosUsuario.PUNTOS_POR_RESERVA
            txtPuntosGanados.text = "+$puntosGanados"
            cardPuntosGanados.visibility = android.view.View.VISIBLE
        } catch (e: Exception) {
            android.util.Log.e("ReciboActivity", "Error al mostrar puntos: ${e.message}", e)
            cardPuntosGanados.visibility = android.view.View.GONE
        }
    }

    private fun mostrarVoucher(recibo: Recibo) {
        txtCodigoConfirmacion.text = recibo.codigoConfirmacion
        txtDestinoNombre.text = recibo.destinoNombre

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        txtFecha.text = dateFormat.format(recibo.fecha)

        // Asumiendo que el voucher tiene horaInicio
        //txtHora.text = voucher.horaInicio ?: "08:00"

        txtNumPersonas.text = "${recibo.numPersonas} persona${if (recibo.numPersonas > 1) "s" else ""}"
        txtMontoTotal.text = "S/ ${recibo.montoTotal.toInt()}"
        txtMetodoPago.text = recibo.metodoPago
        txtHora.text = recibo.horaInicio

        // Mostrar QR usando el código que viene del servicio
        // El QRService ya generó el código, aquí lo renderizamos visualmente
        mostrarQR(recibo.qrCode)
    }

    /**
     * Renderiza el código QR en la vista.
     * Equivalente a showQR(code) del diagrama UML VoucherView.
     * 
     * @param qrCodeData Datos del QR generados por QRService (formato: "RESERVA:<reservaId>")
     */
    private fun mostrarQR(qrCodeData: String) {
        try {
            // El QR ya viene en el formato correcto desde QRService: "RESERVA:<reservaId>"
            // Generar el bitmap QR usando ZXing para renderización visual
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                qrCodeData, // Usar directamente el código QR generado
                BarcodeFormat.QR_CODE,
                512,
                512
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            imgQR.setImageBitmap(bitmap)

        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar QR: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}