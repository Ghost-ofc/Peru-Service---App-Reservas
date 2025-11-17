package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Logro

class LogrosAdapter : RecyclerView.Adapter<LogrosAdapter.LogroViewHolder>() {

    private var logros = listOf<Logro>()

    fun actualizarLista(nuevaLista: List<Logro>) {
        logros = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logro, parent, false)
        return LogroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogroViewHolder, position: Int) {
        holder.bind(logros[position])
    }

    override fun getItemCount() = logros.size

    inner class LogroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val ivIcono: ImageView = itemView.findViewById(R.id.ivIcono)
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        private val txtDesbloqueado: TextView = itemView.findViewById(R.id.txtDesbloqueado)

        fun bind(logro: Logro) {
            txtNombre.text = logro.nombre
            txtDescripcion.text = logro.descripcion
            
            // Configurar según si está desbloqueado
            if (logro.desbloqueado) {
                ivIcono.alpha = 1.0f
                txtDesbloqueado.visibility = View.VISIBLE
                itemView.alpha = 1.0f
            } else {
                ivIcono.alpha = 0.5f
                txtDesbloqueado.visibility = View.GONE
                itemView.alpha = 0.6f
            }
        }
    }
}

