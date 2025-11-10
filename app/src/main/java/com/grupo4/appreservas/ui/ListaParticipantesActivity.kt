package com.grupo4.appreservas.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.ParticipantesAdapter
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.repository.RepositorioAsignaciones

class ListaParticipantesActivity : AppCompatActivity() {

    private lateinit var repositorioAsignaciones: RepositorioAsignaciones
    private lateinit var tvContador: TextView
    private lateinit var recyclerParticipantes: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_participantes)

        inicializarDependencias()
        inicializarVistas()
        cargarParticipantes()
    }

    private fun inicializarDependencias() {
        repositorioAsignaciones = RepositorioAsignaciones(this)
    }

    private fun inicializarVistas() {
        tvContador = findViewById(R.id.tv_contador)
        recyclerParticipantes = findViewById(R.id.recycler_participantes)
        recyclerParticipantes.layoutManager = LinearLayoutManager(this)
    }

    private fun cargarParticipantes() {
        val tourId = intent.getStringExtra("TOUR_ID") ?: return
        
        // Obtener el tour completo para obtener la capacidad total
        val dbHelper = com.grupo4.appreservas.repository.DatabaseHelper(this)
        val tour = dbHelper.obtenerTourPorId(tourId)
        val capacidadTotal = tour?.capacidad ?: 0
        
        val participantes = repositorioAsignaciones.obtenerParticipantes(tourId)

        val confirmados = participantes.count { it.estado == EstadoReserva.CONFIRMADO }
        // Mostrar confirmados de capacidad total, no de participantes.size
        tvContador.text = "$confirmados de $capacidadTotal confirmados"

        val adapter = ParticipantesAdapter(participantes)
        recyclerParticipantes.adapter = adapter
    }
}