package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.DestinosAdapter
import com.grupo4.appreservas.controller.CatalogoController
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.service.DestinoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity para visualizar el catálogo de destinos turísticos.
 * Corresponde a la HU: Visualización del catálogo de destinos.
 */
class CatalogoActivity : AppCompatActivity() {

    private lateinit var catalogoController: CatalogoController
    private lateinit var recyclerDestinos: RecyclerView
    private lateinit var chipGroupCategorias: ChipGroup
    private lateinit var editBuscar: EditText
    private lateinit var btnFiltros: ImageView
    
    // Header views
    private lateinit var topBar: LinearLayout
    private lateinit var ivNotificaciones: ImageView
    private lateinit var tvBadgeNotificaciones: TextView
    private lateinit var ivPerfil: ImageView
    private lateinit var ivLogout: ImageView

    private lateinit var destinosAdapter: DestinosAdapter
    private var destinosList = listOf<Destino>()
    private var destinosFiltrados = listOf<Destino>()
    private var usuarioId: Int = 0
    private lateinit var repository: PeruvianServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        if (usuarioId == 0) {
            Toast.makeText(this, "Error: ID de usuario no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarDependencias()
        inicializarVistas()
        configurarSafeArea()
        configurarRecyclerView()
        configurarBuscador()
        configurarHeader()
        cargarDestinos()
        actualizarBadgeNotificaciones()
    }

    private fun inicializarDependencias() {
        repository = PeruvianServiceRepository.getInstance(this)
        val destinoService = DestinoService(repository)
        catalogoController = CatalogoController(destinoService)
    }

    private fun inicializarVistas() {
        recyclerDestinos = findViewById(R.id.recyclerDestinos)
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias)
        editBuscar = findViewById(R.id.editBuscar)
        btnFiltros = findViewById(R.id.btnFiltros)
        
        // Header views
        topBar = findViewById(R.id.topBar)
        ivNotificaciones = findViewById(R.id.iv_notificaciones)
        tvBadgeNotificaciones = findViewById(R.id.tv_badge_notificaciones)
        ivPerfil = findViewById(R.id.iv_perfil)
        ivLogout = findViewById(R.id.iv_logout)

        btnFiltros.setOnClickListener {
            // Los filtros avanzados se pueden implementar más adelante
            Toast.makeText(this, "Filtros (próximamente)", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Configura el padding del header para respetar el notch y la barra de estado.
     * Esto asegura que los iconos de notificaciones, perfil y logout sean accesibles.
     */
    private fun configurarSafeArea() {
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusBarHeight = systemBars.top
            
            // Aplicar padding superior dinámico basado en la altura de la barra de estado
            // Mantener el padding lateral e inferior original (16dp)
            val paddingDp = 16
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
    
    private fun configurarHeader() {
        // Click en notificaciones
        ivNotificaciones.setOnClickListener {
            abrirNotificaciones()
        }
        
        // Click en perfil
        ivPerfil.setOnClickListener {
            val intent = Intent(this, RecompensasActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        }
        
        // Click en logout
        ivLogout.setOnClickListener {
            // Cerrar sesión y volver al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * Abre la pantalla de notificaciones.
     * Equivalente a abrirNotificaciones() del diagrama UML.
     */
    private fun abrirNotificaciones() {
        val intent = Intent(this, NotificacionesActivity::class.java)
        intent.putExtra("USUARIO_ID", usuarioId)
        startActivity(intent)
    }

    /**
     * Muestra el icono de notificaciones con el contador.
     * Equivalente a mostrarIconoNotificaciones(contador) del diagrama UML.
     */
    private fun mostrarIconoNotificaciones(contador: Int) {
        if (contador > 0) {
            tvBadgeNotificaciones.text = contador.toString()
            tvBadgeNotificaciones.visibility = TextView.VISIBLE
        } else {
            tvBadgeNotificaciones.visibility = TextView.GONE
        }
    }

    private fun actualizarBadgeNotificaciones() {
        CoroutineScope(Dispatchers.Main).launch {
            val notificacionesNoLeidas: List<Notificacion> = withContext<List<Notificacion>>(Dispatchers.IO) {
                repository.obtenerNotificacionesNoLeidasPorUsuario(usuarioId)
            }
            
            mostrarIconoNotificaciones(notificacionesNoLeidas.size)
        }
    }

    private fun configurarRecyclerView() {
        destinosAdapter = DestinosAdapter { destino ->
            abrirDetalle(destino)
        }

        recyclerDestinos.apply {
            layoutManager = LinearLayoutManager(this@CatalogoActivity)
            adapter = destinosAdapter
        }
    }

    private fun configurarBuscador() {
        editBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarPorTexto(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarPorTexto(query: String) {
        if (query.isEmpty()) {
            destinosAdapter.actualizarLista(destinosList)
        } else {
            val filtrados = destinosList.filter { destino ->
                destino.nombre.contains(query, ignoreCase = true) ||
                        destino.ubicacion.contains(query, ignoreCase = true) ||
                        destino.descripcion.contains(query, ignoreCase = true)
            }
            destinosAdapter.actualizarLista(filtrados)
        }
    }

    private fun cargarDestinos() {
        try {
            destinosList = catalogoController.solicitarDestinos()
            destinosFiltrados = destinosList
            destinosAdapter.actualizarLista(destinosList)
            configurarChipsCategorias()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al cargar destinos: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun configurarChipsCategorias() {
        chipGroupCategorias.removeAllViews()

        // Chip "Todos"
        val chipTodos = Chip(this).apply {
            text = "Todos"
            isCheckable = true
            isChecked = true
            setChipBackgroundColorResource(R.color.color_background)
            setTextColor(getColor(R.color.color_text))
            chipStrokeWidth = 1f
            setChipStrokeColorResource(R.color.color_outline)
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mostrarTodos()
                }
            }
        }
        chipGroupCategorias.addView(chipTodos)

        // Extraer categorías únicas
        val categorias = destinosList
            .flatMap { it.categorias }
            .distinct()
            .sorted()

        categorias.forEach { categoria ->
            val chip = Chip(this).apply {
                text = categoria
                isCheckable = true
                setChipBackgroundColorResource(R.color.color_background)
                setTextColor(getColor(R.color.color_text))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.color_outline)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        filtrarPorCategoria(categoria)
                    }
                }
            }
            chipGroupCategorias.addView(chip)
        }
    }

    private fun mostrarTodos() {
        editBuscar.text.clear()
        destinosFiltrados = destinosList
        destinosAdapter.actualizarLista(destinosList)
    }

    private fun filtrarPorCategoria(categoria: String) {
        editBuscar.text.clear()
        val filtrados = destinosList.filter { destino ->
            destino.categorias.contains(categoria)
        }
        destinosFiltrados = filtrados
        destinosAdapter.actualizarLista(filtrados)
    }

    private fun abrirDetalle(destino: Destino) {
        val intent = Intent(this, DetalleDestinoActivity::class.java)
        intent.putExtra("DESTINO_ID", destino.id)
        intent.putExtra("USUARIO_ID", usuarioId)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Actualizar badge de notificaciones cuando se vuelve a la actividad
        actualizarBadgeNotificaciones()
    }
}
