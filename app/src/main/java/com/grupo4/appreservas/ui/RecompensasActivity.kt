package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.HistorialViajesAdapter
import com.grupo4.appreservas.adapter.LogrosAdapter
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.viewmodel.RecompensasViewModel

/**
 * Activity de Recompensas según HU-007 y diagrama UML.
 * Equivalente a RecompensasActivity del diagrama.
 * 
 * Usa arquitectura MVVM con RecompensasViewModel.
 */
class RecompensasActivity : AppCompatActivity() {

    private lateinit var viewModel: RecompensasViewModel
    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvNivelUsuario: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var viewProgressBar: View
    private lateinit var frameProgressBar: ViewGroup
    private lateinit var tvPuntosParaSiguiente: TextView
    private lateinit var tvToursCompletados: TextView
    private lateinit var tvLogrosDesbloqueados: TextView
    private lateinit var recyclerLogros: RecyclerView
    private lateinit var recyclerHistorialViajes: RecyclerView
    private lateinit var btnCanjearPuntos: Button
    private lateinit var tvVolverInicio: TextView
    private lateinit var btnCerrar: ImageView
    private lateinit var logrosAdapter: LogrosAdapter
    private lateinit var historialAdapter: HistorialViajesAdapter

    private var usuarioId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recompensas)

        obtenerDatosUsuario()
        inicializarViewModel()
        inicializarVistas()
        configurarObservers()
        cargarDatos()
    }

    private fun obtenerDatosUsuario() {
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        if (usuarioId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun inicializarViewModel() {
        viewModel = ViewModelProvider(this)[RecompensasViewModel::class.java]
    }

    private fun inicializarVistas() {
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
        tvNivelUsuario = findViewById(R.id.tvNivelUsuario)
        tvPuntos = findViewById(R.id.tvPuntos)
        viewProgressBar = findViewById(R.id.viewProgressBar)
        frameProgressBar = findViewById(R.id.frameProgressBar)
        tvPuntosParaSiguiente = findViewById(R.id.tvPuntosParaSiguiente)
        tvToursCompletados = findViewById(R.id.tvToursCompletados)
        tvLogrosDesbloqueados = findViewById(R.id.tvLogrosDesbloqueados)
        recyclerLogros = findViewById(R.id.recyclerLogros)
        recyclerHistorialViajes = findViewById(R.id.recyclerHistorialViajes)
        btnCanjearPuntos = findViewById(R.id.btnCanjearPuntos)
        tvVolverInicio = findViewById(R.id.tvVolverInicio)
        btnCerrar = findViewById(R.id.btnCerrar)

        // Configurar botón cerrar
        btnCerrar.setOnClickListener {
            finish()
        }

        // Configurar RecyclerViews
        recyclerLogros.layoutManager = LinearLayoutManager(this)
        logrosAdapter = LogrosAdapter()
        recyclerLogros.adapter = logrosAdapter

        recyclerHistorialViajes.layoutManager = LinearLayoutManager(this)
        historialAdapter = HistorialViajesAdapter()
        recyclerHistorialViajes.adapter = historialAdapter

        // Cargar nombre de usuario
        val dbHelper = DatabaseHelper(this)
        val usuario = dbHelper.buscarUsuarioPorId(usuarioId)
        tvNombreUsuario.text = usuario?.nombreCompleto ?: "Usuario"

        // Configurar listeners
        btnCanjearPuntos.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de canje próximamente", Toast.LENGTH_SHORT).show()
        }

        tvVolverInicio.setOnClickListener {
            finish()
        }
    }

    private fun configurarObservers() {
        // Observar puntos
        viewModel.puntos.observe(this) { puntos ->
            tvPuntos.text = puntos.toString()
        }

        // Observar información completa de puntos
        viewModel.puntosUsuario.observe(this) { puntosUsuario ->
            tvPuntos.text = puntosUsuario.puntosAcumulados.toString()
            tvNivelUsuario.text = puntosUsuario.nivel
            
            // Calcular progreso (puntos actuales vs puntos para siguiente nivel)
            val puntosActuales = puntosUsuario.puntosAcumulados
            val puntosSiguienteNivel = when (puntosUsuario.nivel) {
                "Explorador" -> 501
                "Explorador Experto" -> 1501
                "Viajero Profesional" -> 3001
                else -> 3001
            }
            val puntosNivelActual = when (puntosUsuario.nivel) {
                "Explorador" -> 0
                "Explorador Experto" -> 501
                "Viajero Profesional" -> 1501
                else -> 3001
            }
            val progreso = puntosActuales - puntosNivelActual
            val maxProgreso = puntosSiguienteNivel - puntosNivelActual
            
            // Actualizar barra de progreso personalizada
            frameProgressBar.post {
                if (maxProgreso > 0 && frameProgressBar.width > 0) {
                    val porcentaje = (progreso.toFloat() / maxProgreso.toFloat()).coerceIn(0f, 1f)
                    val progressWidth = (frameProgressBar.width * porcentaje).toInt()
                    
                    val layoutParams = viewProgressBar.layoutParams
                    layoutParams.width = progressWidth.coerceAtLeast(0)
                    viewProgressBar.layoutParams = layoutParams
                    viewProgressBar.visibility = View.VISIBLE
                } else {
                    val layoutParams = viewProgressBar.layoutParams
                    layoutParams.width = 0
                    viewProgressBar.layoutParams = layoutParams
                    viewProgressBar.visibility = View.VISIBLE
                }
            }
            
            tvPuntosParaSiguiente.text = "${puntosUsuario.puntosParaSiguienteNivel} puntos para el próximo nivel"
        }

        // Observar tours completados
        viewModel.toursCompletados.observe(this) { numTours ->
            tvToursCompletados.text = numTours.toString()
        }

        // Observar logros
        viewModel.logros.observe(this) { logros ->
            logrosAdapter.actualizarLista(logros)
            val logrosDesbloqueados = logros.count { it.desbloqueado }
            tvLogrosDesbloqueados.text = logrosDesbloqueados.toString()
        }
    }

    private fun cargarDatos() {
        // Cargar logros y puntos
        viewModel.cargarLogros(usuarioId)
        
        // Cargar historial de viajes
        cargarHistorialViajes()
    }

    private fun cargarHistorialViajes() {
        try {
            val reservasRepository = ReservasRepository.getInstance(this)
            val reservas = reservasRepository.obtenerReservasUsuario(usuarioId.toString())
            historialAdapter.actualizarLista(reservas)
        } catch (e: Exception) {
            android.util.Log.e("RecompensasActivity", "Error al cargar historial: ${e.message}", e)
        }
    }

    /**
     * Muestra los puntos del usuario.
     * Equivalente a mostrarPuntos(puntos) del diagrama UML.
     */
    private fun mostrarPuntos(puntos: Int) {
        tvPuntos.text = puntos.toString()
    }

    /**
     * Muestra los logros del usuario.
     * Equivalente a mostrarLogros(logros) del diagrama UML.
     */
    private fun mostrarLogros(logros: List<com.grupo4.appreservas.modelos.Logro>) {
        logrosAdapter.actualizarLista(logros)
    }

    /**
     * Actualiza la vista con los datos más recientes.
     * Equivalente a actualizarVista() del diagrama UML.
     */
    private fun actualizarVista() {
        viewModel.cargarLogros(usuarioId)
        cargarHistorialViajes()
    }
}

