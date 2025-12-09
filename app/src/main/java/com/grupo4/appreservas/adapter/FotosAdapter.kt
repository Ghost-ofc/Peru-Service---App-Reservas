package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Foto

/**
 * Adapter para mostrar las fotos del álbum en un RecyclerView.
 */
class FotosAdapter : RecyclerView.Adapter<FotosAdapter.FotoViewHolder>() {

    private var fotos = listOf<Foto>()

    fun actualizarLista(nuevaLista: List<Foto>) {
        fotos = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        holder.bind(fotos[position])
    }

    override fun getItemCount() = fotos.size

    inner class FotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgFoto: ImageView = itemView.findViewById(R.id.imgFoto)

        fun bind(foto: Foto) {
            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(foto.urlImagen)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imgFoto)
        }
    }
}

