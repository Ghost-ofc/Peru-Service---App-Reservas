package com.grupo4.appreservas.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grupo4.appreservas.R

/**
 * Adapter para mostrar las fotos seleccionadas antes de subirlas.
 */
class FotosSeleccionadasAdapter(
    private val onEliminarFoto: (Int) -> Unit
) : RecyclerView.Adapter<FotosSeleccionadasAdapter.FotoSeleccionadaViewHolder>() {

    private var fotos = listOf<Uri>()

    fun actualizarLista(nuevaLista: List<Uri>) {
        fotos = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoSeleccionadaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto_seleccionada, parent, false)
        return FotoSeleccionadaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoSeleccionadaViewHolder, position: Int) {
        holder.bind(fotos[position], position)
    }

    override fun getItemCount() = fotos.size

    inner class FotoSeleccionadaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgFoto: ImageView = itemView.findViewById(R.id.imgFoto)
        private val btnEliminar: ImageView = itemView.findViewById(R.id.btnEliminar)

        fun bind(foto: Uri, position: Int) {
            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(foto)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imgFoto)

            btnEliminar.setOnClickListener {
                onEliminarFoto(position)
            }
        }
    }
}

