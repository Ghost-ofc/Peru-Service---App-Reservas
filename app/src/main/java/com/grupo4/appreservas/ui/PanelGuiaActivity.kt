package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.ToursAdapter
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.viewmodel.TourDelDiaViewModel

/**
 * Activity para el panel de administrador/guía.
 * Muestra los tours asignados al guía del día.
 * Equivalente a PanelGuiaActivity del diagrama UML.
 */
class PanelGuiaActivity : AppCompatActivity() {

    private var usuarioId: Int = 0
    private lateinit var headerLayout: LinearLayout
    private lateinit var tvNombreGuia: TextView
    private lateinit var btnSalir: Button
    private lateinit var recyclerTours: RecyclerView
    private lateinit var viewModel: TourDelDiaViewModel
    private lateinit var toursAdapter: ToursAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_guia)

        usuarioId = intent.getIntExtra("USUARIO_ID", 0)

        if (usuarioId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(TourDelDiaViewModel::class.java)

        inicializarVistas()
        configurarSafeArea()
        cargarDatosUsuario()
        configurarRecyclerView()
        configurarListeners()
        observarViewModel()
        cargarToursDelDia()
    }

    private fun inicializarVistas() {
        headerLayout = findViewById(R.id.headerLayout)
        tvNombreGuia = findViewById(R.id.tv_nombre_guia)
        btnSalir = findViewById(R.id.btn_salir)
        recyclerTours = findViewById(R.id.recycler_tours)
    }
    
    /**
     * Configura el padding del header para respetar el notch y la barra de estado.
     * Esto asegura que los iconos de notificaciones y el botón salir sean accesibles.
     */
    private fun configurarSafeArea() {
        ViewCompat.setOnApplyWindowInsetsListener(headerLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusBarHeight = systemBars.top
            
            // Aplicar padding superior dinámico basado en la altura de la barra de estado
            // Mantener el padding lateral e inferior original (16dp)
            val paddingDp = 16
            val paddingPx = (paddingDp * resources.displayMetrics.density).toInt()
            
            view.updatePadding(
                top = statusBarHeight + paddingPx,
                bottom = paddingPx,
                left = paddingPx,
                right = paddingPx
            )
            
            insets
        }
    }

    private fun cargarDatosUsuario() {
        val repository = com.grupo4.appreservas.repository.PeruvianServiceRepository.getInstance(this)
        val usuario = repository.buscarUsuarioPorId(usuarioId)
        
        if (usuario != null) {
            tvNombreGuia.text = usuario.nombreCompleto
        }
    }

    private fun configurarRecyclerView() {
        toursAdapter = ToursAdapter { tour ->
            abrirDetalleTour(tour.tourId)
        }

        recyclerTours.apply {
            layoutManager = LinearLayoutManager(this@PanelGuiaActivity)
            adapter = toursAdapter
        }
    }

    private fun configurarListeners() {
        btnSalir.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun observarViewModel() {
        viewModel.toursDelDia.observe(this) { tours ->
            toursAdapter.actualizarLista(tours)
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarToursDelDia() {
        viewModel.cargarToursDelDia(usuarioId)
    }

    /**
     * Abre el detalle de un tour.
     * Equivalente a abrirDetalleTour(tourId) del diagrama UML.
     */
    private fun abrirDetalleTour(tourId: String) {
        val intent = Intent(this, DetalleTourActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        intent.putExtra("USUARIO_ID", usuarioId)
        startActivity(intent)
    }

    private fun cerrarSesion() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

