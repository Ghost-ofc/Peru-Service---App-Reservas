package com.grupo4.appreservas.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.grupo4.appreservas.controller.ControlDetalleDestino
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.R
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.service.DestinoService

/**
 * Activity para mostrar el detalle de un destino turístico.
 * Corresponde a la HU: Detalle del destino.
 */
class DetalleDestinoActivity : AppCompatActivity() {

    private lateinit var controlDetalleDestino: ControlDetalleDestino
    private lateinit var destino: Destino
    private var destinoId: String? = null

    private lateinit var imgDestino: ImageView
    private lateinit var txtNombreHeader: TextView
    private lateinit var txtPrecio: TextView
    private lateinit var txtDuracion: TextView
    private lateinit var txtMaxPersonas: TextView
    private lateinit var txtCalificacion: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtIncluye: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetalle)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        inicializarDependencias()
        
        if (!obtenerDestino()) {
            return
        }
        inicializarVistas()
        mostrarDetalle()
    }

    private fun inicializarDependencias() {
        val repository = PeruvianServiceRepository.getInstance(this)
        val destinoService = DestinoService(repository)
        controlDetalleDestino = ControlDetalleDestino(destinoService)
    }

    /**
     * Obtiene el destino desde el intent.
     */
    private fun obtenerDestino(): Boolean {
        try {
            val idDestino = intent.getStringExtra("DESTINO_ID")
            if (idDestino != null && idDestino.isNotEmpty()) {
                destinoId = idDestino
                destino = controlDetalleDestino.cargarDetalle(idDestino)
                    ?: run {
                        mostrarErrorDestino("El destino solicitado no existe en la base de datos")
                        return false
                    }
                return true
            }
            
            mostrarErrorDestino("No se proporcionó información del destino")
            return false
            
        } catch (e: Exception) {
            mostrarErrorDestino("Error al cargar destino: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Muestra un mensaje de error y cierra la actividad.
     */
    private fun mostrarErrorDestino(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
        window.decorView.rootView.postDelayed({
            finish()
        }, 2000)
    }

    private fun inicializarVistas() {
        imgDestino = findViewById(R.id.imgDestino)
        txtNombreHeader = findViewById(R.id.txtNombreHeader)
        txtPrecio = findViewById(R.id.txtPrecio)
        txtDuracion = findViewById(R.id.txtDuracion)
        txtMaxPersonas = findViewById(R.id.txtMaxPersonas)
        txtCalificacion = findViewById(R.id.txtCalificacion)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtIncluye = findViewById(R.id.txtIncluye)
        
        // Configurar botón reservar
        val btnReservar = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReservar)
        btnReservar?.setOnClickListener {
            abrirReserva()
        }
    }
    
    private fun abrirReserva() {
        val intent = android.content.Intent(this, ReservasActivity::class.java)
        intent.putExtra("DESTINO_ID", destino.id)
        startActivity(intent)
    }

    private fun mostrarDetalle() {
        try {
            // Título sobre la imagen
            txtNombreHeader.text = destino.nombre

            // Precio
            txtPrecio.text = "S/ ${String.format("%.2f", destino.precio)}"

            // Información básica
            txtDuracion.text = "${destino.duracionHoras} horas"
            txtMaxPersonas.text = "Máx. ${destino.maxPersonas} personas"
            txtCalificacion.text = "${destino.calificacion}/5 (${destino.numReseñas} reseñas)"

            // Descripción
            txtDescripcion.text = destino.descripcion.ifEmpty { "Sin descripción disponible" }

            // Qué incluye con checkmarks
            val incluyeTexto = if (destino.incluye.isNotEmpty()) {
                destino.incluye.joinToString("\n") { "✓ $it" }
            } else {
                "Sin información adicional"
            }
            txtIncluye.text = incluyeTexto

            // Cargar imagen con Glide (con manejo de errores)
            if (destino.imagenUrl.isNotEmpty()) {
                try {
                    Glide.with(this)
                        .load(destino.imagenUrl)
                        .centerCrop()
                        .into(imgDestino)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al mostrar detalles: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }
}
