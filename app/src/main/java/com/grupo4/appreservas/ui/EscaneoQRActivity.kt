package com.grupo4.appreservas.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.CheckIn
import com.grupo4.appreservas.viewmodel.CheckInViewModel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * Activity para escanear códigos QR de reservas.
 * Equivalente a EscaneoQRActivity del diagrama UML.
 */
class EscaneoQRActivity : AppCompatActivity() {

    private var tourId: String = ""
    private var usuarioId: Int = 0
    private lateinit var viewModel: CheckInViewModel
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var tvResultado: TextView
    private var escaneoCompletado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escaneo_qr)

        tourId = intent.getStringExtra("TOUR_ID") ?: ""
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)

        if (tourId.isEmpty() || usuarioId == 0) {
            Toast.makeText(this, "Error: Información del tour no disponible", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(CheckInViewModel::class.java)

        inicializarVistas()
        configurarEscaneo()
        observarViewModel()
        
        // Solicitar permisos de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            iniciarCamara()
        }
    }

    private fun inicializarVistas() {
        barcodeView = findViewById(R.id.barcode_scanner)
        tvResultado = findViewById(R.id.tv_resultado)
    }

    private fun configurarEscaneo() {
        barcodeView.decodeContinuous(callback)
    }

    /**
     * Inicia la cámara para escanear.
     * Equivalente a iniciarCamara() del diagrama UML.
     */
    private fun iniciarCamara() {
        barcodeView.resume()
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            if (result == null || escaneoCompletado) {
                return
            }

            val codigoQR = result.text
            if (codigoQR.isNotEmpty()) {
                escaneoCompletado = true
                enviarCodigoQR(codigoQR)
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
            // No necesario para esta implementación
        }
    }

    /**
     * Envía el código QR escaneado para procesamiento.
     * Equivalente a enviarCodigoQRLeido(codigoQR) del diagrama UML.
     */
    private fun enviarCodigoQR(codigoQR: String) {
        barcodeView.pause()
        viewModel.procesarEscaneoQR(codigoQR, tourId, usuarioId)
    }

    private fun observarViewModel() {
        viewModel.resultadoCheckin.observe(this) { checkIn ->
            if (checkIn != null) {
                mostrarResultadoCheckin(checkIn)
                // Esperar un momento y volver
                barcodeView.postDelayed({
                    finish()
                }, 2000)
            }
        }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            mensaje?.let {
                mostrarMensajeError(it)
                // Permitir escanear de nuevo después de un momento
                barcodeView.postDelayed({
                    escaneoCompletado = false
                    barcodeView.resume()
                }, 3000)
            }
        }
    }

    /**
     * Muestra el resultado del check-in.
     * Equivalente a mostrarResultadoCheckin(checkin) del diagrama UML.
     */
    private fun mostrarResultadoCheckin(checkIn: CheckIn) {
        tvResultado.text = "Asistencia confirmada\nReserva: ${checkIn.reservaId}\nHora: ${checkIn.horaRegistro}"
        tvResultado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        Toast.makeText(this, "Check-in registrado exitosamente", Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra un mensaje de error.
     * Equivalente a mostrarMensajeError(mensaje) del diagrama UML.
     */
    private fun mostrarMensajeError(mensaje: String) {
        tvResultado.text = mensaje
        tvResultado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCamara()
            } else {
                Toast.makeText(this, "Se necesita permiso de cámara para escanear QR", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (!escaneoCompletado) {
                barcodeView.resume()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
    }
}

