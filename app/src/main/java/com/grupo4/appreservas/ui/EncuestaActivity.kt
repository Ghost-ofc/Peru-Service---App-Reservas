package com.grupo4.appreservas.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.viewmodel.EncuestaViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para mostrar y enviar encuestas de satisfacción.
 * Equivalente a EncuestaActivity del diagrama UML.
 */
class EncuestaActivity : AppCompatActivity() {

    private var tourId: String = ""
    private var usuarioId: Int = 0
    private lateinit var viewModel: EncuestaViewModel
    private lateinit var btnCerrar: ImageView
    private lateinit var tvTourNombre: TextView
    private lateinit var tvFechaTour: TextView
    private lateinit var etComentario: EditText
    private lateinit var tvContadorCaracteres: TextView
    private lateinit var btnEnviar: TextView
    private lateinit var btnResponderMasTarde: TextView
    private lateinit var estrellas: List<ImageView>
    
    private var calificacionSeleccionada: Int = 0
    private val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encuesta)

        tourId = intent.getStringExtra("TOUR_ID") ?: ""
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)

        if (tourId.isEmpty() || usuarioId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del tour", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(EncuestaViewModel::class.java)

        inicializarVistas()
        configurarListeners()
        observarViewModel()
        cargarEncuesta()
    }

    private fun inicializarVistas() {
        btnCerrar = findViewById(R.id.btnCerrar)
        tvTourNombre = findViewById(R.id.tvTourNombre)
        tvFechaTour = findViewById(R.id.tvFechaTour)
        etComentario = findViewById(R.id.etComentario)
        tvContadorCaracteres = findViewById(R.id.tvContadorCaracteres)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnResponderMasTarde = findViewById(R.id.btnResponderMasTarde)

        // Inicializar estrellas
        estrellas = listOf(
            findViewById(R.id.estrella1),
            findViewById(R.id.estrella2),
            findViewById(R.id.estrella3),
            findViewById(R.id.estrella4),
            findViewById(R.id.estrella5)
        )

        // Configurar contador de caracteres
        etComentario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val longitud = s?.length ?: 0
                tvContadorCaracteres.text = "$longitud/500 caracteres"
            }
        })
    }

    private fun configurarListeners() {
        btnCerrar.setOnClickListener {
            finish()
        }

        btnResponderMasTarde.setOnClickListener {
            finish()
        }

        // Configurar click en estrellas
        estrellas.forEachIndexed { index, estrella ->
            estrella.setOnClickListener {
                seleccionarCalificacion(index + 1)
            }
        }

        btnEnviar.setOnClickListener {
            if (calificacionSeleccionada > 0) {
                enviarRespuesta()
            } else {
                Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observarViewModel() {
        viewModel.tour.observe(this) { tour ->
            tour?.let {
                mostrarFormulario(it)
            }
        }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            mensaje?.let {
                mostrarMensaje(it)
            }
        }

        viewModel.encuestaEnviada.observe(this) { enviada ->
            if (enviada) {
                mostrarPantallaCompletada(tourId)
            }
        }

        viewModel.respuestaEncuesta.observe(this) { respuesta ->
            respuesta?.let {
                // Mostrar confirmación con los puntos ganados
                mostrarConfirmacion("¡Gracias por tu opinión! Has ganado ${it.puntosOtorgados} puntos")
            }
        }
    }

    private fun cargarEncuesta() {
        solicitarFormulario(tourId)
    }

    /**
     * Solicita el formulario de encuesta para un tour.
     * Equivalente a solicitarFormulario(idTour) del diagrama UML.
     */
    private fun solicitarFormulario(idTour: String) {
        viewModel.cargarEncuesta(idTour)
    }

    /**
     * Muestra el formulario de encuesta con la información del tour.
     * Equivalente a mostrarFormulario(calificacion, comentario) del diagrama UML.
     */
    private fun mostrarFormulario(tour: Tour) {
        tvTourNombre.text = tour.nombre
        
        // Formatear fecha del tour
        try {
            val fechaTour = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tour.fecha)
            if (fechaTour != null) {
                tvFechaTour.text = dateFormat.format(fechaTour)
            } else {
                tvFechaTour.text = tour.fecha
            }
        } catch (e: Exception) {
            tvFechaTour.text = tour.fecha
        }
    }

    /**
     * Selecciona una calificación (1-5 estrellas).
     */
    private fun seleccionarCalificacion(calificacion: Int) {
        calificacionSeleccionada = calificacion
        
        // Actualizar visualización de estrellas
        estrellas.forEachIndexed { index, estrella ->
            if (index < calificacion) {
                estrella.setImageResource(android.R.drawable.star_big_on)
            } else {
                estrella.setImageResource(android.R.drawable.star_big_off)
            }
        }

        // Mostrar texto de calificación
        val textoCalificacion = when (calificacion) {
            1 -> "Muy insatisfecho"
            2 -> "Insatisfecho"
            3 -> "Neutral"
            4 -> "Satisfecho"
            5 -> "Muy satisfecho"
            else -> "Selecciona una calificación"
        }
        findViewById<TextView>(R.id.tvTextoCalificacion).text = textoCalificacion
    }

    /**
     * Envía la respuesta de la encuesta.
     * Equivalente a enviarRespuesta(idTour, calificacion, comentario) del diagrama UML.
     */
    private fun enviarRespuesta() {
        val comentario = etComentario.text.toString().trim()
        viewModel.registrarRespuesta(tourId, usuarioId, calificacionSeleccionada, comentario)
        
        // Deshabilitar botón mientras se envía
        btnEnviar.isEnabled = false
        btnEnviar.text = "Enviando..."
    }

    /**
     * Muestra la pantalla de encuesta completada.
     * Equivalente a mostrarPantallaCompletada(idTour) del diagrama UML.
     */
    private fun mostrarPantallaCompletada(idTour: String) {
        // La pantalla de completada se muestra cuando encuestaEnviada es true
        // Esto se maneja en observarViewModel() con mostrarConfirmacion()
    }

    /**
     * Muestra un mensaje de confirmación.
     * Equivalente a mostrarConfirmacion(mensaje) del diagrama UML.
     */
    private fun mostrarConfirmacion(mensaje: String) {
        // Mostrar diálogo o pantalla de confirmación
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        
        // Esperar un momento y cerrar
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }

    /**
     * Muestra un mensaje al usuario.
     */
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        btnEnviar.isEnabled = true
        btnEnviar.text = "Enviar encuesta"
    }
}

