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

class HistorialViajesAdapter(private var reservas: List<Reserva> = emptyList()) :
    RecyclerView.Adapter<HistorialViajesAdapter.ViajeViewHolder>() {

    class ViajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreDestino: TextView = itemView.findViewById(R.id.tvNombreDestino)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_viaje, parent, false)
        return ViajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val reserva = reservas[position]
        val destino = reserva.destino
        
        holder.tvNombreDestino.text = destino?.nombre ?: "Tour"
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvFecha.text = dateFormat.format(reserva.fecha)
        holder.tvHora.text = reserva.horaInicio ?: "N/A"
        holder.tvEstado.text = reserva.estado.valor
        
        // Cambiar color según el estado
        when (reserva.estado.valor) {
            "Confirmado" -> {
                holder.tvEstado.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
                holder.tvEstado.background = holder.itemView.context.getDrawable(android.R.drawable.dialog_holo_light_frame)
            }
            "Pendiente" -> {
                holder.tvEstado.setTextColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            "Cancelado" -> {
                holder.tvEstado.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            }
        }
    }

    override fun getItemCount(): Int = reservas.size

    fun actualizarLista(nuevaLista: List<Reserva>) {
        reservas = nuevaLista
        notifyDataSetChanged()
    }
}

