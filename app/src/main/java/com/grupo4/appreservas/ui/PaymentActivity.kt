package com.grupo4.appreservas.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.grupo4.appreservas.R
import com.grupo4.appreservas.controller.PaymentController
import com.grupo4.appreservas.modelos.MetodoPago
import com.grupo4.appreservas.modelos.PuntosUsuario
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.service.PaymentGateway
import com.grupo4.appreservas.service.PaymentService
import com.grupo4.appreservas.service.QRService
import com.grupo4.appreservas.service.VoucherService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para procesar pagos de reservas.
 * Corresponde a la HU: Selección del método de pago y Pago exitoso/fallido.
 */
class PaymentActivity : AppCompatActivity() {

    private lateinit var paymentController: PaymentController
    private lateinit var reserva: Reserva
    private var reservaId: String = ""
    private var metodoPagoSeleccionado: MetodoPago? = null

    // Vistas
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
    private lateinit var btnPagar: MaterialButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        reservaId = intent.getStringExtra("RESERVA_ID") ?: ""

        if (reservaId.isEmpty()) {
            Toast.makeText(this, "Error: No se proporcionó información de la reserva", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarDependencias()
        inicializarVistas()
        cargarReserva()
        configurarListeners()
    }

    private fun inicializarDependencias() {
        val repository = PeruvianServiceRepository.getInstance(this)
        val paymentGateway = PaymentGateway()
        val paymentService = PaymentService(repository, paymentGateway)
        val qrService = QRService()
        val voucherService = VoucherService(repository, qrService)
        paymentController = PaymentController(paymentService, voucherService)
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
        btnPagar = findViewById(R.id.btnPagar)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun cargarReserva() {
        val repository = PeruvianServiceRepository.getInstance(this)
        reserva = repository.buscarReservaPorId(reservaId)
            ?: run {
                Toast.makeText(this, "Error: Reserva no encontrada", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        // Mostrar información de la reserva
        txtDestinoNombre.text = reserva.destino?.nombre ?: "Destino"
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        txtFecha.text = dateFormat.format(reserva.fecha)
        txtHora.text = reserva.horaInicio
        txtNumPersonas.text = "${reserva.numPersonas} persona${if (reserva.numPersonas > 1) "s" else ""}"

        val precioUnitario = reserva.destino?.precio ?: 0.0
        txtCalculoPrecio.text = "S/ ${String.format("%.2f", precioUnitario)} x ${reserva.numPersonas} persona${if (reserva.numPersonas > 1) "s" else ""}"
        txtSubtotal.text = "S/ ${String.format("%.2f", reserva.precioTotal)}"
        txtMontoTotal.text = "S/ ${String.format("%.2f", reserva.precioTotal)}"
        
        btnPagar.text = "Pagar S/ ${String.format("%.2f", reserva.precioTotal)}"
    }

    private fun configurarListeners() {
        // Selección de método de pago
        cardYape.setOnClickListener {
            seleccionarMetodoPago(MetodoPago.YAPE)
        }

        cardPlin.setOnClickListener {
            seleccionarMetodoPago(MetodoPago.PLIN)
        }

        cardTarjeta.setOnClickListener {
            seleccionarMetodoPago(MetodoPago.TARJETA)
        }

        // Botón pagar
        btnPagar.setOnClickListener {
            procesarPago()
        }
    }

    private fun seleccionarMetodoPago(metodo: MetodoPago) {
        metodoPagoSeleccionado = metodo

        // Actualizar UI
        checkYape.visibility = if (metodo == MetodoPago.YAPE) View.VISIBLE else View.GONE
        checkPlin.visibility = if (metodo == MetodoPago.PLIN) View.VISIBLE else View.GONE
        checkTarjeta.visibility = if (metodo == MetodoPago.TARJETA) View.VISIBLE else View.GONE

        // Actualizar bordes de las cards
        actualizarBordesCards(metodo)

        // Habilitar botón de pago
        btnPagar.isEnabled = true
    }

    private fun actualizarBordesCards(metodoSeleccionado: MetodoPago) {
        // Resetear todos los bordes
        cardYape.strokeWidth = if (metodoSeleccionado == MetodoPago.YAPE) 4 else 2
        cardPlin.strokeWidth = if (metodoSeleccionado == MetodoPago.PLIN) 4 else 2
        cardTarjeta.strokeWidth = if (metodoSeleccionado == MetodoPago.TARJETA) 4 else 2
    }

    private fun procesarPago() {
        if (metodoPagoSeleccionado == null) {
            Toast.makeText(this, "Por favor, selecciona un método de pago", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnPagar.isEnabled = false

        lifecycleScope.launch {
            try {
                val pago = paymentController.pagar(
                    reservaId,
                    metodoPagoSeleccionado!!,
                    reserva.precioTotal
                )

                progressBar.visibility = View.GONE

                if (pago != null && pago.estado == com.grupo4.appreservas.modelos.EstadoPago.APROBADO) {
                    // Pago exitoso
                    val repository = PeruvianServiceRepository.getInstance(this@PaymentActivity)
                    repository.confirmarPago(reservaId, pago)

                    // Programar cambio automático de estado a CONFIRMADO después de 1 minuto
                    // (para permitir subir fotos al álbum del tour)
                    programarCambioEstadoReserva(reservaId)

                    // Generar comprobante y abrir voucher
                    val comprobante = paymentController.generarComprobante(reservaId)
                    if (comprobante != null) {
                        abrirVoucher(comprobante)
                    } else {
                        Toast.makeText(this@PaymentActivity, "Pago exitoso, pero error al generar comprobante", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    // Pago fallido
                    Toast.makeText(
                        this@PaymentActivity,
                        "Error: El pago no se pudo procesar. Por favor, intenta nuevamente.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnPagar.isEnabled = true
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@PaymentActivity,
                    "Error al procesar el pago: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnPagar.isEnabled = true
            }
        }
    }

    /**
     * Programa el cambio automático de estado de la reserva a CONFIRMADO después de 1 minuto.
     * Esto permite que el usuario pueda subir fotos al álbum del tour.
     * También marca el tour del día actual como completado.
     * 
     * Usa un Handler con el Looper principal para que persista aunque la Activity se destruya.
     */
    private fun programarCambioEstadoReserva(bookingId: String) {
        android.util.Log.d("PaymentActivity", "Programando cambio automático de estado para reserva: $bookingId (en 1 minuto)")
        
        // Usar Handler con Looper principal para que persista aunque la Activity se destruya
        val handler = Handler(Looper.getMainLooper())
        
        handler.postDelayed({
            android.util.Log.d("PaymentActivity", "1 minuto transcurrido, cambiando estado de reserva $bookingId a CONFIRMADO...")
            
            // Ejecutar en un hilo de fondo para no bloquear el hilo principal
            Thread {
                try {
                    // Cambiar el estado a CONFIRMADO usando el contexto de la aplicación
                    val repository = PeruvianServiceRepository.getInstance(applicationContext)
                    val exito = repository.cambiarEstadoReservaAConfirmado(bookingId)
                    
                    if (exito) {
                        android.util.Log.d("PaymentActivity", "✓ Reserva $bookingId cambiada automáticamente a CONFIRMADO después de 1 minuto")
                        android.util.Log.d("PaymentActivity", "✓ Tour del día actual marcado como COMPLETADO - usuarios pueden subir fotos")
                    } else {
                        android.util.Log.w("PaymentActivity", "✗ No se pudo cambiar el estado de la reserva $bookingId (puede que ya esté confirmada)")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PaymentActivity", "✗ Error al cambiar estado de reserva: ${e.message}", e)
                }
            }.start()
        }, 60000) // 1 minuto = 60000 milisegundos
        
        android.util.Log.d("PaymentActivity", "Handler programado para ejecutarse en 1 minuto")
    }

    private fun abrirVoucher(comprobante: com.grupo4.appreservas.modelos.Recibo) {
        val intent = android.content.Intent(this, VoucherActivity::class.java)
        intent.putExtra("COMPROBANTE", comprobante)
        intent.putExtra("USUARIO_ID", reserva.usuarioId)
        intent.putExtra("PUNTOS_GANADOS", PuntosUsuario.PUNTOS_POR_RESERVA)
        startActivity(intent)
        finish()
    }
}

