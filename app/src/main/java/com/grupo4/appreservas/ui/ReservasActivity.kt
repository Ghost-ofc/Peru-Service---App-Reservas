package com.grupo4.appreservas.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.ReservaViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para realizar reservas de tours.
 * Corresponde a la HU: Selección de tour y Confirmación de reserva.
 */
class ReservasActivity : AppCompatActivity() {

    private lateinit var viewModel: ReservaViewModel
    private lateinit var destino: Destino
    private var destinoId: String = ""
    private var usuarioId: Int = 2 // Por defecto, se puede pasar desde el intent

    // Vistas
    private lateinit var txtDestinoNombre: TextView
    private lateinit var btnSeleccionarFecha: MaterialCardView
    private lateinit var txtFechaSeleccionada: TextView
    private lateinit var spinnerHora: Spinner
    private lateinit var spinnerNumPersonas: Spinner
    private lateinit var txtCuposDisponibles: TextView
    private lateinit var txtPrecioTotal: TextView
    private lateinit var btnConfirmarReserva: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Datos seleccionados
    private var fechaSeleccionada: String = ""
    private var horaSeleccionada: String = ""
    private var cantidadPersonas: Int = 1
    private var tourSlotId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        // Obtener datos del intent
        destinoId = intent.getStringExtra("DESTINO_ID") ?: ""
        usuarioId = intent.getIntExtra("USUARIO_ID", 2)

        if (destinoId.isEmpty()) {
            Toast.makeText(this, "Error: No se proporcionó información del destino", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarDependencias()
        inicializarVistas()
        cargarDestino()
        configurarListeners()
    }

    private fun inicializarDependencias() {
        val repository = PeruvianServiceRepository.getInstance(this)
        viewModel = ViewModelProvider(this, ReservaViewModelFactory(repository))[ReservaViewModel::class.java]

        // Observar cambios en cupos disponibles
        viewModel.cuposDisponibles.observe(this) { cupos ->
            actualizarCuposDisponibles(cupos)
        }

        // Observar cambios en disponibilidad
        viewModel.disponibilidad.observe(this) { disponible ->
            btnConfirmarReserva.isEnabled = disponible && fechaSeleccionada.isNotEmpty() && horaSeleccionada.isNotEmpty()
        }
    }

    private fun inicializarVistas() {
        txtDestinoNombre = findViewById(R.id.txtDestinoNombre)
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada)
        spinnerHora = findViewById(R.id.spinnerHora)
        spinnerNumPersonas = findViewById(R.id.spinnerNumPersonas)
        txtCuposDisponibles = findViewById(R.id.txtCuposDisponibles)
        txtPrecioTotal = findViewById(R.id.txtPrecioTotal)
        btnConfirmarReserva = findViewById(R.id.btnConfirmarReserva)
        progressBar = findViewById(R.id.progressBar)

        // Configurar toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
            .setNavigationOnClickListener {
                finish()
            }
    }

    private fun cargarDestino() {
        val repository = PeruvianServiceRepository.getInstance(this)
        destino = repository.buscarDestinoPorId(destinoId)
            ?: run {
                Toast.makeText(this, "Error: Destino no encontrado", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        txtDestinoNombre.text = destino.nombre
        configurarSpinnerPersonas()
    }

    private fun configurarSpinnerPersonas() {
        val opciones = (1..destino.maxPersonas).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNumPersonas.adapter = adapter

        spinnerNumPersonas.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                cantidadPersonas = (position + 1)
                actualizarPrecioTotal()
                consultarDisponibilidad()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun configurarListeners() {
        // Seleccionar fecha
        btnSeleccionarFecha.setOnClickListener {
            mostrarSelectorFecha()
        }

        // Confirmar reserva
        btnConfirmarReserva.setOnClickListener {
            confirmarReserva()
        }
    }

    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaSeleccionadaCal = Calendar.getInstance()
                fechaSeleccionadaCal.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                fechaSeleccionada = dateFormat.format(fechaSeleccionadaCal.time)
                txtFechaSeleccionada.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(fechaSeleccionadaCal.time)
                
                cargarHorasDisponibles()
            },
            anio, mes, dia
        )

        // Establecer fecha mínima como hoy
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun cargarHorasDisponibles() {
        if (fechaSeleccionada.isEmpty()) return

        val repository = PeruvianServiceRepository.getInstance(this)
        val horas = repository.obtenerHorasDisponibles(destinoId, fechaSeleccionada)

        if (horas.isEmpty()) {
            Toast.makeText(this, "No hay horarios disponibles para esta fecha", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHora.adapter = adapter

        spinnerHora.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                horaSeleccionada = horas[position]
                actualizarTourSlotId()
                consultarDisponibilidad()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun actualizarTourSlotId() {
        // El tourSlotId tiene formato: destinoId_fecha
        tourSlotId = "${destinoId}_$fechaSeleccionada"
    }

    private fun consultarDisponibilidad() {
        if (tourSlotId.isEmpty()) return

        viewModel.consultarDisponibilidadAsientos(tourSlotId)
    }

    private fun actualizarCuposDisponibles(cupos: Int) {
        if (cupos > 0) {
            txtCuposDisponibles.text = "$cupos cupos disponibles"
            txtCuposDisponibles.visibility = View.VISIBLE
        } else {
            txtCuposDisponibles.text = "Sin cupos disponibles"
            txtCuposDisponibles.visibility = View.VISIBLE
        }
    }

    private fun actualizarPrecioTotal() {
        val precioTotal = destino.precio * cantidadPersonas
        txtPrecioTotal.text = "Total: S/ ${String.format("%.2f", precioTotal)}"
    }

    private fun confirmarReserva() {
        if (fechaSeleccionada.isEmpty() || horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnConfirmarReserva.isEnabled = false

        // Crear la reserva
        val reserva = viewModel.crearReserva(usuarioId, tourSlotId, cantidadPersonas)

        progressBar.visibility = View.GONE

        if (reserva != null) {
            Toast.makeText(
                this,
                "Reserva creada exitosamente. Procede al pago.",
                Toast.LENGTH_SHORT
            ).show()
            
            // Redirigir a la pantalla de pago
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("RESERVA_ID", reserva.id)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(
                this,
                "Error: No se pudo crear la reserva. Verifica la disponibilidad.",
                Toast.LENGTH_SHORT
            ).show()
            btnConfirmarReserva.isEnabled = true
        }
    }
}

/**
 * Factory para crear ReservaViewModel con dependencias.
 */
class ReservaViewModelFactory(
    private val repository: PeruvianServiceRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReservaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

