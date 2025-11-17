package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.modelos.TipoNotificacion
import java.text.SimpleDateFormat
import java.util.*

class NotificacionesAdapter(
    private val onItemClick: (Notificacion) -> Unit,
    private val onMarcarLeida: (String) -> Unit
) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {

    private var notificaciones = listOf<Notificacion>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun actualizarLista(nuevaLista: List<Notificacion>) {
        notificaciones = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(notificaciones[position])
    }

    override fun getItemCount() = notificaciones.size

    inner class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val txtTipo: TextView = itemView.findViewById(R.id.txt_tipo)
        private val txtFecha: TextView = itemView.findViewById(R.id.txt_fecha)
        private val txtTitulo: TextView = itemView.findViewById(R.id.txt_titulo)
        private val txtDescripcion: TextView = itemView.findViewById(R.id.txt_descripcion)
        private val layoutInfoAdicional: LinearLayout = itemView.findViewById(R.id.layout_info_adicional)
        private val txtHoraTour: TextView = itemView.findViewById(R.id.txt_hora_tour)
        private val txtPuntoEncuentro: TextView = itemView.findViewById(R.id.txt_punto_encuentro)
        private val txtDescuento: TextView = itemView.findViewById(R.id.txt_descuento)
        private val txtRecomendaciones: TextView = itemView.findViewById(R.id.txt_recomendaciones)

        fun bind(notificacion: Notificacion) {
            // Tipo de notificación
            txtTipo.text = notificacion.tipo.valor
            
            // Fecha
            txtFecha.text = dateFormat.format(notificacion.fechaCreacion)
            
            // Título y descripción
            txtTitulo.text = notificacion.titulo
            txtDescripcion.text = notificacion.descripcion
            
            // Configurar según el tipo
            when (notificacion.tipo) {
                TipoNotificacion.RECORDATORIO -> {
                    layoutInfoAdicional.visibility = View.VISIBLE
                    txtDescuento.visibility = View.GONE
                    txtRecomendaciones.visibility = View.GONE
                    
                    notificacion.horaTour?.let {
                        txtHoraTour.text = "Hora: $it"
                        txtHoraTour.visibility = View.VISIBLE
                    } ?: run {
                        txtHoraTour.visibility = View.GONE
                    }
                    
                    notificacion.puntoEncuentro?.let {
                        txtPuntoEncuentro.text = "Punto de encuentro: $it"
                        txtPuntoEncuentro.visibility = View.VISIBLE
                    } ?: run {
                        txtPuntoEncuentro.visibility = View.GONE
                    }
                }
                
                TipoNotificacion.ALERTA_CLIMATICA -> {
                    layoutInfoAdicional.visibility = View.GONE
                    txtDescuento.visibility = View.GONE
                    
                    notificacion.recomendaciones?.let {
                        txtRecomendaciones.text = "Recomendaciones: $it"
                        txtRecomendaciones.visibility = View.VISIBLE
                    } ?: run {
                        txtRecomendaciones.visibility = View.GONE
                    }
                }
                
                TipoNotificacion.OFERTA_ULTIMO_MINUTO -> {
                    layoutInfoAdicional.visibility = View.GONE
                    txtRecomendaciones.visibility = View.GONE
                    
                    notificacion.descuento?.let {
                        txtDescuento.text = "Descuento: $it%"
                        txtDescuento.visibility = View.VISIBLE
                    } ?: run {
                        txtDescuento.visibility = View.GONE
                    }
                }
                
                else -> {
                    layoutInfoAdicional.visibility = View.GONE
                    txtDescuento.visibility = View.GONE
                    txtRecomendaciones.visibility = View.GONE
                }
            }
            
            // Cambiar color de fondo si está leída
            if (notificacion.leida) {
                itemView.alpha = 0.6f
            } else {
                itemView.alpha = 1.0f
            }
            
            // Click en el item
            itemView.setOnClickListener {
                if (!notificacion.leida) {
                    onMarcarLeida(notificacion.id)
                }
                onItemClick(notificacion)
            }
        }
    }
}

