package com.grupo4.appreservas.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.grupo4.appreservas.R
import com.grupo4.appreservas.viewmodel.CheckInViewModel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class EscaneoQRActivity : AppCompatActivity() {

    private lateinit var checkInViewModel: CheckInViewModel
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var tvResultado: TextView
    private lateinit var tourId: String

    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escaneo_qr)

        inicializarDependencias()
        inicializarVistas()
        verificarPermisos()
    }

    private fun inicializarDependencias() {
        checkInViewModel = CheckInViewModel(this)
        tourId = intent.getStringExtra("TOUR_ID") ?: ""
    }

    private fun inicializarVistas() {
        barcodeView = findViewById(R.id.barcode_scanner)
        tvResultado = findViewById(R.id.tv_resultado)
    }

    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            iniciarCamara()
        }
    }

    private fun iniciarCamara() {
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let { codigo ->
                    enviarCodigo(codigo)
                }
            }
        })
    }

    private fun enviarCodigo(codigo: String) {
        barcodeView.pause()

        when (val resultado = checkInViewModel.procesarEscaneoQR(codigo, tourId)) {
            is CheckInViewModel.ResultadoEscaneo.Exito -> {
                mostrarResultado(
                    "¡Check-in Confirmado!",
                    "El turista ha sido registrado exitosamente\n\n" +
                            "Nombre: ${resultado.reserva.nombreTurista}\n" +
                            "Documento: ${resultado.reserva.documento}\n" +
                            "Tour: Machu Picchu Tour Completo\n" +
                            "Hora de registro: ${resultado.reserva.horaRegistro}",
                    true
                )
            }
            is CheckInViewModel.ResultadoEscaneo.Error -> {
                mostrarResultado(
                    "QR No Válido",
                    resultado.mensaje + "\n\nPosibles causas:\n" +
                            "• El turista no pertenece a este grupo\n" +
                            "• El código QR ya fue utilizado\n" +
                            "• El código QR está dañado o es ilegible\n" +
                            "• La reserva fue cancelada",
                    false
                )
            }
        }
    }

    private fun mostrarResultado(titulo: String, mensaje: String, exito: Boolean) {
        // Aquí mostrarías un diálogo personalizado o navegarías a otra pantalla
        Toast.makeText(this, "$titulo: $mensaje", Toast.LENGTH_LONG).show()

        // Reanudar escaneo después de 3 segundos
        barcodeView.postDelayed({
            barcodeView.resume()
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCamara()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}