package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.DestinosAdapter
import com.grupo4.appreservas.controller.CatalogoController
import com.grupo4.appreservas.controller.FiltrosController
import com.grupo4.appreservas.controller.ControlNotificaciones
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.RepositorioNotificaciones
import com.grupo4.appreservas.repository.RepositorioOfertas
import com.grupo4.appreservas.repository.RepositorioClima
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinoService
import com.grupo4.appreservas.service.NotificacionesService
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

class CatalogoActivity : AppCompatActivity() {

    private lateinit var catalogoController: CatalogoController
    private lateinit var filtrosController: FiltrosController
    private lateinit var controlNotificaciones: ControlNotificaciones

    private lateinit var recyclerDestinos: RecyclerView
    private lateinit var chipGroupCategorias: ChipGroup
    private lateinit var editBuscar: EditText
    private lateinit var btnFiltros: ImageView
    private lateinit var topAppBar: MaterialToolbar

    private lateinit var destinosAdapter: DestinosAdapter
    private var destinosList = listOf<Destino>()
    private var destinosFiltrados = listOf<Destino>()
    private var usuarioId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        obtenerDatosUsuario()
        inicializarDependencias()
        inicializarVistas()
        configurarRecyclerView()
        configurarBuscador()
        configurarToolbar()
        cargarDestinos()
        verificarPermisosNotificaciones()
    }

    private fun obtenerDatosUsuario() {
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
    }

    private fun inicializarDependencias() {
        val destinoRepo = DestinoRepository.getInstance(this)
        val bookingRepo = ReservasRepository.getInstance(this)
        val destinoService = DestinoService(destinoRepo)
        val availabilityService = AvailabilityService(destinoRepo, bookingRepo)

        catalogoController = CatalogoController(destinoService, availabilityService)
        filtrosController = FiltrosController(destinoService)

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
        recyclerDestinos = findViewById(R.id.recyclerDestinos)
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias)
        editBuscar = findViewById(R.id.editBuscar)
        btnFiltros = findViewById(R.id.btnFiltros)
        topAppBar = findViewById(R.id.topAppBar)

        setSupportActionBar(topAppBar)

        btnFiltros.setOnClickListener {
            abrirFiltros()
        }
    }

    private fun configurarToolbar() {
        // El menú se maneja en onCreateOptionsMenu
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_topbar, menu)
        
        // Configurar badge de notificaciones
        val menuItem = menu.findItem(R.id.action_notifications)
        if (menuItem != null && usuarioId > 0) {
            actualizarBadgeNotificaciones(menuItem)
        }
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                abrirNotificaciones()
                true
            }
            R.id.action_favorite -> {
                // TODO: Implementar favoritos
                Toast.makeText(this, "Favoritos (próximamente)", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_profile -> {
                abrirPerfil()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Actualiza el badge de notificaciones con el número de no leídas.
     */
    private fun actualizarBadgeNotificaciones(menuItem: MenuItem) {
        if (usuarioId > 0) {
            val noLeidas = controlNotificaciones.cargarRecordatorios(usuarioId)
                .count { !it.leida }
            
            if (noLeidas > 0) {
                // Mostrar el número en el título (más compatible)
                menuItem.title = "Notificaciones ($noLeidas)"
                // Intentar crear badge si está disponible (requiere Material Components)
                try {
                    val badgeDrawable = BadgeDrawable.create(this)
                    badgeDrawable.number = noLeidas
                    badgeDrawable.backgroundColor = ContextCompat.getColor(this, android.R.color.holo_red_dark)
                    BadgeUtils.attachBadgeDrawable(badgeDrawable, topAppBar, R.id.action_notifications)
                } catch (e: Exception) {
                    // Si BadgeDrawable no está disponible, solo usar el título
                    android.util.Log.d("CatalogoActivity", "BadgeDrawable no disponible: ${e.message}")
                }
            } else {
                menuItem.title = "Notificaciones"
            }
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
     * Abre la actividad de perfil/recompensas (HU-007).
     */
    private fun abrirPerfil() {
        if (usuarioId > 0) {
            val intent = Intent(this, RecompensasActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Verifica y solicita permisos de notificaciones (Android 13+).
     */
    private fun verificarPermisosNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                Toast.makeText(this, "Los permisos de notificaciones son necesarios para recibir alertas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar badge de notificaciones al volver a la actividad
        if (::controlNotificaciones.isInitialized && usuarioId > 0) {
            invalidateOptionsMenu()
            // Ejecutar tareas de notificaciones en segundo plano
            ejecutarTareasNotificaciones()
        }
    }

    /**
     * Ejecuta las tareas de notificaciones (recordatorios, alertas climáticas, ofertas).
     * Estas tareas se ejecutan cuando el usuario abre la aplicación.
     */
    private fun ejecutarTareasNotificaciones() {
        if (usuarioId > 0) {
            // Ejecutar en un hilo separado para no bloquear la UI
            Thread {
                try {
                    // Generar recordatorios de tours próximos
                    controlNotificaciones.generarRecordatoriosTours(usuarioId)
                    
                    // Detectar cambios climáticos
                    controlNotificaciones.detectarCambioClimatico(usuarioId)
                    
                    // Generar ofertas de último minuto
                    controlNotificaciones.generarOfertaUltimoMinuto(usuarioId)
                } catch (e: Exception) {
                    android.util.Log.e("CatalogoActivity", "Error al ejecutar tareas de notificaciones: ${e.message}", e)
                }
            }.start()
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
        val criterios = mapOf("categoria" to categoria)
        destinosFiltrados = filtrosController.filtrarDestinos(criterios)
        destinosAdapter.actualizarLista(destinosFiltrados)
    }

    private fun abrirFiltros() {
        val intent = Intent(this, FiltrosActivity::class.java)
        startActivityForResult(intent, REQUEST_FILTROS)
    }

    /**
     * Abre la actividad de detalle de destino pasando solo el ID.
     * Esto es más eficiente ya que el destino se carga desde SQLite.
     */
    private fun abrirDetalle(destino: Destino) {
        val intent = Intent(this, DetalleDestinoActivity::class.java)
        // Pasar solo el ID en lugar del objeto completo (más eficiente)
        intent.putExtra("DESTINO_ID", destino.id)
        
        // Pasar USUARIO_ID si está disponible en el Intent
        val usuarioId = getIntent().getIntExtra("USUARIO_ID", 0)
        if (usuarioId > 0) {
            intent.putExtra("USUARIO_ID", usuarioId)
        }
        
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_FILTROS && resultCode == RESULT_OK) {
            val categoria = data?.getStringExtra("categoria")
            val precioMin = data?.getDoubleExtra("precioMin", 0.0) ?: 0.0
            val precioMax = data?.getDoubleExtra("precioMax", Double.MAX_VALUE) ?: Double.MAX_VALUE

            val criterios = mutableMapOf<String, Any>()
            categoria?.let { criterios["categoria"] = it }
            if (precioMin > 0) criterios["precioMin"] = precioMin
            if (precioMax < Double.MAX_VALUE) criterios["precioMax"] = precioMax

            destinosFiltrados = filtrosController.filtrarDestinos(criterios)
            destinosAdapter.actualizarLista(destinosFiltrados)
        }
    }

    companion object {
        private const val REQUEST_FILTROS = 100
        private const val REQUEST_NOTIFICATION_PERMISSION = 200
    }
}