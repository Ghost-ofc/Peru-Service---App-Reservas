package com.grupo4.appreservas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.grupo4.appreservas.service.NotificacionesScheduler
import com.grupo4.appreservas.ui.LoginActivity

/**
 * Activity principal que inicia la aplicación.
 * Redirige al login y programa las notificaciones.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Programar notificaciones periódicas
        val scheduler = NotificacionesScheduler(this)
        scheduler.programarNotificaciones()

        // Redirigir al login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
