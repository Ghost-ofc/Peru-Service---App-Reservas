package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.R
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar

class DetalleDestinoActivity : AppCompatActivity() {

    private lateinit var destino: Destino

    private lateinit var imgDestino: ImageView
    private lateinit var txtNombreHeader: TextView
    private lateinit var txtPrecio: TextView
    private lateinit var txtDuracion: TextView
    private lateinit var txtMaxPersonas: TextView
    private lateinit var txtCalificacion: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtIncluye: TextView
    private lateinit var txtCuposDisponibles: TextView
    private lateinit var btnReservar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetalle)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (!obtenerDestino()) {
            return
        }
        inicializarVistas()
        mostrarDetalle()
    }

    private fun obtenerDestino(): Boolean {
        destino = intent.getSerializableExtra("DESTINO") as? Destino
            ?: run {
                Toast.makeText(this, "Error al cargar destino", Toast.LENGTH_SHORT).show()
                finish()
                return false
            }
        return true
    }

    private fun inicializarVistas() {
        imgDestino = findViewById(R.id.imgDestino)
        txtNombreHeader = findViewById(R.id.txtNombreHeader)
        txtPrecio = findViewById(R.id.txtPrecio)
        txtDuracion = findViewById(R.id.txtDuracion)
        txtMaxPersonas = findViewById(R.id.txtMaxPersonas)
        txtCalificacion = findViewById(R.id.txtCalificacion)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtIncluye = findViewById(R.id.txtIncluye)
        txtCuposDisponibles = findViewById(R.id.txtCuposDisponibles)
        btnReservar = findViewById(R.id.btnReservar)

        btnReservar.setOnClickListener {
            abrirReserva()
        }
    }

    private fun mostrarDetalle() {
        // Título sobre la imagen
        txtNombreHeader.text = destino.nombre

        // Precio
        txtPrecio.text = "S/ ${destino.precio}"

        // Información básica
        txtDuracion.text = "${destino.duracionHoras} horas"
        txtMaxPersonas.text = "Máx. ${destino.maxPersonas}"
        txtCalificacion.text = "${destino.calificacion}/5 (${destino.numReseñas} reseñas)"

        // Cupos disponibles (en rojo)
        txtCuposDisponibles.text = "${destino.maxPersonas} cupos disponibles"

        // Descripción
        txtDescripcion.text = destino.descripcion

        // Qué incluye con checkmarks
        val incluyeTexto = destino.incluye.joinToString("\n") { "✓ $it" }
        txtIncluye.text = incluyeTexto

        // Cargar imagen con Glide
        Glide.with(this)
            .load(destino.imagenUrl)
            .centerCrop()
            .into(imgDestino)
    }

    private fun abrirReserva() {
        val intent = Intent(this, ReservasActivity::class.java)
        intent.putExtra("DESTINO", destino)
        startActivity(intent)
    }
}