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
 * Servicio para manejar notificaciones push de Android.
 * Envía notificaciones del sistema Android según el diagrama UML.
 */
class NotificacionesService(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "notificaciones_channel"
        private const val CHANNEL_NAME = "Notificaciones de Tours"
        private const val CHANNEL_DESCRIPTION = "Notificaciones sobre recordatorios, alertas climáticas y ofertas"
        private var notificationId = 1000
    }

    init {
        crearCanalNotificaciones()
    }

    /**
     * Crea el canal de notificaciones para Android Oreo y superiores.
     */
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importancia).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    /**
     * Envía una notificación push.
     * 
     * @param notificacion Notificación a enviar
     */
    fun enviarNotificacionPush(notificacion: Notificacion) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear Intent para abrir la actividad de notificaciones
        val intent = Intent(context, NotificacionesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("USUARIO_ID", notificacion.usuarioId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId++,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Determinar ícono según el tipo de notificación
        val iconId = when (notificacion.tipo) {
            com.grupo4.appreservas.modelos.TipoNotificacion.RECORDATORIO -> android.R.drawable.ic_dialog_info
            com.grupo4.appreservas.modelos.TipoNotificacion.ALERTA_CLIMATICA -> android.R.drawable.ic_dialog_alert
            com.grupo4.appreservas.modelos.TipoNotificacion.OFERTA_ULTIMO_MINUTO -> android.R.drawable.ic_menu_view
            com.grupo4.appreservas.modelos.TipoNotificacion.CONFIRMACION_RESERVA -> android.R.drawable.ic_dialog_info
            com.grupo4.appreservas.modelos.TipoNotificacion.CLIMA_FAVORABLE -> android.R.drawable.ic_dialog_info
        }

        // Construir la notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setContentTitle(notificacion.titulo)
            .setContentText(notificacion.descripcion)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificacion.descripcion))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Agregar información adicional según el tipo
        when (notificacion.tipo) {
            com.grupo4.appreservas.modelos.TipoNotificacion.RECORDATORIO -> {
                notificacion.puntoEncuentro?.let {
                    builder.setContentText("${notificacion.descripcion}\nPunto de encuentro: $it")
                }
            }
            com.grupo4.appreservas.modelos.TipoNotificacion.ALERTA_CLIMATICA -> {
                notificacion.recomendaciones?.let {
                    builder.setStyle(NotificationCompat.BigTextStyle()
                        .bigText("${notificacion.descripcion}\n\nRecomendación: $it"))
                }
            }
            com.grupo4.appreservas.modelos.TipoNotificacion.OFERTA_ULTIMO_MINUTO -> {
                notificacion.descuento?.let {
                    builder.setContentText("${notificacion.descripcion}\n¡${it}% de descuento!")
                }
            }
            else -> {
                // Otros tipos
            }
        }

        // Mostrar la notificación
        notificationManager.notify(notificacion.id.hashCode(), builder.build())
    }

    /**
     * Cancela una notificación.
     * 
     * @param notificacionId ID de la notificación a cancelar
     */
    fun cancelarNotificacion(notificacionId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificacionId.hashCode())
    }

    /**
     * Cancela todas las notificaciones.
     */
    fun cancelarTodasLasNotificaciones() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}

