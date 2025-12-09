package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Tour
import java.text.SimpleDateFormat
import java.util.*

class ToursAdapter(
    private val onItemClick: (Tour) -> Unit
) : RecyclerView.Adapter<ToursAdapter.TourViewHolder>() {

    private var tours = listOf<Tour>()

    fun actualizarLista(nuevaLista: List<Tour>) {
        tours = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tour, parent, false)
        return TourViewHolder(view)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        holder.bind(tours[position])
    }

    override fun getItemCount() = tours.size

    inner class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val txtNombreTour: TextView = itemView.findViewById(R.id.txt_nombre_tour)
        private val txtFechaHora: TextView = itemView.findViewById(R.id.txt_fecha_hora)
        private val txtPuntoEncuentro: TextView = itemView.findViewById(R.id.txt_punto_encuentro)
        private val txtParticipantes: TextView = itemView.findViewById(R.id.txt_participantes)
        private val txtEstado: TextView = itemView.findViewById(R.id.txt_estado)

        fun bind(tour: Tour) {
            txtNombreTour.text = tour.nombre
            
            // Formatear fecha y hora
            val fechaHora = "${tour.fecha} ${tour.hora}"
            txtFechaHora.text = fechaHora
            
            txtPuntoEncuentro.text = tour.puntoEncuentro
            txtParticipantes.text = "${tour.participantesConfirmados} de ${tour.capacidad} confirmados"
            
            // Estado del tour
            txtEstado.text = tour.estado
            when (tour.estado) {
                "Pendiente" -> {
                    txtEstado.setBackgroundResource(R.drawable.bg_chip_pendiente)
                    txtEstado.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                "En Curso" -> {
                    txtEstado.setBackgroundResource(R.drawable.bg_chip_pendiente)
                    txtEstado.setTextColor(itemView.context.getColor(android.R.color.holo_blue_dark))
                }
                "Completado" -> {
                    txtEstado.setBackgroundResource(R.drawable.bg_chip_pendiente)
                    txtEstado.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
            }

            // Click en toda la card
            itemView.setOnClickListener {
                onItemClick(tour)
            }
        }
    }
}

