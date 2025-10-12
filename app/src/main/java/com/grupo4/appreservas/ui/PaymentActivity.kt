package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.controller.PaymentController
import com.grupo4.appreservas.repository.BookingRepository
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.repository.PaymentRepository
import com.grupo4.appreservas.modelos.Booking
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.service.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var paymentController: PaymentController
    private lateinit var booking: Booking

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
        booking = intent.getSerializableExtra("BOOKING") as? Booking
            ?: run {
                Toast.makeText(this, "Error al cargar reserva", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
    }

    private fun inicializarDependencias() {
        val paymentRepo = PaymentRepository.getInstance()
        val bookingRepo = BookingRepository.getInstance()
        val destinoRepo = DestinoRepository.getInstance()

        val paymentService = PaymentService(paymentRepo, bookingRepo)
        val availabilityService = AvailabilityService(destinoRepo, bookingRepo)
        val bookingService = BookingService(bookingRepo, destinoRepo, availabilityService)
        val voucherService = VoucherService(bookingRepo)

        paymentController = PaymentController(paymentService, bookingService, voucherService)
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
        booking.destino?.let { destino ->
            txtDestinoNombre.text = destino.nombre

            // Precio unitario
            val precioUnitario = destino.precio

            // Cálculo
            txtCalculoPrecio.text = "S/ $precioUnitario x ${booking.numPersonas} persona${if (booking.numPersonas > 1) "s" else ""}"
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        txtFecha.text = dateFormat.format(booking.fecha)
        txtHora.text = booking.horaInicio
        txtNumPersonas.text = "${booking.numPersonas} persona${if (booking.numPersonas > 1) "s" else ""}"

        // Totales
        txtSubtotal.text = "S/ ${booking.precioTotal.toInt()}"
        txtMontoTotal.text = "S/ ${booking.precioTotal.toInt()}"
        btnPagar.text = "Pagar S/ ${booking.precioTotal.toInt()}"
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
                val resultado = paymentController.process(booking.id, metodo)
                val success = resultado["success"] as? Boolean ?: false

                if (success) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "¡Pago procesado exitosamente!",
                        Toast.LENGTH_SHORT
                    ).show()

                    mostrarComprobante()
                } else {
                    Toast.makeText(
                        this@PaymentActivity,
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
                    this@PaymentActivity,
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
        val intent = Intent(this, VoucherActivity::class.java)
        intent.putExtra("BOOKING_ID", booking.id)
        startActivity(intent)
        finish()
    }
}