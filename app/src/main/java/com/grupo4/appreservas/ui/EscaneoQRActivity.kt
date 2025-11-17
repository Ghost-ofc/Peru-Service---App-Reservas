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
     * Equivalente a enviarCodigoQR(codigoReserva) del diagrama UML.
     */
    private fun enviarCodigoQR(codigoReserva: String) {
        barcodeView.pause()
        viewModel.procesarEscaneoQR(codigoReserva, tourId, usuarioId)
    }

    private fun observarViewModel() {
        viewModel.resultadoEscaneo.observe(this) { resultado ->
            resultado?.let {
                mostrarResultado(it)
                
                // Si el resultado es exitoso, esperar un momento y volver
                if (it == "Asistencia confirmada") {
                    barcodeView.postDelayed({
                        finish()
                    }, 2000)
                } else {
                    // Si hay error, permitir escanear de nuevo después de un momento
                    barcodeView.postDelayed({
                        escaneoCompletado = false
                        barcodeView.resume()
                    }, 3000)
                }
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                mostrarResultado("Error: $it")
                // Permitir escanear de nuevo después de un momento
                barcodeView.postDelayed({
                    escaneoCompletado = false
                    barcodeView.resume()
                }, 3000)
            }
        }
    }

    /**
     * Muestra el resultado del escaneo.
     * Equivalente a mostrarResultado(mensaje) del diagrama UML.
     */
    private fun mostrarResultado(mensaje: String) {
        tvResultado.text = mensaje
        
        // Cambiar color según el resultado
        when {
            mensaje == "Asistencia confirmada" -> {
                tvResultado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            }
            mensaje.contains("no válido") || mensaje.contains("ya registrado") -> {
                tvResultado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            }
            else -> {
                tvResultado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            }
        }
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

