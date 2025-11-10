package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.R
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.grupo4.appreservas.controller.ControlDetalleDestino
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinoService
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var txtCuposDisponibles: TextView
    private lateinit var btnReservar: Button

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

    override fun onResume() {
        super.onResume()
        // Actualizar cupos disponibles al volver a la actividad
        // Esto asegura que se muestren los datos más recientes desde SQLite
        if (::destino.isInitialized && destinoId != null) {
            mostrarCuposDisponibles()
        }
    }

    private fun inicializarDependencias() {
        val destinoRepo = DestinoRepository.getInstance(this)
        val bookingRepo = ReservasRepository.getInstance(this)
        val destinoService = DestinoService(destinoRepo)
        val availabilityService = AvailabilityService(destinoRepo, bookingRepo)
        
        controlDetalleDestino = ControlDetalleDestino(destinoService, availabilityService)
    }

    /**
     * Obtiene el destino desde el intent o desde la base de datos.
     * Prioriza obtener por ID desde SQLite (más eficiente y actualizado).
     */
    private fun obtenerDestino(): Boolean {
        try {
            // Prioridad 1: Intentar obtener por ID desde SQLite (método preferido)
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
            
            // Prioridad 2: Compatibilidad con código existente (objeto serializado)
            val destinoExtra = intent.getSerializableExtra("DESTINO") as? Destino
            if (destinoExtra != null) {
                destino = destinoExtra
                destinoId = destino.id
                // Si el destino viene serializado, verificar que existe en la BD
                // y actualizar con los datos más recientes
                val destinoActualizado = controlDetalleDestino.cargarDetalle(destino.id)
                if (destinoActualizado != null) {
                    destino = destinoActualizado
                }
                return true
            }
            
            // Error: No se proporcionó ni ID ni objeto destino
            mostrarErrorDestino("No se proporcionó información del destino")
            return false
            
        } catch (e: Exception) {
            // Manejo de excepciones inesperadas
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
        // Esperar un momento antes de cerrar para que el usuario vea el mensaje
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
        txtCuposDisponibles = findViewById(R.id.txtCuposDisponibles)
        btnReservar = findViewById(R.id.btnReservar)

        btnReservar.setOnClickListener {
            abrirReserva()
        }
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

            // Cupos disponibles - usar el controlador para obtener disponibilidad real
            mostrarCuposDisponibles()

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
                    // Si falla la carga de la imagen, se mantiene el estado por defecto
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

    /**
     * Muestra los cupos disponibles usando el controlador.
     * Equivalente a mostrarCupos(cupos) del diagrama UML.
     * Se actualiza automáticamente al volver a la actividad (onResume).
     */
    private fun mostrarCuposDisponibles() {
        if (!::destino.isInitialized || destinoId == null) {
            return
        }
        
        try {
            // Obtener fecha actual para consultar disponibilidad
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val disponibilidad = controlDetalleDestino.obtenerCupos(destino.id, fechaActual)
            
            if (disponibilidad != null) {
                val cuposDisponibles = disponibilidad["cuposDisponibles"] as? Int ?: destino.maxPersonas
                val cuposTotales = disponibilidad["cuposTotales"] as? Int ?: destino.maxPersonas
                val disponible = disponibilidad["disponible"] as? Boolean ?: true
                
                if (disponible && cuposDisponibles > 0) {
                    txtCuposDisponibles.text = "$cuposDisponibles cupos disponibles de $cuposTotales"
                    txtCuposDisponibles.setTextColor(getColor(R.color.color_success_fg)) // Verde si hay disponibilidad
                    btnReservar.isEnabled = true
                    btnReservar.alpha = 1.0f
                } else {
                    txtCuposDisponibles.text = "Sin cupos disponibles"
                    txtCuposDisponibles.setTextColor(getColor(R.color.color_price)) // Naranja si no hay disponibilidad
                    btnReservar.isEnabled = false
                    btnReservar.alpha = 0.5f
                }
            } else {
                // Si no se puede obtener disponibilidad, mostrar capacidad máxima como fallback
                txtCuposDisponibles.text = "${destino.maxPersonas} cupos disponibles (estimado)"
                txtCuposDisponibles.setTextColor(getColor(R.color.color_text_muted))
            }
        } catch (e: Exception) {
            // Manejo de errores al consultar disponibilidad
            txtCuposDisponibles.text = "Error al consultar disponibilidad"
            txtCuposDisponibles.setTextColor(getColor(R.color.color_text_muted))
            e.printStackTrace()
        }
    }

    private fun abrirReserva() {
        val intent = Intent(this, ReservasActivity::class.java)
        // Pasar solo el ID en lugar del objeto completo (más eficiente y datos actualizados)
        intent.putExtra("DESTINO_ID", destino.id)
        
        // Pasar USUARIO_ID si está disponible en el Intent
        val usuarioId = getIntent().getIntExtra("USUARIO_ID", 0)
        if (usuarioId > 0) {
            intent.putExtra("USUARIO_ID", usuarioId)
        }
        
        startActivity(intent)
    }
}