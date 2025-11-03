package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Tour

class ToursAdapter(
    private val tours: List<Tour>,
    private val onTourClick: (Tour) -> Unit
) : RecyclerView.Adapter<ToursAdapter.TourViewHolder>() {

    inner class TourViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tv_tour_nombre)
        val tvFecha: TextView = view.findViewById(R.id.tv_tour_fecha)
        val tvPunto: TextView = view.findViewById(R.id.tv_tour_punto)
        val tvParticipantes: TextView = view.findViewById(R.id.tv_tour_participantes)
        val tvEstado: TextView = view.findViewById(R.id.tv_tour_estado)
        val btnVerDetalles: android.widget.Button = view.findViewById(R.id.btn_ver_detalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tour_guia, parent, false)
        return TourViewHolder(view)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val tour = tours[position]

        holder.tvNombre.text = tour.nombre
        holder.tvFecha.text = "${tour.fecha} • ${tour.hora}"
        holder.tvPunto.text = tour.puntoEncuentro

        // Formato mejorado para participantes
        val textoParticipantes = if (tour.participantesConfirmados > 0) {
            "${tour.participantesConfirmados}/${tour.capacidad} turistas confirmados"
        } else {
            "0/${tour.capacidad} turistas (sin confirmaciones)"
        }
        holder.tvParticipantes.text = textoParticipantes

        holder.tvEstado.text = tour.estado

        // Color según estado del tour
        val colorEstado = when (tour.estado) {
            "Completado" -> android.graphics.Color.parseColor("#4CAF50")
            "En Curso" -> android.graphics.Color.parseColor("#2196F3")
            else -> android.graphics.Color.parseColor("#FF9800") // Pendiente
        }
        holder.tvEstado.setTextColor(colorEstado)

        holder.btnVerDetalles.setOnClickListener {
            onTourClick(tour)
        }
    }

    override fun getItemCount() = tours.size
}