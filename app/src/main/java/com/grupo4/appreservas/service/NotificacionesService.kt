package com.grupo4.appreservas.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.ui.NotificacionesActivity

/**
 * Servicio para gestionar notificaciones push de Android.
 * Equivalente a NotificacionesService del diagrama UML.
 */
class NotificacionesService(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        crearCanalNotificaciones()
    }

    /**
     * Crea el canal de notificaciones para Android 8.0+
     */
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de Tours",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre tours, recordatorios y ofertas"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Muestra una notificación push.
     */
    fun mostrarNotificacion(notificacion: Notificacion, usuarioId: Int) {
        // Si es una notificación de encuesta, abrir directamente la EncuestaActivity
        val intent = if (notificacion.tipo == com.grupo4.appreservas.modelos.TipoNotificacion.ENCUESTA_SATISFACCION && notificacion.tourId != null) {
            Intent(context, com.grupo4.appreservas.ui.EncuestaActivity::class.java).apply {
                putExtra("TOUR_ID", notificacion.tourId)
                putExtra("USUARIO_ID", usuarioId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            Intent(context, NotificacionesActivity::class.java).apply {
                putExtra("USUARIO_ID", usuarioId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificacion.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(notificacion.titulo)
            .setContentText(notificacion.descripcion)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificacion.descripcion))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Agregar información adicional según el tipo
        when (notificacion.tipo) {
            com.grupo4.appreservas.modelos.TipoNotificacion.RECORDATORIO -> {
                notificacion.horaTour?.let {
                    builder.setContentText("Hora: $it - ${notificacion.descripcion}")
                }
                notificacion.puntoEncuentro?.let {
                    builder.setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("${notificacion.descripcion}\nPunto de encuentro: $it")
                    )
                }
            }
            com.grupo4.appreservas.modelos.TipoNotificacion.ALERTA_CLIMATICA -> {
                notificacion.recomendaciones?.let {
                    builder.setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("${notificacion.descripcion}\nRecomendaciones: $it")
                    )
                }
            }
            com.grupo4.appreservas.modelos.TipoNotificacion.OFERTA_ULTIMO_MINUTO -> {
                notificacion.descuento?.let {
                    builder.setContentText("¡$it% de descuento! ${notificacion.descripcion}")
                }
            }
            com.grupo4.appreservas.modelos.TipoNotificacion.ENCUESTA_SATISFACCION -> {
                builder.setContentText(notificacion.descripcion)
                // Agregar acción para responder encuesta
                builder.addAction(
                    R.drawable.ic_notifications,
                    "Responder encuesta",
                    pendingIntent
                )
            }
            else -> {}
        }

        notificationManager.notify(notificacion.id.hashCode(), builder.build())
    }

    companion object {
        private const val CHANNEL_ID = "notificaciones_tours"
    }
}

