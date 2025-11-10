package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.ToursAdapter
import com.grupo4.appreservas.controller.ControlGuia
import com.grupo4.appreservas.controller.ControlNotificaciones
import com.grupo4.appreservas.repository.RepositorioAsignaciones
import com.grupo4.appreservas.repository.RepositorioNotificaciones
import com.grupo4.appreservas.repository.RepositorioOfertas
import com.grupo4.appreservas.repository.RepositorioClima
import com.grupo4.appreservas.service.NotificacionesService

/**
 * Panel de Guía Activity según el diagrama UML.
 * Equivalente a PanelGuiaActivity del diagrama.
 * 
 * En arquitectura MVC, esta Activity (Vista) usa el ControlGuia (Controller)
 * para manejar la lógica de tours.
 */
class PanelGuiaActivity : AppCompatActivity() {

    private lateinit var controlGuia: ControlGuia
    private lateinit var controlNotificaciones: ControlNotificaciones
    private lateinit var recyclerTours: RecyclerView
    private lateinit var toursAdapter: ToursAdapter
    private lateinit var btnSalir: Button
    private lateinit var ivNotificaciones: ImageView
    private var usuarioId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_guia)

        obtenerDatosUsuario()
        inicializarDependencias()
        inicializarVistas()
        configurarBotones()
        mostrarToursDelDia()
    }

    private fun obtenerDatosUsuario() {
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        if (usuarioId == 0) {
            usuarioId = intent.getIntExtra("GUIA_ID", 1)
        }
    }

    private fun inicializarDependencias() {
        // Inicializar repositorio y controlador según arquitectura MVC
        val repositorioAsignaciones = RepositorioAsignaciones(this)
        controlGuia = ControlGuia(repositorioAsignaciones)

        // Inicializar controlador de notificaciones
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
        recyclerTours = findViewById(R.id.recycler_tours)
        recyclerTours.layoutManager = LinearLayoutManager(this)
        btnSalir = findViewById(R.id.btn_salir)
        ivNotificaciones = findViewById(R.id.iv_notificaciones)
    }

    private fun configurarBotones() {
        btnSalir.setOnClickListener {
            mostrarDialogoConfirmacionSalir()
        }

        ivNotificaciones.setOnClickListener {
            abrirNotificaciones()
        }
    }

    /**
     * Abre la actividad de notificaciones.
     */
    private fun abrirNotificaciones() {
        if (usuarioId > 0) {
            val intent = Intent(this, NotificacionesActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra todos los tours asignados al guía, ordenados por fecha ascendente.
     * Modificado para mostrar todos los tours, no solo los del día actual.
     */
    private fun mostrarToursDelDia() {
        // Obtener USUARIO_ID del Intent (viene de PanelPrincipalActivity)
        val usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        val guiaId = if (usuarioId > 0) usuarioId else {
            // Si no viene USUARIO_ID, intentar obtener GUIA_ID (compatibilidad)
            intent.getIntExtra("GUIA_ID", 1)
        }
        
        // Usar ControlGuia para cargar TODOS los tours del guía (ordenados por fecha ascendente)
        val tours = controlGuia.cargarTodosLosTours(guiaId)

        toursAdapter = ToursAdapter(tours) { tour ->
            abrirDetalleTour(tour.tourId)
        }
        recyclerTours.adapter = toursAdapter

        if (tours.isEmpty()) {
            Toast.makeText(this, "No tienes tours asignados", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Abre el detalle de un tour específico.
     * Equivalente a abrirDetalleTour(tourId) del diagrama UML.
     * 
     * @param tourId ID del tour
     */
    private fun abrirDetalleTour(tourId: String) {
        val intent = Intent(this, DetalleTourActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        // Pasar USUARIO_ID (o GUIA_ID) para que DetalleTourActivity pueda pasarlo a EscaneoQRActivity
        val usuarioId = this.intent.getIntExtra("USUARIO_ID", 0)
        if (usuarioId > 0) {
            intent.putExtra("GUIA_ID", usuarioId)
        } else {
            val guiaId = this.intent.getIntExtra("GUIA_ID", 1)
            intent.putExtra("GUIA_ID", guiaId)
        }
        startActivity(intent)
    }

    /**
     * Muestra un diálogo de confirmación antes de salir.
     */
    private fun mostrarDialogoConfirmacionSalir() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas salir?")
            .setPositiveButton("Sí, Salir") { _, _ ->
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Cierra la sesión del usuario y redirige al login.
     */
    private fun cerrarSesion() {
        // Limpiar cualquier dato de sesión si es necesario
        // Por ahora, solo redirigimos al login
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }
}
