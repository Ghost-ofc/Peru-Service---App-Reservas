package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.FotosAdapter
import com.grupo4.appreservas.modelos.Foto
import com.grupo4.appreservas.viewmodel.AlbumTourViewModel

/**
 * Activity para visualizar el álbum de fotos del tour.
 * Equivalente a AlbumTourActivity del diagrama UML.
 */
class AlbumTourActivity : AppCompatActivity() {

    private var tourId: String = ""
    private var tourNombre: String = ""
    private var usuarioId: Int = 0
    private lateinit var viewModel: AlbumTourViewModel
    private lateinit var recyclerFotos: RecyclerView
    private lateinit var btnSubirFotos: ImageView
    private lateinit var btnVolver: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvSubtitulo: TextView
    private lateinit var tvFechaTour: TextView
    private lateinit var tvFotosCompartidas: TextView
    private lateinit var fotosAdapter: FotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_tour)

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
        cargarFotos()
    }

    private fun inicializarVistas() {
        recyclerFotos = findViewById(R.id.recyclerFotos)
        btnSubirFotos = findViewById(R.id.btnSubirFotos)
        btnVolver = findViewById(R.id.btnVolver)
        tvTitulo = findViewById(R.id.tvTitulo)
        tvSubtitulo = findViewById(R.id.tvSubtitulo)
        tvFechaTour = findViewById(R.id.tvFechaTour)
        tvFotosCompartidas = findViewById(R.id.tvFotosCompartidas)

        tvTitulo.text = "Álbum del Tour"
        tvSubtitulo.text = tourNombre
    }

    private fun configurarRecyclerView() {
        fotosAdapter = FotosAdapter()

        recyclerFotos.apply {
            layoutManager = GridLayoutManager(this@AlbumTourActivity, 2)
            adapter = fotosAdapter
        }
    }

    private fun configurarListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnSubirFotos.setOnClickListener {
            abrirSubidaFotos()
        }
    }

    private fun observarViewModel() {
        viewModel.fotosAlbum.observe(this) { fotos ->
            mostrarFotos(fotos)
        }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            mensaje?.let {
                mostrarMensaje(it)
            }
        }
    }

    /**
     * Carga el álbum del tour.
     * Equivalente a cargarAlbum(idTour) del diagrama UML.
     */
    private fun cargarAlbum(idTour: String) {
        viewModel.cargarFotosAlbum(idTour)
    }

    private fun cargarFotos() {
        cargarAlbum(tourId)
    }

    /**
     * Muestra las fotos en el RecyclerView.
     * Equivalente a mostrarFotos(listaFotos) del diagrama UML.
     */
    private fun mostrarFotos(listaFotos: List<Foto>) {
        fotosAdapter.actualizarLista(listaFotos)
        tvFotosCompartidas.text = listaFotos.size.toString()
    }

    /**
     * Abre la pantalla de subida de fotos.
     * Equivalente a abrirSubidaFotos() del diagrama UML.
     */
    private fun abrirSubidaFotos() {
        val intent = Intent(this, SubirFotosActivity::class.java)
        intent.putExtra("TOUR_ID", tourId)
        intent.putExtra("TOUR_NOMBRE", tourNombre)
        intent.putExtra("USUARIO_ID", usuarioId)
        startActivity(intent)
    }

    /**
     * Muestra un mensaje al usuario.
     * Equivalente a mostrarMensaje(mensaje) del diagrama UML.
     */
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar fotos cuando se vuelve a la actividad
        cargarFotos()
    }
}

