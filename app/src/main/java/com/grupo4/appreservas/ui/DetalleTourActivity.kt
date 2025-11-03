package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.EstadoReserva
import com.grupo4.appreservas.repository.RepositorioAsignaciones

class DetalleTourActivity : AppCompatActivity() {

    private lateinit var repositorioAsignaciones: RepositorioAsignaciones
    private lateinit var tvNombreTour: TextView
    private lateinit var tvFechaHora: TextView
    private lateinit var tvPunto: TextView
    private lateinit var tvParticipantes: TextView
    private lateinit var btnEscanearQR: Button
    private lateinit var btnVerLista: Button
    private lateinit var tourId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_tour)

        inicializarDependencias()
        inicializarVistas()
        mostrarDetalleTour()
    }

    private fun inicializarDependencias() {
        repositorioAsignaciones = RepositorioAsignaciones(this)
    }

    private fun inicializarVistas() {
        tvNombreTour = findViewById(R.id.tv_nombre_tour)
        tvFechaHora = findViewById(R.id.tv_fecha_hora)
        tvPunto = findViewById(R.id.tv_punto_encuentro)
        tvParticipantes = findViewById(R.id.tv_participantes)
        btnEscanearQR = findViewById(R.id.btn_escanear_qr)
        btnVerLista = findViewById(R.id.btn_ver_lista)

        btnEscanearQR.setOnClickListener {
            abrirEscaneoQR()
        }

        btnVerLista.setOnClickListener {
            verListaParticipantes()
        }
    }

    private fun mostrarDetalleTour() {
        tourId = intent.getStringExtra("TOUR_ID") ?: return

        // En producción, obtendrías el tour completo del repositorio
        // Por ahora, usamos datos de ejemplo
        tvNombreTour.text = "Machu Picchu Tour Completo"
        tvFechaHora.text = "27 de Octubre, 2025\n08:00 AM"
        tvPunto.text = "Plaza de Armas, Cusco"

        val participantes = repositorioAsignaciones.obtenerParticipantes(tourId)
        val confirmados = participantes.count { it.estado == EstadoReserva.CONFIRMADO }
        tvParticipantes.text = "$confirmados de ${participantes.size} confirmados"
    }

    private fun abrirEscaneoQR() {
        val intent = Intent(this, EscaneoQRActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        startActivity(intent)
    }

    private fun verListaParticipantes() {
        val intent = Intent(this, ListaParticipantesActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        startActivity(intent)
    }
}