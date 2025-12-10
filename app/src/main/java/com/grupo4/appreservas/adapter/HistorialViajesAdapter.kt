package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Reserva
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adaptador para mostrar el historial de viajes (reservas) del usuario.
 */
class HistorialViajesAdapter(
    private val onItemClick: (Reserva) -> Unit = {}
) : RecyclerView.Adapter<HistorialViajesAdapter.HistorialViajeViewHolder>() {

    private val reservas = mutableListOf<Reserva>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun actualizarLista(nuevasReservas: List<Reserva>) {
        reservas.clear()
        reservas.addAll(nuevasReservas)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViajeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_viaje, parent, false)
        return HistorialViajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViajeViewHolder, position: Int) {
        holder.bind(reservas[position])
    }

    override fun getItemCount() = reservas.size

    inner class HistorialViajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreDestino: TextView = itemView.findViewById(R.id.tvNombreDestino)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)

        fun bind(reserva: Reserva) {
            // Nombre del destino
            tvNombreDestino.text = reserva.destino?.nombre ?: "Tour sin nombre"

            // Fecha
            tvFecha.text = dateFormat.format(reserva.fecha)

            // Hora
            tvHora.text = reserva.horaInicio

            // Estado
            tvEstado.text = reserva.estado.valor
            when (reserva.estado) {
                com.grupo4.appreservas.modelos.EstadoReserva.CONFIRMADO -> {
                    tvEstado.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    tvEstado.setBackgroundColor(0xFFE8F5E9.toInt())
                }
                com.grupo4.appreservas.modelos.EstadoReserva.PENDIENTE -> {
                    tvEstado.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                    tvEstado.setBackgroundColor(0xFFFFF3E0.toInt())
                }
                com.grupo4.appreservas.modelos.EstadoReserva.CANCELADO -> {
                    tvEstado.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    tvEstado.setBackgroundColor(0xFFFFEBEE.toInt())
                }
            }

            // Click en el item
            itemView.setOnClickListener {
                onItemClick(reserva)
            }
        }
    }
}

