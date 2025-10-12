package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.R

class DestinosAdapter(
    private val onItemClick: (Destino) -> Unit
) : RecyclerView.Adapter<DestinosAdapter.DestinoViewHolder>() {

    private var destinos = listOf<Destino>()

    fun actualizarLista(nuevaLista: List<Destino>) {
        destinos = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destino, parent, false)
        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        holder.bind(destinos[position])
    }

    override fun getItemCount() = destinos.size

    inner class DestinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgDestino: ImageView = itemView.findViewById(R.id.imgDestino)
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        private val txtUbicacion: TextView = itemView.findViewById(R.id.txtUbicacion)
        private val txtDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        private val txtDuracion: TextView = itemView.findViewById(R.id.txtDuracion)
        private val txtMaxPersonas: TextView = itemView.findViewById(R.id.txtMaxPersonas)
        private val txtCalificacion: TextView = itemView.findViewById(R.id.txtCalificacion)
        private val txtCupos: TextView = itemView.findViewById(R.id.txtCupos)
        private val btnVerDetalles: Button = itemView.findViewById(R.id.btnVerDetalles)

        fun bind(destino: Destino) {
            txtNombre.text = destino.nombre
            txtPrecio.text = "S/ ${destino.precio}"
            txtUbicacion.text = destino.ubicacion
            txtDescripcion.text = destino.descripcion
            txtDuracion.text = "${destino.duracionHoras} horas"
            txtMaxPersonas.text = "Máx. ${destino.maxPersonas} personas"
            txtCalificacion.text = "${destino.calificacion}/5 (${destino.numReseñas} reseñas)"
            txtCupos.text = "${destino.maxPersonas} cupos disponibles"

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(destino.imagenUrl)
                .centerCrop()
                .into(imgDestino)

            // Click en el botón
            btnVerDetalles.setOnClickListener {
                onItemClick(destino)
            }

            // Click en toda la card (opcional)
            itemView.setOnClickListener {
                onItemClick(destino)
            }
        }
    }
}