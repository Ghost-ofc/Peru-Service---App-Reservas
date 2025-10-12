package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.grupo4.appreservas.adapter.DestinosAdapter
import com.grupo4.appreservas.controller.CatalogController
import com.grupo4.appreservas.controller.FilterController
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.repository.BookingRepository
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.DestinationService
import com.grupo4.appreservas.R

class CatalogActivity : AppCompatActivity() {

    private lateinit var catalogController: CatalogController
    private lateinit var filterController: FilterController

    private lateinit var recyclerDestinos: RecyclerView
    private lateinit var chipGroupCategorias: ChipGroup
    private lateinit var editBuscar: EditText
    private lateinit var btnFiltros: ImageView

    private lateinit var destinosAdapter: DestinosAdapter
    private var destinosList = listOf<Destino>()
    private var destinosFiltrados = listOf<Destino>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        inicializarDependencias()
        inicializarVistas()
        configurarRecyclerView()
        configurarBuscador()
        cargarDestinos()
    }

    private fun inicializarDependencias() {
        val destinoRepo = DestinoRepository.getInstance()
        val bookingRepo = BookingRepository.getInstance()
        val destinationService = DestinationService(destinoRepo)
        val availabilityService = AvailabilityService(destinoRepo, bookingRepo)

        catalogController = CatalogController(destinationService, availabilityService)
        filterController = FilterController(destinationService)
    }

    private fun inicializarVistas() {
        recyclerDestinos = findViewById(R.id.recyclerDestinos)
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias)
        editBuscar = findViewById(R.id.editBuscar)
        btnFiltros = findViewById(R.id.btnFiltros)

        btnFiltros.setOnClickListener {
            abrirFiltros()
        }
    }

    private fun configurarRecyclerView() {
        destinosAdapter = DestinosAdapter { destino ->
            abrirDetalle(destino)
        }

        recyclerDestinos.apply {
            layoutManager = LinearLayoutManager(this@CatalogActivity)
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
            destinosList = catalogController.solicitarDestinos()
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
        destinosFiltrados = filterController.filtrarDestinos(criterios)
        destinosAdapter.actualizarLista(destinosFiltrados)
    }

    private fun abrirFiltros() {
        val intent = Intent(this, FilterActivity::class.java)
        startActivityForResult(intent, REQUEST_FILTROS)
    }

    private fun abrirDetalle(destino: Destino) {
        val intent = Intent(this, DestinationDetailActivity::class.java)
        intent.putExtra("DESTINO", destino)
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

            destinosFiltrados = filterController.filtrarDestinos(criterios)
            destinosAdapter.actualizarLista(destinosFiltrados)
        }
    }

    companion object {
        private const val REQUEST_FILTROS = 100
    }
}