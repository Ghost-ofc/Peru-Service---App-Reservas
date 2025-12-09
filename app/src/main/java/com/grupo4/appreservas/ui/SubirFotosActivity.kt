package com.grupo4.appreservas.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.FotosSeleccionadasAdapter
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.AlbumTourViewModel

/**
 * Activity para subir fotos al álbum del tour.
 * Equivalente a SubirFotosActivity del diagrama UML.
 */
class SubirFotosActivity : AppCompatActivity() {

    private var tourId: String = ""
    private var tourNombre: String = ""
    private var usuarioId: Int = 0
    private lateinit var viewModel: AlbumTourViewModel
    private lateinit var recyclerFotosSeleccionadas: RecyclerView
    private lateinit var btnElegirFotos: TextView
    private lateinit var btnSubir: TextView
    private lateinit var btnVolver: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvSubtitulo: TextView
    private lateinit var tvFotosSeleccionadas: TextView
    private lateinit var fotosSeleccionadasAdapter: FotosSeleccionadasAdapter
    private val fotosSeleccionadas = mutableListOf<Uri>()

    companion object {
        private const val REQUEST_CODE_SELECCIONAR_IMAGENES = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subir_fotos)

        tourId = intent.getStringExtra("TOUR_ID") ?: ""
        tourNombre = intent.getStringExtra("TOUR_NOMBRE") ?: "Tour"
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)

        if (tourId.isEmpty()) {
            Toast.makeText(this, "Error: No se proporcionó información del tour", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(AlbumTourViewModel::class.java)

        inicializarVistas()
        configurarRecyclerView()
        configurarListeners()
        observarViewModel()
    }

    private fun inicializarVistas() {
        recyclerFotosSeleccionadas = findViewById(R.id.recyclerFotosSeleccionadas)
        btnElegirFotos = findViewById(R.id.btnElegirFotos)
        btnSubir = findViewById(R.id.btnSubir)
        btnVolver = findViewById(R.id.btnVolver)
        tvTitulo = findViewById(R.id.tvTitulo)
        tvSubtitulo = findViewById(R.id.tvSubtitulo)
        tvFotosSeleccionadas = findViewById(R.id.tvFotosSeleccionadas)

        tvTitulo.text = "Subir Fotos"
        tvSubtitulo.text = tourNombre
        actualizarContadorFotos()
    }

    private fun configurarRecyclerView() {
        fotosSeleccionadasAdapter = FotosSeleccionadasAdapter(
            onEliminarFoto = { posicion ->
                eliminarFoto(posicion)
            }
        )

        recyclerFotosSeleccionadas.apply {
            layoutManager = GridLayoutManager(this@SubirFotosActivity, 3)
            adapter = fotosSeleccionadasAdapter
        }
    }

    private fun configurarListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnElegirFotos.setOnClickListener {
            seleccionarFotos(tourId)
        }

        btnSubir.setOnClickListener {
            if (fotosSeleccionadas.isNotEmpty()) {
                enviarFotosSeleccionadas()
            } else {
                Toast.makeText(this, "Por favor selecciona al menos una foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observarViewModel() {
        viewModel.mensajeEstado.observe(this) { mensaje ->
            mensaje?.let {
                mostrarMensaje(it)
                if (it.contains("correctamente", ignoreCase = true)) {
                    mostrarPantallaConfirmacion()
                }
            }
        }
    }

    /**
     * Muestra la pantalla de confirmación de subida.
     * Equivalente a mostrarPantallaConfirmacion() del diagrama UML.
     */
    private fun mostrarPantallaConfirmacion() {
        // Esperar un momento y volver al álbum
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            finish()
        }, 1500)
    }

    /**
     * Selecciona fotos para un tour.
     * Equivalente a seleccionarFotos(idTour) del diagrama UML.
     */
    private fun seleccionarFotos(idTour: String) {
        mostrarSelectorImagenes()
    }

    /**
     * Muestra el selector de imágenes.
     * Equivalente a mostrarSelectorImagenes() del diagrama UML.
     */
    private fun mostrarSelectorImagenes() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, REQUEST_CODE_SELECCIONAR_IMAGENES)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECCIONAR_IMAGENES && resultCode == Activity.RESULT_OK) {
            data?.let {
                if (it.clipData != null) {
                    // Múltiples imágenes seleccionadas
                    val clipData = it.clipData
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        fotosSeleccionadas.add(uri)
                    }
                } else if (it.data != null) {
                    // Una sola imagen seleccionada
                    fotosSeleccionadas.add(it.data!!)
                }
                actualizarFotosSeleccionadas()
            }
        }
    }

    private fun actualizarFotosSeleccionadas() {
        fotosSeleccionadasAdapter.actualizarLista(fotosSeleccionadas)
        actualizarContadorFotos()
    }

    private fun eliminarFoto(posicion: Int) {
        if (posicion < fotosSeleccionadas.size) {
            fotosSeleccionadas.removeAt(posicion)
            actualizarFotosSeleccionadas()
        }
    }

    private fun actualizarContadorFotos() {
        val cantidad = fotosSeleccionadas.size
        if (cantidad > 0) {
            tvFotosSeleccionadas.text = "Fotos seleccionadas ($cantidad)"
        } else {
            tvFotosSeleccionadas.text = "No hay fotos seleccionadas aún"
        }
        btnSubir.text = if (cantidad > 0) "Subir $cantidad foto${if (cantidad > 1) "s" else ""}" else "Subir fotos"
    }

    /**
     * Envía las fotos seleccionadas para subirlas.
     * Equivalente a enviarFotosSeleccionadas(idTour, rutasImagenes) del diagrama UML.
     */
    private fun enviarFotosSeleccionadas() {
        mostrarProgresoSubida()
        
        // Obtener nombre del usuario
        val repository = PeruvianServiceRepository.getInstance(this)
        val usuario = repository.buscarUsuarioPorId(usuarioId)
        val nombreAutor = usuario?.nombreCompleto ?: "Usuario"

        // Convertir URIs a strings (rutas)
        val rutasImagenes = fotosSeleccionadas.map { it.toString() }
        
        viewModel.subirFotosSeleccionadas(tourId, rutasImagenes, nombreAutor)
    }

    /**
     * Muestra el progreso de la subida.
     * Equivalente a mostrarProgresoSubida() del diagrama UML.
     */
    private fun mostrarProgresoSubida() {
        btnSubir.isEnabled = false
        btnSubir.text = "Subiendo..."
        Toast.makeText(this, "Subiendo fotos...", Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra un mensaje al usuario.
     */
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        btnSubir.isEnabled = true
    }
}

