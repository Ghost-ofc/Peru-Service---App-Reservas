package com.grupo4.appreservas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.grupo4.appreservas.R
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDateItem(
    val date: Date?,
    val day: Int,
    val isAvailable: Boolean,
    val isSelected: Boolean = false,
    val isCurrentMonth: Boolean = true
)

class CalendarDateAdapter(
    private val dates: List<CalendarDateItem>,
    private val onDateClick: (CalendarDateItem) -> Unit
) : RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder>() {

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardDate: MaterialCardView = itemView.findViewById(R.id.cardDate)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val item = dates[position]

        holder.txtFecha.text = if (item.date != null && item.isCurrentMonth) {
            item.day.toString()
        } else {
            ""
        }

        // Configurar estilo según el estado de la fecha
        when {
            item.isSelected -> {
                // Fecha seleccionada: fondo oscuro (similar al diseño), texto blanco
                holder.cardDate.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#333333")
                )
                holder.txtFecha.setTextColor(
                    holder.itemView.context.getColor(android.R.color.white)
                )
                holder.cardDate.strokeWidth = 0
                holder.cardDate.isEnabled = true
                holder.cardDate.isClickable = true
            }
            item.isAvailable && item.isCurrentMonth -> {
                // Fecha disponible: texto negro oscuro (#333333), fondo blanco
                holder.cardDate.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.white)
                )
                holder.txtFecha.setTextColor(
                    android.graphics.Color.parseColor("#333333")
                )
                holder.cardDate.strokeWidth = 0
                holder.cardDate.isEnabled = true
                holder.cardDate.isClickable = true
            }
            !item.isAvailable && item.isCurrentMonth -> {
                // Fecha no disponible: texto gris muy claro (#CCCCCC), fondo blanco
                holder.cardDate.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.white)
                )
                holder.txtFecha.setTextColor(
                    android.graphics.Color.parseColor("#CCCCCC")
                )
                holder.cardDate.strokeWidth = 0
                holder.cardDate.isEnabled = false
                holder.cardDate.isClickable = false
            }
            else -> {
                // Días de otros meses: texto gris claro (#E0E0E0), fondo transparente
                holder.cardDate.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.transparent)
                )
                holder.txtFecha.setTextColor(
                    android.graphics.Color.parseColor("#E0E0E0")
                )
                holder.cardDate.strokeWidth = 0
                holder.cardDate.isEnabled = false
                holder.cardDate.isClickable = false
            }
        }

        // Click listener
        if (item.isAvailable && item.isCurrentMonth && item.date != null) {
            holder.cardDate.setOnClickListener {
                onDateClick(item)
            }
        } else {
            holder.cardDate.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = dates.size
}

