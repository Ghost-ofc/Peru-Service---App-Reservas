package com.grupo4.appreservas.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.adapter.CalendarDateAdapter
import com.grupo4.appreservas.adapter.CalendarDateItem
import java.text.SimpleDateFormat
import java.util.*

class CalendarPickerDialog(
    context: Context,
    private val fechasDisponibles: List<String>,
    private val onDateSelected: (Date) -> Unit
) : Dialog(context) {

    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedDate: Date? = null
    private var fechaInicial: Date? = null

    private lateinit var txtMesAno: TextView
    private lateinit var btnMesAnterior: ImageButton
    private lateinit var btnMesSiguiente: ImageButton
    private lateinit var btnCerrar: ImageButton
    private lateinit var recyclerViewCalendario: RecyclerView
    private lateinit var adapter: CalendarDateAdapter
    private lateinit var frameLayoutBackground: View

    private val dateFormatId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    
    init {
        // Establecer mes inicial a la primera fecha disponible
        val calendar = Calendar.getInstance()
        if (fechasDisponibles.isNotEmpty()) {
            val primeraFecha = dateFormatId.parse(fechasDisponibles.first())
            if (primeraFecha != null) {
                calendar.time = primeraFecha
                currentMonth = calendar.get(Calendar.MONTH)
                currentYear = calendar.get(Calendar.YEAR)
            }
        } else {
            // Si no hay fechas disponibles, usar el mes actual
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
        }
    }
    
    constructor(
        context: Context,
        fechasDisponibles: List<String>,
        onDateSelected: (Date) -> Unit,
        fechaInicial: Date? = null
    ) : this(context, fechasDisponibles, onDateSelected) {
        this.fechaInicial = fechaInicial
        if (fechaInicial != null) {
            val calendar = Calendar.getInstance()
            calendar.time = fechaInicial
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            selectedDate = fechaInicial
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_calendar_picker)

        // Configurar ventana para fondo semitransparente
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        inicializarVistas()
        configurarListeners()
        
        // Aplicar fecha inicial si existe (después de que las vistas estén inicializadas)
        if (fechaInicial != null) {
            selectedDate = fechaInicial
        }
        
        mostrarCalendario()
    }

    private fun inicializarVistas() {
        frameLayoutBackground = findViewById(R.id.frameLayoutBackground)
        txtMesAno = findViewById(R.id.txtMesAno)
        btnMesAnterior = findViewById(R.id.btnMesAnterior)
        btnMesSiguiente = findViewById(R.id.btnMesSiguiente)
        btnCerrar = findViewById(R.id.btnCerrarCalendario)
        recyclerViewCalendario = findViewById(R.id.recyclerViewCalendario)

        recyclerViewCalendario.layoutManager = GridLayoutManager(context, 7)
        actualizarTituloMes()
    }

    private fun configurarListeners() {
        // Cerrar al hacer click en el fondo (pero no en la card)
        frameLayoutBackground.setOnClickListener {
            dismiss()
        }

        // Prevenir que el click en la card cierre el diálogo
        findViewById<View>(R.id.cardViewCalendar)?.setOnClickListener {
            // No hacer nada, evitar que se propague al fondo
        }

        btnCerrar.setOnClickListener {
            dismiss()
        }

        btnMesAnterior.setOnClickListener {
            cambiarMes(-1)
        }

        btnMesSiguiente.setOnClickListener {
            cambiarMes(1)
        }
    }

    private fun cambiarMes(delta: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        calendar.add(Calendar.MONTH, delta)
        
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)
        
        actualizarTituloMes()
        mostrarCalendario()
    }

    private fun actualizarTituloMes() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        val monthText = monthFormat.format(calendar.time)
        // Capitalizar primera letra
        txtMesAno.text = monthText.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() 
        }
    }

    private fun mostrarCalendario() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        
        // Obtener el primer día del mes y ajustar al domingo
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
        
        calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract)
        
        val dates = mutableListOf<CalendarDateItem>()
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        // Generar 42 días (6 semanas)
        for (i in 0 until 42) {
            val date = calendar.time
            val dateStr = dateFormatId.format(date)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val isCurrentMonth = calendar.get(Calendar.MONTH) == currentMonth
            
            // Verificar si la fecha está disponible (debe estar en la lista Y ser hoy o futura)
            val todayStr = dateFormatId.format(today.time)
            val isAvailable = fechasDisponibles.contains(dateStr) && 
                             (dateStr >= todayStr)
            
            // Verificar si está seleccionada
            val isSelected = selectedDate?.let { dateFormatId.format(it) == dateStr } ?: false
            
            dates.add(
                CalendarDateItem(
                    date = date,
                    day = day,
                    isAvailable = isAvailable,
                    isSelected = isSelected,
                    isCurrentMonth = isCurrentMonth
                )
            )
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        adapter = CalendarDateAdapter(dates) { item ->
            if (item.isAvailable && item.date != null) {
                val fechaSeleccionada = item.date
                selectedDate = fechaSeleccionada
                mostrarCalendario() // Refrescar para mostrar la selección
                onDateSelected(fechaSeleccionada)
                dismiss()
            }
        }
        
        recyclerViewCalendario.adapter = adapter
    }

    fun setSelectedDate(date: Date?) {
        if (!::txtMesAno.isInitialized) {
            // Si las vistas no están inicializadas, guardar para aplicar después
            fechaInicial = date
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                currentMonth = calendar.get(Calendar.MONTH)
                currentYear = calendar.get(Calendar.YEAR)
            }
            return
        }
        
        fechaInicial = date
        if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            actualizarTituloMes()
        }
        selectedDate = date
        // Refrescar el calendario si ya está mostrado
        if (::recyclerViewCalendario.isInitialized) {
            mostrarCalendario()
        }
    }
}

