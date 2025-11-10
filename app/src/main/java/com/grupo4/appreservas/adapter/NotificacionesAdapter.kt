package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.modelos.TipoNotificacion
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Adapter para mostrar notificaciones en un RecyclerView.
 */
class NotificacionesAdapter(
    private val notificaciones: List<Notificacion>,
    private val onItemClick: (Notificacion) -> Unit,
    private val onVerOfertaClick: ((Notificacion) -> Unit)? = null
) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(notificaciones[position])
    }

    override fun getItemCount(): Int = notificaciones.size

    inner class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcono: ImageView = itemView.findViewById(R.id.ivIcono)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val tvPuntoEncuentro: TextView = itemView.findViewById(R.id.tvPuntoEncuentro)
        private val layoutPuntoEncuentro: LinearLayout = itemView.findViewById(R.id.layoutPuntoEncuentro)
        private val tvRecomendacion: TextView = itemView.findViewById(R.id.tvRecomendacion)
        private val layoutRecomendacion: LinearLayout = itemView.findViewById(R.id.layoutRecomendacion)
        private val tvDescuento: TextView = itemView.findViewById(R.id.tvDescuento)
        private val layoutDescuento: LinearLayout = itemView.findViewById(R.id.layoutDescuento)
        private val btnVerOferta: Button = itemView.findViewById(R.id.btnVerOferta)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val viewNoLeida: View = itemView.findViewById(R.id.viewNoLeida)

        fun bind(notificacion: Notificacion) {
            // Configurar ícono según el tipo
            val (iconRes, iconColor) = when (notificacion.tipo) {
                TipoNotificacion.RECORDATORIO -> Pair(
                    android.R.drawable.ic_menu_recent_history,
                    "#2196F3" // Azul
                )
                TipoNotificacion.ALERTA_CLIMATICA -> Pair(
                    android.R.drawable.ic_dialog_alert,
                    "#FF9800" // Naranja
                )
                TipoNotificacion.CLIMA_FAVORABLE -> Pair(
                    android.R.drawable.ic_dialog_info,
                    "#FF9800" // Naranja
                )
                TipoNotificacion.OFERTA_ULTIMO_MINUTO -> Pair(
                    android.R.drawable.ic_menu_view,
                    "#4CAF50" // Verde
                )
                TipoNotificacion.CONFIRMACION_RESERVA -> Pair(
                    android.R.drawable.ic_menu_recent_history,
                    "#2196F3" // Azul
                )
            }
            
            ivIcono.setImageResource(iconRes)
            ivIcono.setColorFilter(android.graphics.Color.parseColor(iconColor))

            // Configurar título y descripción
            tvTitulo.text = notificacion.titulo
            tvDescripcion.text = notificacion.descripcion

            // Mostrar punto de encuentro solo para recordatorios
            if (notificacion.tipo == TipoNotificacion.RECORDATORIO && !notificacion.puntoEncuentro.isNullOrEmpty()) {
                layoutPuntoEncuentro.visibility = View.VISIBLE
                tvPuntoEncuentro.text = "Punto de encuentro: ${notificacion.puntoEncuentro}"
            } else {
                layoutPuntoEncuentro.visibility = View.GONE
            }

            // Mostrar recomendación solo para alertas climáticas
            if (notificacion.esAlertaClimatica() && !notificacion.recomendaciones.isNullOrEmpty()) {
                layoutRecomendacion.visibility = View.VISIBLE
                tvRecomendacion.text = "Recomendación: ${notificacion.recomendaciones}"
            } else {
                layoutRecomendacion.visibility = View.GONE
            }

            // Mostrar descuento solo para ofertas
            if (notificacion.esOferta() && notificacion.descuento != null) {
                layoutDescuento.visibility = View.VISIBLE
                tvDescuento.text = "${notificacion.descuento}% OFF"
                btnVerOferta.setOnClickListener {
                    onVerOfertaClick?.invoke(notificacion)
                }
            } else {
                layoutDescuento.visibility = View.GONE
            }

            // Mostrar timestamp relativo
            tvTimestamp.text = obtenerTimestampRelativo(notificacion.fechaCreacion)

            // Mostrar indicador de no leída
            viewNoLeida.visibility = if (!notificacion.leida) View.VISIBLE else View.GONE

            // Click en el item
            itemView.setOnClickListener {
                onItemClick(notificacion)
            }
        }

        private fun obtenerTimestampRelativo(fecha: Date): String {
            val ahora = Date()
            val diferencia = ahora.time - fecha.time
            val segundos = diferencia / 1000
            val minutos = segundos / 60
            val horas = minutos / 60
            val dias = horas / 24

            return when {
                minutos < 1 -> "Hace unos momentos"
                minutos < 60 -> "Hace $minutos ${if (minutos == 1L) "minuto" else "minutos"}"
                horas < 24 -> "Hace $horas ${if (horas == 1L) "hora" else "horas"}"
                dias < 7 -> "Hace $dias ${if (dias == 1L) "día" else "días"}"
                else -> {
                    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formato.format(fecha)
                }
            }
        }
    }
}

