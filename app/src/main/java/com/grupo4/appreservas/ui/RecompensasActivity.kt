package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.HistorialViajesAdapter
import com.grupo4.appreservas.adapter.LogrosAdapter
import com.grupo4.appreservas.modelos.Logro
import com.grupo4.appreservas.modelos.PuntosUsuario
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.RecompensasViewModel

/**
 * Activity para mostrar las recompensas (puntos y logros) del usuario.
 * Equivalente a PerfilUsuarioActivity del diagrama UML.
 * Incluye funcionalidad de PerfilActivity según el diagrama.
 */
class RecompensasActivity : AppCompatActivity() {

    private var usuarioId: Int = 0
    private lateinit var viewModel: RecompensasViewModel
    private lateinit var headerLayout: LinearLayout
    private lateinit var tvPuntos: TextView
    private lateinit var tvNivelUsuario: TextView
    private lateinit var tvPuntosParaSiguiente: TextView
    private lateinit var tvToursCompletados: TextView
    private lateinit var tvLogrosDesbloqueados: TextView
    private lateinit var viewProgressBar: View
    private lateinit var frameProgressBar: View
    private lateinit var recyclerLogros: RecyclerView
    private lateinit var recyclerHistorialViajes: RecyclerView
    private lateinit var btnCerrar: ImageView
    private lateinit var tvVolverInicio: TextView
    private lateinit var logrosAdapter: LogrosAdapter
    private lateinit var historialViajesAdapter: HistorialViajesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recompensas)

        usuarioId = intent.getIntExtra("USUARIO_ID", 0)

        if (usuarioId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(RecompensasViewModel::class.java)

        inicializarVistas()
        configurarSafeArea()
        configurarRecyclerView()
        configurarListeners()
        observarViewModel()
        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando la Activity vuelve a estar visible
        // Esto asegura que se muestren los estados actualizados de las reservas
        cargarDatos()
    }

    private fun inicializarVistas() {
        headerLayout = findViewById(R.id.headerLayout)
        tvPuntos = findViewById(R.id.tvPuntos)
        tvNivelUsuario = findViewById(R.id.tvNivelUsuario)
        tvPuntosParaSiguiente = findViewById(R.id.tvPuntosParaSiguiente)
        tvToursCompletados = findViewById(R.id.tvToursCompletados)
        tvLogrosDesbloqueados = findViewById(R.id.tvLogrosDesbloqueados)
        viewProgressBar = findViewById(R.id.viewProgressBar)
        frameProgressBar = findViewById(R.id.frameProgressBar)
        recyclerLogros = findViewById(R.id.recyclerLogros)
        recyclerHistorialViajes = findViewById(R.id.recyclerHistorialViajes)
        btnCerrar = findViewById(R.id.btnCerrar)
        tvVolverInicio = findViewById(R.id.tvVolverInicio)
        
        // Cargar nombre del usuario
        val repository = PeruvianServiceRepository.getInstance(this)
        val usuario = repository.buscarUsuarioPorId(usuarioId)
        usuario?.let {
            findViewById<TextView>(R.id.tvNombreUsuario).text = it.nombreCompleto
        }
    }
    
    /**
     * Configura el padding del header para respetar el notch y la barra de estado.
     * Esto asegura que el botón cerrar sea accesible.
     */
    private fun configurarSafeArea() {
        ViewCompat.setOnApplyWindowInsetsListener(headerLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusBarHeight = systemBars.top
            
            // Aplicar padding superior dinámico basado en la altura de la barra de estado
            // Mantener el padding lateral e inferior original (24dp)
            val paddingDp = 24
            val paddingPx = (paddingDp * resources.displayMetrics.density).toInt()
            
            view.updatePadding(
                top = statusBarHeight + paddingPx,
                bottom = paddingPx,
                left = paddingPx,
                right = paddingPx
            )
            
            insets
        }
    }

    private fun configurarRecyclerView() {
        logrosAdapter = LogrosAdapter()
        historialViajesAdapter = HistorialViajesAdapter { reserva ->
            // Si la reserva está confirmada, abrir el álbum de fotos
            // Si está pendiente o cancelada, abrir el detalle del destino (para reservar nuevamente si se desea)
            if (reserva.estado == com.grupo4.appreservas.modelos.EstadoReserva.CONFIRMADO) {
                // Abrir álbum de fotos del tour
                abrirAlbum(reserva.tourId)
            } else {
                // Abrir detalle del destino (para reservar o ver información)
                reserva.destino?.let { destino ->
                    val intent = Intent(this, DetalleDestinoActivity::class.java)
                    intent.putExtra("DESTINO_ID", destino.id)
                    intent.putExtra("USUARIO_ID", usuarioId)
                    startActivity(intent)
                } ?: run {
                    Toast.makeText(this, "No se encontró información del destino", Toast.LENGTH_SHORT).show()
                }
            }
        }

        recyclerLogros.apply {
            layoutManager = LinearLayoutManager(this@RecompensasActivity)
            adapter = logrosAdapter
        }

        recyclerHistorialViajes.apply {
            layoutManager = LinearLayoutManager(this@RecompensasActivity)
            adapter = historialViajesAdapter
        }
    }

    private fun configurarListeners() {
        btnCerrar.setOnClickListener {
            finish()
        }

        tvVolverInicio.setOnClickListener {
            // Volver al catálogo
            val intent = Intent(this, CatalogoActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
            finish()
        }
    }

    private fun observarViewModel() {
        viewModel.puntos.observe(this) { puntos ->
            mostrarPuntos(puntos)
        }

        viewModel.logros.observe(this) { logros ->
            mostrarLogros(logros)
        }

        viewModel.toursCompletados.observe(this) { cantidad ->
            tvToursCompletados.text = cantidad.toString()
        }

        viewModel.reservas.observe(this) { reservas ->
            mostrarReservas(reservas)
        }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            // El mensaje de estado se puede usar para mostrar notificaciones si es necesario
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarDatos() {
        abrirPerfil(usuarioId)
    }

    /**
     * Abre el perfil de un usuario.
     * Equivalente a abrirPerfil(usuariold) del diagrama UML.
     */
    private fun abrirPerfil(usuarioId: Int) {
        viewModel.cargarResumenPuntosYLogros(usuarioId)
    }

    /**
     * Abre el álbum de un tour.
     * Equivalente a abrirAlbum(idTour) del diagrama UML (PerfilActivity).
     */
    private fun abrirAlbum(idTour: String) {
        val repository = PeruvianServiceRepository.getInstance(this)
        // Buscar el tour directamente por su ID
        val tour = repository.obtenerTourPorId(idTour)
        
        if (tour != null) {
            val intent = Intent(this, AlbumTourActivity::class.java)
            intent.putExtra("TOUR_ID", tour.tourId)
            intent.putExtra("TOUR_NOMBRE", tour.nombre)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Tour no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra los puntos del usuario.
     * Equivalente a mostrarPuntos(puntos) del diagrama UML.
     */
    private fun mostrarPuntos(puntos: Int) {
        tvPuntos.text = puntos.toString()
        
        // Calcular nivel y puntos para siguiente nivel
        val nivel = PuntosUsuario.calcularNivel(puntos)
        val puntosParaSiguiente = PuntosUsuario.calcularPuntosParaSiguienteNivel(puntos)
        
        tvNivelUsuario.text = nivel
        
        if (puntosParaSiguiente > 0) {
            tvPuntosParaSiguiente.text = "$puntosParaSiguiente puntos para el próximo nivel"
        } else {
            tvPuntosParaSiguiente.text = "¡Nivel máximo alcanzado!"
        }
        
        // Actualizar barra de progreso
        actualizarBarraProgreso(puntos, puntosParaSiguiente)
    }

    /**
     * Muestra los logros del usuario.
     * Equivalente a mostrarLogros(logros) del diagrama UML.
     */
    private fun mostrarLogros(logros: List<Logro>) {
        logrosAdapter.actualizarLista(logros)
        
        // Actualizar contador de logros desbloqueados
        val logrosDesbloqueados = logros.count { it.desbloqueado }
        tvLogrosDesbloqueados.text = logrosDesbloqueados.toString()
    }

    /**
     * Muestra las reservas del usuario en el historial.
     * Equivalente a mostrarReservas(reservas) del diagrama UML.
     */
    private fun mostrarReservas(reservas: List<Reserva>) {
        historialViajesAdapter.actualizarLista(reservas)
    }

    /**
     * Actualiza la barra de progreso según los puntos.
     */
    private fun actualizarBarraProgreso(puntos: Int, puntosParaSiguiente: Int) {
        val porcentaje = when {
            puntos < 501 -> (puntos.toFloat() / 501f) * 100f
            puntos < 1501 -> ((puntos - 501).toFloat() / 1000f) * 100f
            puntos < 3001 -> ((puntos - 1501).toFloat() / 1500f) * 100f
            else -> 100f
        }
        
        val layoutParams = viewProgressBar.layoutParams
        layoutParams.width = (frameProgressBar.width * porcentaje / 100f).toInt()
        viewProgressBar.layoutParams = layoutParams
    }
}

