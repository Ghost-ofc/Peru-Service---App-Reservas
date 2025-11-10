package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.NotificacionesAdapter
import com.grupo4.appreservas.controller.ControlNotificaciones
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.repository.RepositorioNotificaciones
import com.grupo4.appreservas.repository.RepositorioOfertas
import com.grupo4.appreservas.repository.RepositorioClima
import com.grupo4.appreservas.service.NotificacionesService

/**
 * Activity de Notificaciones según el diagrama UML.
 * Equivalente a NotificacionesActivity del diagrama.
 * 
 * En arquitectura MVC, esta Activity (Vista) usa el ControlNotificaciones (Controller)
 * para manejar la lógica de notificaciones.
 */
class NotificacionesActivity : AppCompatActivity() {

    private lateinit var controlNotificaciones: ControlNotificaciones
    private lateinit var recyclerNotificaciones: RecyclerView
    private lateinit var tvContador: TextView
    private lateinit var tvMarcarTodas: TextView
    private lateinit var btnCerrar: ImageView
    private lateinit var notificacionesAdapter: NotificacionesAdapter

    private var usuarioId: Int = 0
    private var notificaciones: List<Notificacion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        obtenerDatosUsuario()
        inicializarDependencias()
        inicializarVistas()
        cargarNotificaciones()
    }

    private fun obtenerDatosUsuario() {
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        if (usuarioId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun inicializarDependencias() {
        val repositorioNotificaciones = RepositorioNotificaciones(this)
        val repositorioOfertas = RepositorioOfertas(this)
        val repositorioClima = RepositorioClima(this)
        val notificacionesService = NotificacionesService(this)

        controlNotificaciones = ControlNotificaciones(
            repositorioNotificaciones,
            repositorioOfertas,
            repositorioClima,
            notificacionesService,
            this
        )
    }

    private fun inicializarVistas() {
        recyclerNotificaciones = findViewById(R.id.recyclerNotificaciones)
        tvContador = findViewById(R.id.tvContador)
        tvMarcarTodas = findViewById(R.id.tvMarcarTodas)
        btnCerrar = findViewById(R.id.btnCerrar)

        recyclerNotificaciones.layoutManager = LinearLayoutManager(this)

        btnCerrar.setOnClickListener {
            finish()
        }

        tvMarcarTodas.setOnClickListener {
            marcarTodasComoLeidas()
        }

        // Configurar adapter
        notificacionesAdapter = NotificacionesAdapter(
            notificaciones = notificaciones,
            onItemClick = { notificacion ->
                mostrarDetalleNotificacion(notificacion)
            },
            onVerOfertaClick = { notificacion ->
                verOferta(notificacion)
            }
        )
        recyclerNotificaciones.adapter = notificacionesAdapter
    }

    /**
     * Carga las notificaciones del usuario.
     * Equivalente a mostrarNotificaciones() del diagrama UML.
     */
    private fun cargarNotificaciones() {
        notificaciones = controlNotificaciones.cargarRecordatorios(usuarioId)
        notificacionesAdapter = NotificacionesAdapter(
            notificaciones = notificaciones,
            onItemClick = { notificacion ->
                mostrarDetalleNotificacion(notificacion)
            },
            onVerOfertaClick = { notificacion ->
                verOferta(notificacion)
            }
        )
        recyclerNotificaciones.adapter = notificacionesAdapter

        // Actualizar contador de no leídas
        val noLeidas = notificaciones.count { !it.leida }
        tvContador.text = "$noLeidas sin leer"

        if (notificaciones.isEmpty()) {
            Toast.makeText(this, "No tienes notificaciones", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra el detalle de una notificación.
     * Equivalente a mostrarDetalleNotificacion(notificacion) del diagrama UML.
     * 
     * @param notificacion Notificación a mostrar
     */
    private fun mostrarDetalleNotificacion(notificacion: Notificacion) {
        // Marcar como leída si no lo está
        if (!notificacion.leida) {
            controlNotificaciones.marcarComoLeida(notificacion.id)
            // Recargar notificaciones para actualizar el contador
            cargarNotificaciones()
        }

        // Mostrar diálogo con detalles
        val mensaje = buildString {
            append(notificacion.descripcion)
            if (!notificacion.puntoEncuentro.isNullOrEmpty()) {
                append("\n\nPunto de encuentro: ${notificacion.puntoEncuentro}")
            }
            if (!notificacion.horaTour.isNullOrEmpty()) {
                append("\nHora: ${notificacion.horaTour}")
            }
            if (!notificacion.recomendaciones.isNullOrEmpty()) {
                append("\n\nRecomendación: ${notificacion.recomendaciones}")
            }
            if (notificacion.descuento != null) {
                append("\n\nDescuento: ${notificacion.descuento}%")
            }
        }

        android.app.AlertDialog.Builder(this)
            .setTitle(notificacion.titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    /**
     * Muestra la oferta cuando el usuario hace clic en "Ver oferta".
     * 
     * @param notificacion Notificación de oferta
     */
    private fun verOferta(notificacion: Notificacion) {
        if (notificacion.tourId != null) {
            // Extraer destinoId del tourId
            val destinoId = if (notificacion.tourId.contains("_")) {
                val partes = notificacion.tourId.split("_")
                "${partes[0]}_${partes[1]}"
            } else {
                notificacion.tourId
            }

            // Abrir detalle del destino
            val intent = Intent(this, DetalleDestinoActivity::class.java)
            intent.putExtra("DESTINO_ID", destinoId)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        }
    }

    /**
     * Marca todas las notificaciones como leídas.
     */
    private fun marcarTodasComoLeidas() {
        val marcadas = controlNotificaciones.marcarTodasComoLeidas(usuarioId)
        if (marcadas > 0) {
            Toast.makeText(this, "$marcadas notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show()
            cargarNotificaciones()
        } else {
            Toast.makeText(this, "No hay notificaciones para marcar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar notificaciones al volver a la actividad
        cargarNotificaciones()
    }
}

