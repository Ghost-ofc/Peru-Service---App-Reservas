package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.ToursAdapter
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.viewmodel.GuiaViewModel

class PanelGuiaActivity : AppCompatActivity() {

    private lateinit var guiaViewModel: GuiaViewModel
    private lateinit var recyclerTours: RecyclerView
    private lateinit var toursAdapter: ToursAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_guia)

        inicializarDependencias()
        inicializarVistas()
        mostrarToursDelDia()
    }

    private fun inicializarDependencias() {
        guiaViewModel = GuiaViewModel(this)
        val destinoRepo = DestinoRepository.getInstance(this)
    }

    private fun inicializarVistas() {
        recyclerTours = findViewById(R.id.recycler_tours)
        recyclerTours.layoutManager = LinearLayoutManager(this)
    }

    private fun mostrarToursDelDia() {
        val guiaId = intent.getIntExtra("GUIA_ID", 1)
        val tours = guiaViewModel.cargarToursDelDia(guiaId)

        toursAdapter = ToursAdapter(tours) { tour ->
            abrirDetalleTour(tour.tourId)
        }
        recyclerTours.adapter = toursAdapter

        if (tours.isEmpty()) {
            Toast.makeText(this, "No tienes tours asignados para hoy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirDetalleTour(tourId: String) {
        val intent = Intent(this, DetalleTourActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        startActivity(intent)
    }
}
