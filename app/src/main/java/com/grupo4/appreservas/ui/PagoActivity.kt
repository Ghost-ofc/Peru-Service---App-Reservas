package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.controller.PagoController
import com.grupo4.appreservas.controller.ControlNotificaciones
import com.grupo4.appreservas.repository.ReservasRepository
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.PagoRepository
import com.grupo4.appreservas.repository.RepositorioNotificaciones
import com.grupo4.appreservas.repository.RepositorioOfertas
import com.grupo4.appreservas.repository.RepositorioClima
import com.grupo4.appreservas.repository.RepositorioRecompensas
import com.grupo4.appreservas.viewmodel.RecompensasViewModel
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.modelos.TipoNotificacion
import com.grupo4.appreservas.modelos.PuntosUsuario
import com.grupo4.appreservas.service.*
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PagoActivity : AppCompatActivity() {

    private lateinit var pagoController: PagoController
    private lateinit var controlNotificaciones: ControlNotificaciones
    private lateinit var reserva: Reserva

    private lateinit var txtDestinoNombre: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtHora: TextView
    private lateinit var txtNumPersonas: TextView
    private lateinit var txtCalculoPrecio: TextView
    private lateinit var txtSubtotal: TextView
    private lateinit var txtMontoTotal: TextView
    private lateinit var cardYape: MaterialCardView
    private lateinit var cardPlin: MaterialCardView
    private lateinit var cardTarjeta: MaterialCardView
    private lateinit var checkYape: View
    private lateinit var checkPlin: View
    private lateinit var checkTarjeta: View
    private lateinit var radioGroupMetodos: RadioGroup
    private lateinit var radioYape: RadioButton
    private lateinit var radioPlin: RadioButton
    private lateinit var radioTarjeta: RadioButton
    private lateinit var btnPagar: Button
    private lateinit var progressBar: ProgressBar

    private var metodoSeleccionado: MetodoPago? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        obtenerBooking()
        inicializarDependencias()
        inicializarVistas()
        mostrarResumenReserva()
        configurarMetodosPago()
    }

    private fun obtenerBooking() {
        reserva = intent.getSerializableExtra("BOOKING") as? Reserva
            ?: run {
                Toast.makeText(this, "Error al cargar reserva", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
    }

    private fun inicializarDependencias() {
        val paymentRepo = PagoRepository.getInstance(this)
        val bookingRepo = ReservasRepository.getInstance(this)
        val destinoRepo = DestinoRepository.getInstance(this)

        val paymentGateway = PaymentGateway()
        val pagoService = PagoService(paymentRepo, bookingRepo, paymentGateway)
        val availabilityService = AvailabilityService(destinoRepo, bookingRepo)
        val reservasService = ReservasService(bookingRepo, destinoRepo, availabilityService, this)
        val qrService = QRService()
        val reciboService = ReciboService(bookingRepo, qrService)

        pagoController = PagoController(pagoService, reservasService, reciboService)

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
        txtDestinoNombre = findViewById(R.id.txtDestinoNombre)
        txtFecha = findViewById(R.id.txtFecha)
        txtHora = findViewById(R.id.txtHora)
        txtNumPersonas = findViewById(R.id.txtNumPersonas)
        txtCalculoPrecio = findViewById(R.id.txtCalculoPrecio)
        txtSubtotal = findViewById(R.id.txtSubtotal)
        txtMontoTotal = findViewById(R.id.txtMontoTotal)
        cardYape = findViewById(R.id.cardYape)
        cardPlin = findViewById(R.id.cardPlin)
        cardTarjeta = findViewById(R.id.cardTarjeta)
        checkYape = findViewById(R.id.checkYape)
        checkPlin = findViewById(R.id.checkPlin)
        checkTarjeta = findViewById(R.id.checkTarjeta)
        radioGroupMetodos = findViewById(R.id.radioGroupMetodos)
        radioYape = findViewById(R.id.radioYape)
        radioPlin = findViewById(R.id.radioPlin)
        radioTarjeta = findViewById(R.id.radioTarjeta)
        btnPagar = findViewById(R.id.btnPagar)
        progressBar = findViewById(R.id.progressBar)

        btnPagar.setOnClickListener {
            procesarPago()
        }
    }

    private fun configurarMetodosPago() {
        cardYape.setOnClickListener {
            seleccionarMetodo(MetodoPago.YAPE)
            radioYape.isChecked = true
        }

        cardPlin.setOnClickListener {
            seleccionarMetodo(MetodoPago.PLIN)
            radioPlin.isChecked = true
        }

        cardTarjeta.setOnClickListener {
            seleccionarMetodo(MetodoPago.TARJETA)
            radioTarjeta.isChecked = true
        }
    }

    private fun seleccionarMetodo(metodo: MetodoPago) {
        metodoSeleccionado = metodo

        // Resetear todas las cards
        resetearSeleccion()

        // Marcar la seleccionada
        when (metodo) {
            MetodoPago.YAPE -> {
                cardYape.strokeColor = getColor(R.color.color_primary)
                cardYape.strokeWidth = 4
                checkYape.visibility = View.VISIBLE
            }
            MetodoPago.PLIN -> {
                cardPlin.strokeColor = getColor(R.color.color_primary)
                cardPlin.strokeWidth = 4
                checkPlin.visibility = View.VISIBLE
            }
            MetodoPago.TARJETA -> {
                cardTarjeta.strokeColor = getColor(R.color.color_primary)
                cardTarjeta.strokeWidth = 4
                checkTarjeta.visibility = View.VISIBLE
            }
        }

        btnPagar.isEnabled = true
    }

    private fun resetearSeleccion() {
        cardYape.strokeColor = getColor(R.color.color_outline)
        cardYape.strokeWidth = 2
        checkYape.visibility = View.GONE

        cardPlin.strokeColor = getColor(R.color.color_outline)
        cardPlin.strokeWidth = 2
        checkPlin.visibility = View.GONE

        cardTarjeta.strokeColor = getColor(R.color.color_outline)
        cardTarjeta.strokeWidth = 2
        checkTarjeta.visibility = View.GONE
    }

    private fun mostrarResumenReserva() {
        reserva.destino?.let { destino ->
            txtDestinoNombre.text = destino.nombre

            // Precio unitario
            val precioUnitario = destino.precio

            // Cálculo
            txtCalculoPrecio.text = "S/ $precioUnitario x ${reserva.numPersonas} persona${if (reserva.numPersonas > 1) "s" else ""}"
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        txtFecha.text = dateFormat.format(reserva.fecha)
        txtHora.text = reserva.horaInicio
        txtNumPersonas.text = "${reserva.numPersonas} persona${if (reserva.numPersonas > 1) "s" else ""}"

        // Totales
        txtSubtotal.text = "S/ ${reserva.precioTotal.toInt()}"
        txtMontoTotal.text = "S/ ${reserva.precioTotal.toInt()}"
        btnPagar.text = "Pagar S/ ${reserva.precioTotal.toInt()}"
    }

    private fun procesarPago() {
        val metodo = metodoSeleccionado ?: return

        progressBar.visibility = View.VISIBLE
        btnPagar.isEnabled = false
        cardYape.isEnabled = false
        cardPlin.isEnabled = false
        cardTarjeta.isEnabled = false

        lifecycleScope.launch {
            try {
                val resultado = pagoController.process(reserva.id, metodo)
                val success = resultado["success"] as? Boolean ?: false

                if (success) {
                    Toast.makeText(
                        this@PagoActivity,
                        "¡Pago procesado exitosamente!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Generar notificación de confirmación de reserva
                    generarNotificacionConfirmacionReserva()

                    // Sumar puntos por reserva completada (HU-007)
                    sumarPuntosPorReserva()

                    mostrarComprobante()
                } else {
                    Toast.makeText(
                        this@PagoActivity,
                        "Pago rechazado, intente nuevamente",
                        Toast.LENGTH_LONG
                    ).show()

                    progressBar.visibility = View.GONE
                    btnPagar.isEnabled = true
                    cardYape.isEnabled = true
                    cardPlin.isEnabled = true
                    cardTarjeta.isEnabled = true
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@PagoActivity,
                    "Error al procesar pago: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                progressBar.visibility = View.GONE
                btnPagar.isEnabled = true
                cardYape.isEnabled = true
                cardPlin.isEnabled = true
                cardTarjeta.isEnabled = true
            }
        }
    }

    private fun mostrarComprobante() {
        val intent = Intent(this, ReciboActivity::class.java)
        intent.putExtra("BOOKING_ID", reserva.id)
        startActivity(intent)
        finish()
    }

    /**
     * Genera una notificación de confirmación de reserva después de confirmar el pago.
     */
    private fun generarNotificacionConfirmacionReserva() {
        try {
            val destino = reserva.destino
            val destinoNombre = destino?.nombre ?: "Tour"
            val puntoEncuentro = destino?.ubicacion ?: reserva.destino?.ubicacion ?: ""
            
            // Obtener tour para obtener punto de encuentro y hora
            val dbHelper = com.grupo4.appreservas.repository.DatabaseHelper(this)
            val tour = reserva.tourId.let { dbHelper.obtenerTourPorId(it) }
            val puntoEncuentroFinal = tour?.puntoEncuentro ?: puntoEncuentro
            val horaTour = reserva.horaInicio ?: tour?.hora ?: ""

            val notificacion = Notificacion(
                id = "CONF_${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
                usuarioId = reserva.usuarioId,
                tipo = TipoNotificacion.CONFIRMACION_RESERVA,
                titulo = "Confirmación de Reserva",
                descripcion = "Tu reserva para '$destinoNombre' ha sido confirmada",
                fechaCreacion = Date(),
                tourId = reserva.tourId,
                destinoNombre = destinoNombre,
                puntoEncuentro = puntoEncuentroFinal,
                horaTour = horaTour
            )

            // Guardar y enviar notificación
            val repositorioNotificaciones = RepositorioNotificaciones(this)
            repositorioNotificaciones.enviarNotificacionPush(notificacion)
            
            val notificacionesService = NotificacionesService(this)
            notificacionesService.enviarNotificacionPush(notificacion)
        } catch (e: Exception) {
            android.util.Log.e("PagoActivity", "Error al generar notificación de confirmación: ${e.message}", e)
        }
    }

    /**
     * Suma puntos al usuario por completar una reserva (HU-007).
     * También actualiza el ViewModel para verificar logros.
     */
    private fun sumarPuntosPorReserva() {
        try {
            val repositorioRecompensas = RepositorioRecompensas(this)
            val puntosAGanar = PuntosUsuario.PUNTOS_POR_RESERVA
            repositorioRecompensas.sumarPuntos(reserva.usuarioId, puntosAGanar)
            
            // Actualizar ViewModel para verificar logros
            val viewModel = ViewModelProvider(this)[RecompensasViewModel::class.java]
            viewModel.actualizarPuntos(reserva.usuarioId, reserva.id)
            
            android.util.Log.d("PagoActivity", "Puntos sumados: $puntosAGanar para usuario ${reserva.usuarioId}")
        } catch (e: Exception) {
            android.util.Log.e("PagoActivity", "Error al sumar puntos: ${e.message}", e)
        }
    }
}