package com.grupo4.appreservas.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.controller.ReservasController
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.modelos.Booking
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.ReservasService
import java.text.SimpleDateFormat
import java.util.*

class ReservasActivity : AppCompatActivity() {

    private lateinit var reservasController: ReservasController
    private lateinit var destino: Destino

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var txtDestinoNombre: TextView
    private lateinit var btnSeleccionarFecha: MaterialCardView
    private lateinit var txtFechaSeleccionada: TextView
    private lateinit var spinnerHora: Spinner
    private lateinit var spinnerNumPersonas: Spinner
    private lateinit var txtPrecioTotal: TextView
    private lateinit var txtCuposDisponibles: TextView
    private lateinit var btnConfirmarReserva: Button
    private lateinit var progressBar: ProgressBar

    private var fechaSeleccionada: Date? = null
    private var horaSeleccionada: String = "08:00"
    private var numPersonasSeleccionadas: Int = 1
    private var tourSlotId: String = ""

    private val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    private val dateFormatId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        obtenerDestino()
        inicializarDependencias()
        inicializarVistas()
        configurarSpinners()
    }

    private fun obtenerDestino() {
        destino = intent.getSerializableExtra("DESTINO") as? Destino
            ?: run {
                Toast.makeText(this, "Error al cargar destino", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
    }

    private fun inicializarDependencias() {
        val destinoRepo = DestinoRepository.getInstance()
        val bookingRepo = ReservasRepository.getInstance()
        val availabilityService = AvailabilityService(destinoRepo, bookingRepo)
        val reservasService = ReservasService(bookingRepo, destinoRepo, availabilityService)

        reservasController = ReservasController(reservasService, availabilityService)
    }

    private fun inicializarVistas() {
        topAppBar = findViewById(R.id.topAppBar)
        txtDestinoNombre = findViewById(R.id.txtDestinoNombre)
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada)
        spinnerHora = findViewById(R.id.spinnerHora)
        spinnerNumPersonas = findViewById(R.id.spinnerNumPersonas)
        txtPrecioTotal = findViewById(R.id.txtPrecioTotal)
        txtCuposDisponibles = findViewById(R.id.txtCuposDisponibles)
        btnConfirmarReserva = findViewById(R.id.btnConfirmarReserva)
        progressBar = findViewById(R.id.progressBar)

        // Configurar toolbar
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        txtDestinoNombre.text = destino.nombre

        // Seleccionar fecha
        btnSeleccionarFecha.setOnClickListener {
            mostrarDatePicker()
        }

        // Confirmar reserva
        btnConfirmarReserva.setOnClickListener {
            confirmarReserva()
        }

        actualizarPrecioTotal()
    }

    private fun configurarSpinners() {
        // Spinner de horas
        val horas = listOf(
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
        )
        val horaAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            horas.map { "Seleccionar hora" }.toMutableList().apply {
                clear()
                addAll(horas)
            }
        )
        horaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHora.adapter = horaAdapter

        spinnerHora.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                horaSeleccionada = horas[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Spinner de personas
        val opcionesPersonas = (1..destino.maxPersonas).toList()
        val personasAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesPersonas.map { "$it persona${if (it > 1) "s" else ""}" }
        )
        personasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNumPersonas.adapter = personasAdapter

        spinnerNumPersonas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                numPersonasSeleccionadas = position + 1
                actualizarPrecioTotal()
                if (fechaSeleccionada != null) {
                    consultarDisponibilidad()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                fechaSeleccionada = calendar.time
                txtFechaSeleccionada.text = dateFormat.format(calendar.time)

                generarTourSlotId()
                consultarDisponibilidad()
                actualizarPrecioTotal()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun generarTourSlotId() {
        fechaSeleccionada?.let { fecha ->
            val fechaStr = dateFormatId.format(fecha)
            tourSlotId = "${destino.id}_${fechaStr}_$horaSeleccionada"
        }
    }

    private fun consultarDisponibilidad() {
        if (tourSlotId.isEmpty()) return

        try {
            val disponibilidad = reservasController.consultarDisponibilidad(tourSlotId)
            val cuposDisp = disponibilidad["cuposDisponibles"] as? Int ?: 0

            txtCuposDisponibles.text = "$cuposDisp cupos disponibles"
            txtCuposDisponibles.visibility = View.VISIBLE

            btnConfirmarReserva.isEnabled = cuposDisp >= numPersonasSeleccionadas

        } catch (e: Exception) {
            Toast.makeText(this, "Error al consultar disponibilidad", Toast.LENGTH_SHORT).show()
            btnConfirmarReserva.isEnabled = false
        }
    }

    private fun actualizarPrecioTotal() {
        val total = destino.precio * numPersonasSeleccionadas
        txtPrecioTotal.text = "Total: S/ $total"
    }

    private fun confirmarReserva() {
        if (fechaSeleccionada == null) {
            Toast.makeText(this, "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnConfirmarReserva.isEnabled = false

        try {
            val seatsLocked = reservasController.lockSeats(tourSlotId, numPersonasSeleccionadas)

            if (seatsLocked) {
                val booking = reservasController.crearReservaCmd(
                    userId = "user_123",
                    tourSlotId = tourSlotId,
                    pax = numPersonasSeleccionadas
                )

                if (booking != null) {
                    irAPago(booking)
                } else {
                    Toast.makeText(this, "Error al crear reserva", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No hay cupos suficientes", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            progressBar.visibility = View.GONE
            btnConfirmarReserva.isEnabled = true
        }
    }

    private fun irAPago(booking: Booking) {
        val intent = Intent(this, PagoActivity::class.java)
        intent.putExtra("BOOKING", booking)
        startActivity(intent)
        finish()
    }
}