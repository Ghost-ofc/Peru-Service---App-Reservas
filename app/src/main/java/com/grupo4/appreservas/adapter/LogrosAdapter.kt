package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Logro

class LogrosAdapter(private var logros: List<Logro> = emptyList()) :
    RecyclerView.Adapter<LogrosAdapter.LogroViewHolder>() {

    class LogroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreLogro: TextView = itemView.findViewById(R.id.tvNombreLogro)
        val tvDescripcionLogro: TextView = itemView.findViewById(R.id.tvDescripcionLogro)
        val ivIconoLogro: ImageView = itemView.findViewById(R.id.ivIconoLogro)
        val ivEstadoLogro: ImageView = itemView.findViewById(R.id.ivEstadoLogro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logro, parent, false)
        return LogroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogroViewHolder, position: Int) {
        val logro = logros[position]
        holder.tvNombreLogro.text = logro.nombre
        holder.tvDescripcionLogro.text = logro.descripcion
        
        // Asignar icono y color según el tipo de logro (según diseño UX)
        when (logro.tipo) {
            com.grupo4.appreservas.modelos.TipoLogro.PRIMER_VIAJE -> {
                // Pin de ubicación amarillo
                holder.ivIconoLogro.setImageResource(android.R.drawable.ic_menu_myplaces)
                holder.ivIconoLogro.setBackgroundResource(R.drawable.circle_yellow)
            }
            com.grupo4.appreservas.modelos.TipoLogro.EXPLORADOR_SEMANA -> {
                // Trofeo azul (usando estrella como sustituto)
                holder.ivIconoLogro.setImageResource(android.R.drawable.btn_star_big_on)
                holder.ivIconoLogro.setBackgroundResource(R.drawable.circle_blue)
            }
            com.grupo4.appreservas.modelos.TipoLogro.TOURS_COMPLETADOS -> {
                // Gráfico púrpura
                holder.ivIconoLogro.setImageResource(android.R.drawable.ic_menu_sort_by_size)
                holder.ivIconoLogro.setBackgroundResource(R.drawable.circle_purple)
            }
            com.grupo4.appreservas.modelos.TipoLogro.PUNTOS_ACUMULADOS -> {
                // Estrella amarilla
                holder.ivIconoLogro.setImageResource(android.R.drawable.btn_star_big_on)
                holder.ivIconoLogro.setBackgroundResource(R.drawable.circle_yellow)
            }
            else -> {
                holder.ivIconoLogro.setImageResource(android.R.drawable.btn_star_big_on)
                holder.ivIconoLogro.setBackgroundResource(R.drawable.circle_blue)
            }
        }
        holder.ivIconoLogro.setColorFilter(android.graphics.Color.WHITE)
        
        // Mostrar checkmark solo si está desbloqueado
        holder.ivEstadoLogro.visibility = if (logro.desbloqueado) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun getItemCount(): Int = logros.size

    fun actualizarLista(nuevaLista: List<Logro>) {
        logros = nuevaLista
        notifyDataSetChanged()
    }
}

