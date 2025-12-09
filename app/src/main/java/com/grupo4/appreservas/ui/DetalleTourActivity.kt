package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.TourDelDiaViewModel

/**
 * Activity para mostrar el detalle de un tour.
 * Equivalente a DetalleTourActivity del diagrama UML.
 */
class DetalleTourActivity : AppCompatActivity() {

    private var tourId: String = ""
    private var usuarioId: Int = 0
    private lateinit var viewModel: TourDelDiaViewModel
    private lateinit var tvNombreTour: TextView
    private lateinit var tvFechaHora: TextView
    private lateinit var tvPuntoEncuentro: TextView
    private lateinit var tvParticipantes: TextView
    private lateinit var btnEscanearQR: Button
    private lateinit var btnVerLista: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_tour)

        tourId = intent.getStringExtra("TOUR_ID") ?: ""
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)

        if (tourId.isEmpty() || usuarioId == 0) {
            Toast.makeText(this, "Error: Información del tour no disponible", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(TourDelDiaViewModel::class.java)

        inicializarVistas()
        configurarListeners()
        cargarDetalleTour()
    }

    private fun inicializarVistas() {
        tvNombreTour = findViewById(R.id.tv_nombre_tour)
        tvFechaHora = findViewById(R.id.tv_fecha_hora)
        tvPuntoEncuentro = findViewById(R.id.tv_punto_encuentro)
        tvParticipantes = findViewById(R.id.tv_participantes)
        btnEscanearQR = findViewById(R.id.btn_escanear_qr)
        btnVerLista = findViewById(R.id.btn_ver_lista)
    }

    private fun configurarListeners() {
        btnEscanearQR.setOnClickListener {
            abrirEscaneoQR()
        }

        btnVerLista.setOnClickListener {
            // Por ahora solo mostrar un mensaje
            Toast.makeText(this, "Lista de participantes (próximamente)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDetalleTour() {
        val tour = viewModel.abrirTour(tourId)
        if (tour != null) {
            mostrarDetalleTour(tour)
        } else {
            // Si no está en el ViewModel, buscar directamente en el repository
            val repository = PeruvianServiceRepository.getInstance(this)
            val tourEncontrado = repository.obtenerTourPorId(tourId)
            
            if (tourEncontrado != null) {
                mostrarDetalleTour(tourEncontrado)
            } else {
                Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Muestra el detalle del tour.
     * Equivalente a mostrarDetalleTour(tour) del diagrama UML.
     */
    private fun mostrarDetalleTour(tour: Tour) {
        tvNombreTour.text = tour.nombre
        tvFechaHora.text = "${tour.fecha}\n${tour.hora}"
        tvPuntoEncuentro.text = tour.puntoEncuentro
        tvParticipantes.text = "${tour.participantesConfirmados} de ${tour.capacidad} confirmados"
    }

    /**
     * Abre la actividad de escaneo QR.
     * Equivalente a abrirEscaneoQR() del diagrama UML.
     */
    private fun abrirEscaneoQR() {
        val intent = Intent(this, EscaneoQRActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        intent.putExtra("USUARIO_ID", usuarioId)
        startActivity(intent)
    }
}

