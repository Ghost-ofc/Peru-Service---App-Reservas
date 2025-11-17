package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.NotificacionesAdapter
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.viewmodel.NotificacionesViewModel

/**
 * Activity para mostrar las notificaciones del usuario.
 * Equivalente a NotificacionesActivity del diagrama UML.
 */
class NotificacionesActivity : AppCompatActivity() {

    private var usuarioId: Int = 0
    private lateinit var viewModel: NotificacionesViewModel
    private lateinit var recyclerNotificaciones: RecyclerView
    private lateinit var tvContador: TextView
    private lateinit var btnCerrar: ImageView
    private lateinit var tvMarcarTodas: TextView
    private lateinit var notificacionesAdapter: NotificacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        usuarioId = intent.getIntExtra("USUARIO_ID", 0)

        if (usuarioId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(NotificacionesViewModel::class.java)

        inicializarVistas()
        configurarRecyclerView()
        configurarListeners()
        observarViewModel()
        cargarNotificaciones()
    }

    private fun inicializarVistas() {
        recyclerNotificaciones = findViewById(R.id.recyclerNotificaciones)
        tvContador = findViewById(R.id.tvContador)
        btnCerrar = findViewById(R.id.btnCerrar)
        tvMarcarTodas = findViewById(R.id.tvMarcarTodas)
    }

    private fun configurarRecyclerView() {
        notificacionesAdapter = NotificacionesAdapter(
            onItemClick = { notificacion ->
                mostrarDetalleNotificacion(notificacion)
            },
            onMarcarLeida = { notificacionId ->
                viewModel.marcarComoLeida(notificacionId)
            }
        )

        recyclerNotificaciones.apply {
            layoutManager = LinearLayoutManager(this@NotificacionesActivity)
            adapter = notificacionesAdapter
        }
    }

    private fun configurarListeners() {
        btnCerrar.setOnClickListener {
            finish()
        }

        tvMarcarTodas.setOnClickListener {
            viewModel.marcarTodasComoLeidas(usuarioId)
        }
    }

    private fun observarViewModel() {
        viewModel.recordatorios.observe(this) { notificaciones ->
            mostrarNotificaciones(notificaciones)
            actualizarContador(notificaciones)
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarNotificaciones() {
        viewModel.cargarRecordatoriosUsuario(usuarioId)
    }

    /**
     * Muestra las notificaciones en la lista.
     * Equivalente a mostrarNotificaciones() del diagrama UML.
     */
    private fun mostrarNotificaciones(notificaciones: List<Notificacion>) {
        notificacionesAdapter.actualizarLista(notificaciones)
    }

    /**
     * Muestra el detalle de una notificación.
     * Equivalente a mostrarDetalleNotificacion(notificacion) del diagrama UML.
     */
    private fun mostrarDetalleNotificacion(notificacion: Notificacion) {
        // Por ahora solo mostrar un Toast con la descripción completa
        val mensaje = when (notificacion.tipo) {
            com.grupo4.appreservas.modelos.TipoNotificacion.RECORDATORIO -> {
                "${notificacion.descripcion}\nHora: ${notificacion.horaTour}\nPunto de encuentro: ${notificacion.puntoEncuentro}"
            }
            com.grupo4.appreservas.modelos.TipoNotificacion.ALERTA_CLIMATICA -> {
                "${notificacion.descripcion}\nRecomendaciones: ${notificacion.recomendaciones}"
            }
            com.grupo4.appreservas.modelos.TipoNotificacion.OFERTA_ULTIMO_MINUTO -> {
                "${notificacion.descripcion}\nDescuento: ${notificacion.descuento}%"
            }
            else -> notificacion.descripcion
        }
        
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun actualizarContador(notificaciones: List<Notificacion>) {
        val noLeidas = notificaciones.count { !it.leida }
        tvContador.text = "$noLeidas sin leer"
    }
}

