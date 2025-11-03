package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.EstadoReserva

class ParticipantesAdapter(
    private val participantes: List<Reserva>
) : RecyclerView.Adapter<ParticipantesAdapter.ParticipanteViewHolder>() {

    inner class ParticipanteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivEstado: ImageView = view.findViewById(R.id.iv_estado)
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre)
        val tvDocumento: TextView = view.findViewById(R.id.tv_documento)
        val tvEstado: TextView = view.findViewById(R.id.tv_estado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipanteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participante, parent, false)
        return ParticipanteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipanteViewHolder, position: Int) {
        val participante = participantes[position]

        holder.tvNombre.text = participante.nombreTurista
        holder.tvDocumento.text = "Doc: ${participante.documento}"
        holder.tvEstado.text = participante.estadoStr

        // Cambiar ícono según el estado usando el enum
        val iconoRes = when (participante.estado) {
            EstadoReserva.CONFIRMADO -> R.drawable.ic_check
            EstadoReserva.PENDIENTE -> R.drawable.ic_pending
            EstadoReserva.CANCELADO -> R.drawable.ic_cancel
        }
        holder.ivEstado.setImageResource(iconoRes)

        // Cambiar color según estado
        val colorEstado = when (participante.estado) {
            EstadoReserva.CONFIRMADO -> android.graphics.Color.parseColor("#4CAF50")
            EstadoReserva.PENDIENTE -> android.graphics.Color.parseColor("#FF9800")
            EstadoReserva.CANCELADO -> android.graphics.Color.parseColor("#F44336")
        }
        holder.tvEstado.setTextColor(colorEstado)
    }

    override fun getItemCount() = participantes.size
}