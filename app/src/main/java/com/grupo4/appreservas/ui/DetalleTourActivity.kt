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

    /**
     * Muestra el detalle de un tour específico.
     * Equivalente a mostrarDetalleTour(tour) del diagrama UML.
     */
    private fun mostrarDetalleTour() {
        tourId = intent.getStringExtra("TOUR_ID") ?: return

        // Obtener el tour completo del repositorio
        val dbHelper = com.grupo4.appreservas.repository.DatabaseHelper(this)
        val tour = dbHelper.obtenerTourPorId(tourId)
        
        if (tour != null) {
            // Formatear fecha para mostrar
            val fechaFormateada = try {
                val formatoEntrada = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val formatoSalida = java.text.SimpleDateFormat("d 'de' MMMM, yyyy", java.util.Locale("es", "ES"))
                val fecha = formatoEntrada.parse(tour.fecha)
                if (fecha != null) {
                    formatoSalida.format(fecha).replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(java.util.Locale("es", "ES")) else it.toString() 
                    }
                } else {
                    tour.fecha
                }
            } catch (e: Exception) {
                tour.fecha
            }
            
            tvNombreTour.text = tour.nombre
            tvFechaHora.text = "$fechaFormateada\n${tour.hora}"
            tvPunto.text = tour.puntoEncuentro

            // Obtener participantes y mostrar confirmados vs capacidad total del tour
            val participantes = repositorioAsignaciones.obtenerParticipantes(tourId)
            val confirmados = participantes.count { it.estado == EstadoReserva.CONFIRMADO }
            tvParticipantes.text = "$confirmados de ${tour.capacidad} confirmados"
        } else {
            // Fallback si no se encuentra el tour
            tvNombreTour.text = "Tour no encontrado"
            tvParticipantes.text = "0 de 0 confirmados"
        }
    }

    /**
     * Abre la actividad de escaneo QR.
     * Equivalente a abrirEscaneoQR() del diagrama UML.
     */
    private fun abrirEscaneoQR() {
        val intent = Intent(this, EscaneoQRActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        // Obtener GUIA_ID de la sesión o del intent
        val guiaId = intent.getIntExtra("GUIA_ID", 1)
        intent.putExtra("GUIA_ID", guiaId)
        startActivity(intent)
    }

    private fun verListaParticipantes() {
        val intent = Intent(this, ListaParticipantesActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        startActivity(intent)
    }
}