package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R

class FilterActivity : AppCompatActivity() {

    private lateinit var spinnerRangoPrecio: Spinner
    private lateinit var radioGroupDisponibilidad: RadioGroup
    private lateinit var btnAplicarFiltros: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        inicializarVistas()
    }

    private fun inicializarVistas() {
        spinnerRangoPrecio = findViewById(R.id.spinnerRangoPrecio)
        radioGroupDisponibilidad = findViewById(R.id.radioGroupDisponibilidad)
        btnAplicarFiltros = findViewById(R.id.btnAplicarFiltros)

        btnAplicarFiltros.setOnClickListener {
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {
        val intent = Intent()

        // Obtener rango de precio seleccionado
        val rangoPrecio = spinnerRangoPrecio.selectedItem.toString()
        when (rangoPrecio) {
            "Todos los precios" -> {
                // No aplicar filtro de precio
            }
            "Menos de S/ 200" -> {
                intent.putExtra("precioMax", 200.0)
            }
            "S/ 200 - S/ 400" -> {
                intent.putExtra("precioMin", 200.0)
                intent.putExtra("precioMax", 400.0)
            }
            "Más de S/ 400" -> {
                intent.putExtra("precioMin", 400.0)
            }
        }

        setResult(RESULT_OK, intent)
        finish()
    }
}