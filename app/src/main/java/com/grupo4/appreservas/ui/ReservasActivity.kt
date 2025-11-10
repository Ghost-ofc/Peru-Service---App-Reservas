package com.grupo4.appreservas.ui

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
import com.grupo4.appreservas.repository.DatabaseHelper
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.service.AvailabilityService
import com.grupo4.appreservas.service.ReservasService
import com.grupo4.appreservas.service.DestinoService
import java.text.SimpleDateFormat
import java.util.*

class ReservasActivity : AppCompatActivity() {

    private lateinit var reservasController: ReservasController
    private lateinit var availabilityService: AvailabilityService
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
    private var horaSeleccionada: String = ""
    private var numPersonasSeleccionadas: Int = 1
    private var tourSlotId: String = ""
    private var usuarioId: Int = 0
    
    // Fechas y horas disponibles desde la base de datos
    private var fechasDisponibles: List<String> = emptyList()
    private var horasDisponibles: List<String> = emptyList()

    private val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    private val dateFormatId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        obtenerUsuarioId()
        inicializarDependencias()
        obtenerDestino()
        cargarFechasDisponibles() // Cargar fechas disponibles desde BD
        inicializarVistas()
        configurarSpinners()
    }

    private fun obtenerUsuarioId() {
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        if (usuarioId == 0) {
            // Si no viene en el intent, intentar obtenerlo de otra forma
            // Por ahora, usamos un valor por defecto temporal
            Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerDestino() {
        // Prioridad 1: Intentar obtener por ID usando el controlador (método preferido)
        val destinoId = intent.getStringExtra("DESTINO_ID")
        if (destinoId != null && destinoId.isNotEmpty()) {
            destino = reservasController.iniciarReserva(destinoId)
                ?: run {
                    Toast.makeText(this, "El destino solicitado no existe", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
            return
        }
        
        // Prioridad 2: Compatibilidad con código existente (objeto serializado)
        val destinoExtra = intent.getSerializableExtra("DESTINO") as? Destino
        if (destinoExtra != null) {
            // Si viene objeto serializado, cargar desde BD para datos actualizados
            destino = reservasController.iniciarReserva(destinoExtra.id) ?: destinoExtra
            return
        }
        
        // Error: No se proporcionó información del destino
        Toast.makeText(this, "Error al cargar destino", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun inicializarDependencias() {
        val destinoRepo = DestinoRepository.getInstance(this)
        val bookingRepo = ReservasRepository.getInstance(this)
        val destinoService = DestinoService(destinoRepo)
        availabilityService = AvailabilityService(destinoRepo, bookingRepo)
        val reservasService = ReservasService(bookingRepo, destinoRepo, availabilityService, this)

        reservasController = ReservasController(reservasService, availabilityService, destinoService)
    }
    
    /**
     * Carga las fechas disponibles para el destino desde la base de datos.
     */
    private fun cargarFechasDisponibles() {
        try {
            val dbHelper = DatabaseHelper(this)
            fechasDisponibles = dbHelper.obtenerFechasDisponiblesPorDestino(destino.id)
            
            if (fechasDisponibles.isEmpty()) {
                Toast.makeText(this, "No hay fechas disponibles para este destino", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar fechas disponibles", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
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
        // Spinner de horas - se actualizará dinámicamente cuando se seleccione una fecha
        actualizarSpinnerHoras(emptyList())
        
        spinnerHora.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (horasDisponibles.isNotEmpty() && position < horasDisponibles.size) {
                    horaSeleccionada = horasDisponibles[position]
                    // Actualizar disponibilidad si ya hay fecha seleccionada
                    if (fechaSeleccionada != null) {
                        consultarDisponibilidad()
                    }
                }
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
        if (fechasDisponibles.isEmpty()) {
            Toast.makeText(this, "No hay fechas disponibles para este destino", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Determinar fecha inicial para el calendario
        val fechaInicial = fechaSeleccionada ?: run {
            // Si no hay fecha seleccionada, usar la primera disponible
            if (fechasDisponibles.isNotEmpty()) {
                dateFormatId.parse(fechasDisponibles.first())
            } else {
                null
            }
        }
        
        val calendarDialog = CalendarPickerDialog(
            this,
            fechasDisponibles,
            { fecha ->
                fechaSeleccionada = fecha
                val fechaSeleccionadaStr = dateFormatId.format(fecha)
                txtFechaSeleccionada.text = dateFormat.format(fecha)
                
                // Cargar horas disponibles para la fecha seleccionada
                cargarHorasDisponibles(fechaSeleccionadaStr)
                
                generarTourSlotId()
                consultarDisponibilidad()
                actualizarPrecioTotal()
            },
            fechaInicial
        )
        
        calendarDialog.show()
    }
    
    /**
     * Carga las horas disponibles para una fecha específica desde la base de datos.
     */
    private fun cargarHorasDisponibles(fecha: String) {
        try {
            val dbHelper = DatabaseHelper(this)
            horasDisponibles = dbHelper.obtenerHorasDisponiblesPorDestinoYFecha(destino.id, fecha)
            
            if (horasDisponibles.isEmpty()) {
                Toast.makeText(this, "No hay horas disponibles para esta fecha", Toast.LENGTH_SHORT).show()
                horaSeleccionada = ""
            } else {
                // Actualizar el spinner de horas con las horas disponibles
                actualizarSpinnerHoras(horasDisponibles)
                
                // Seleccionar la primera hora por defecto
                if (horasDisponibles.isNotEmpty()) {
                    horaSeleccionada = horasDisponibles[0]
                    spinnerHora.setSelection(0)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar horas disponibles", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    /**
     * Actualiza el spinner de horas con las horas disponibles.
     */
    private fun actualizarSpinnerHoras(horas: List<String>) {
        val horaAdapter = if (horas.isEmpty()) {
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("No hay horas disponibles")
            )
        } else {
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                horas
            )
        }
        horaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHora.adapter = horaAdapter
    }

    private fun generarTourSlotId() {
        fechaSeleccionada?.let { fecha ->
            val fechaStr = dateFormatId.format(fecha)
            // Formato: destinoId_fecha (sin hora, ya que los slots se manejan por fecha)
            // La hora se almacena en la reserva, no en el slot
            tourSlotId = "${destino.id}_$fechaStr"
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
        
        if (horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una hora", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar diálogo de confirmación/resumen antes de crear la reserva
        mostrarResumenReserva()
    }

    /**
     * Muestra un resumen de la reserva para confirmación.
     * Equivalente a ReservationView.confirm(resumen) del diagrama UML.
     */
    private fun mostrarResumenReserva() {
        val precioTotal = destino.precio * numPersonasSeleccionadas
        val fechaFormateada = fechaSeleccionada?.let { dateFormat.format(it) } ?: "No seleccionada"
        
        val resumen = """
            Destino: ${destino.nombre}
            Fecha: $fechaFormateada
            Hora: $horaSeleccionada
            Pasajeros: $numPersonasSeleccionadas
            Precio por persona: S/ ${String.format("%.2f", destino.precio)}
            Precio total: S/ ${String.format("%.2f", precioTotal)}
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("Confirmar Reserva")
            .setMessage(resumen)
            .setPositiveButton("Confirmar") { _, _ ->
                crearReserva()
            }
            .setNegativeButton("Cancelar", null)
            .setCancelable(true)
            .show()
    }

    /**
     * Crea la reserva después de la confirmación del usuario.
     */
    private fun crearReserva() {
        progressBar.visibility = View.VISIBLE
        btnConfirmarReserva.isEnabled = false

        try {
            val seatsLocked = reservasController.lockSeats(tourSlotId, numPersonasSeleccionadas)

            if (seatsLocked) {
                val booking = reservasController.crearReservaCmd(
                    userId = usuarioId.toString(),
                    tourSlotId = tourSlotId,
                    pax = numPersonasSeleccionadas,
                    horaInicio = horaSeleccionada
                )

                if (booking != null) {
                    irAPago(booking)
                } else {
                    Toast.makeText(this, "Error al crear reserva", Toast.LENGTH_SHORT).show()
                    // Liberar cupos bloqueados si falla la creación
                    availabilityService.liberarCupos(tourSlotId, numPersonasSeleccionadas)
                }
            } else {
                Toast.makeText(this, "No hay cupos suficientes", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } finally {
            progressBar.visibility = View.GONE
            btnConfirmarReserva.isEnabled = true
        }
    }

    private fun irAPago(reserva: Reserva) {
        val intent = Intent(this, PagoActivity::class.java)
        intent.putExtra("BOOKING", reserva)
        startActivity(intent)
        finish()
    }
}