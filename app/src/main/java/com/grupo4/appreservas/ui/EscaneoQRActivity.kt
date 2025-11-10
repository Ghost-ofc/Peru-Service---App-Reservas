package com.grupo4.appreservas.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.grupo4.appreservas.R
import com.grupo4.appreservas.controller.ControlCheckIn
import com.grupo4.appreservas.repository.RepositorioCheckIn
import com.grupo4.appreservas.repository.RepositorioQR
import com.grupo4.appreservas.service.QRService
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * Activity de Escaneo QR según el diagrama UML.
 * Equivalente a EscaneoQRActivity del diagrama.
 * 
 * En arquitectura MVC, esta Activity (Vista) usa el ControlCheckIn (Controller)
 * para manejar la lógica de validación y check-in.
 */
class EscaneoQRActivity : AppCompatActivity() {

    private lateinit var controlCheckIn: ControlCheckIn
    private lateinit var qrService: QRService
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var tvResultado: TextView
    private lateinit var tourId: String
    private var guiaId: Int = 1 // Por defecto, se puede obtener de la sesión

    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escaneo_qr)

        inicializarDependencias()
        inicializarVistas()
        verificarPermisos()
    }

    private fun inicializarDependencias() {
        // Inicializar repositorios y controlador según arquitectura MVC
        val repositorioQR = RepositorioQR(this)
        val repositorioCheckIn = RepositorioCheckIn(this)
        controlCheckIn = ControlCheckIn(repositorioQR, repositorioCheckIn)
        qrService = QRService()
        
        tourId = intent.getStringExtra("TOUR_ID") ?: ""
        guiaId = intent.getIntExtra("GUIA_ID", 1)
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

    /**
     * Inicia la cámara para escanear códigos QR.
     * Equivalente a iniciarCamara() del diagrama UML.
     */
    private fun iniciarCamara() {
        try {
            // Configurar el scanner para dispositivos físicos
            val settings = barcodeView.barcodeView.cameraSettings
            settings.isAutoFocusEnabled = true
            settings.isContinuousFocusEnabled = true
            // -1 = cámara trasera por defecto, 0 = frontal
            settings.requestedCameraId = -1
            
            // Iniciar el escaneo continuo con manejo de errores
            barcodeView.decodeContinuous(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    result?.text?.let { codigo ->
                        // Pausar el scanner mientras se procesa para evitar múltiples escaneos
                        barcodeView.pause()
                        // Procesar el código en el hilo principal
                        runOnUiThread {
                            enviarCodigo(codigo)
                        }
                    }
                }
                
                override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
                    // Opcional: dibujar puntos de resultado en la vista
                }
            })
            
            // Dar tiempo para que la cámara se inicialice completamente (especialmente en dispositivos físicos)
            barcodeView.postDelayed({
                try {
                    barcodeView.resume()
                } catch (e: Exception) {
                    android.util.Log.e("EscaneoQRActivity", "Error al reanudar scanner: ${e.message}", e)
                }
            }, 500)
            
        } catch (e: Exception) {
            android.util.Log.e("EscaneoQRActivity", "Error al iniciar cámara: ${e.message}", e)
            Toast.makeText(this, "Error al iniciar la cámara. Por favor, verifica los permisos y reinicia la aplicación.", Toast.LENGTH_LONG).show()
            // Intentar cerrar la actividad si no se puede iniciar la cámara
            finish()
        }
    }

    /**
     * Envía el código QR escaneado para procesamiento.
     * Equivalente a enviarCodigo(codigo) del diagrama UML.
     * 
     * @param codigoQR Código QR escaneado (formato: "RESERVA:<reservaId>" o código antiguo)
     */
    private fun enviarCodigo(codigoQR: String) {
        barcodeView.pause()

        // Extraer el reservaId del QR si está en el formato nuevo
        // Si no, usar el código directamente (formato antiguo para compatibilidad)
        val codigo = qrService.extraerReservaId(codigoQR) ?: codigoQR

        // Usar ControlCheckIn para procesar el escaneo (patrón MVC)
        // El código puede ser el reservaId (formato nuevo) o el códigoQR (formato antiguo)
        val resultado = controlCheckIn.procesarEscaneoQR(codigo, tourId, guiaId)

        when (resultado) {
            is ControlCheckIn.ResultadoEscaneo.Exito -> {
                val reserva = resultado.reserva
                val nombreTour = reserva.destino?.nombre ?: "Tour"
                val horaRegistro = reserva.horaRegistro ?: obtenerHoraActual()
                
                mostrarResultado(
                    "¡Check-in Confirmado!",
                    "El turista ha sido registrado exitosamente\n\n" +
                            "Nombre: ${reserva.nombreTurista}\n" +
                            "Documento: ${reserva.documento}\n" +
                            "Tour: $nombreTour\n" +
                            "Hora de registro: $horaRegistro",
                    true
                )
            }
            is ControlCheckIn.ResultadoEscaneo.Error -> {
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

    /**
     * Muestra el resultado del escaneo QR.
     * Equivalente a mostrarResultado(mensaje) del diagrama UML.
     * 
     * @param titulo Título del resultado
     * @param mensaje Mensaje detallado
     * @param exito true si fue exitoso, false si hubo error
     */
    private fun mostrarResultado(titulo: String, mensaje: String, exito: Boolean) {
        // Mostrar diálogo según las interfaces proporcionadas
        val builder = AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Aceptar") { _, _ ->
                // Reanudar escaneo después de cerrar el diálogo
                barcodeView.postDelayed({
                    barcodeView.resume()
                }, 500)
            }

        if (exito) {
            builder.setIcon(android.R.drawable.ic_dialog_info)
        } else {
            builder.setIcon(android.R.drawable.ic_dialog_alert)
        }

        builder.show()
    }

    private fun obtenerHoraActual(): String {
        // Usar el mismo formato que la base de datos: "yyyy-MM-dd HH:mm:ss"
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return formato.format(java.util.Date())
    }

    override fun onResume() {
        super.onResume()
        try {
            // Verificar permisos antes de reanudar
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                barcodeView.resume()
            }
        } catch (e: Exception) {
            android.util.Log.e("EscaneoQRActivity", "Error al reanudar scanner: ${e.message}", e)
            Toast.makeText(this, "Error al reanudar el escáner", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            barcodeView.pause()
        } catch (e: Exception) {
            android.util.Log.e("EscaneoQRActivity", "Error al pausar scanner: ${e.message}", e)
        }
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