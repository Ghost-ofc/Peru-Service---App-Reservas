package com.grupo4.appreservas.ui

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
import com.grupo4.appreservas.modelos.Voucher
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

    private var bookingId: String = ""

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
        val bookingRepo = ReservasRepository.getInstance()
        reciboService = ReciboService(bookingRepo)
        qrService = QRService()
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

        btnBack.setOnClickListener {
            finish()
        }

        btnVerMisReservas.setOnClickListener {
            // Navegar a mis reservas
            finish()
        }
    }

    private fun cargarVoucher() {
        try {
            val voucher = reciboService.emitir(bookingId)

            if (voucher != null) {
                mostrarVoucher(voucher)
            } else {
                Toast.makeText(this, "Error al generar comprobante", Toast.LENGTH_SHORT).show()
                finish()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun mostrarVoucher(voucher: Voucher) {
        txtCodigoConfirmacion.text = voucher.codigoConfirmacion
        txtDestinoNombre.text = voucher.destinoNombre

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        txtFecha.text = dateFormat.format(voucher.fecha)

        // Asumiendo que el voucher tiene horaInicio
        //txtHora.text = voucher.horaInicio ?: "08:00"

        txtNumPersonas.text = "${voucher.numPersonas} persona${if (voucher.numPersonas > 1) "s" else ""}"
        txtMontoTotal.text = "S/ ${voucher.montoTotal.toInt()}"
        txtMetodoPago.text = voucher.metodoPago

        // Generar y mostrar QR
        generarQR(voucher.codigoConfirmacion)
    }

    private fun generarQR(codigo: String) {
        try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                codigo,
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
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
        }
    }
}