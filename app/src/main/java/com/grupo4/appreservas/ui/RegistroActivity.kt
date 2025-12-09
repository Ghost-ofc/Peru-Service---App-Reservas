package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.grupo4.appreservas.R
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.AutenticacionViewModel
import kotlinx.coroutines.launch

/**
 * Activity para registro de usuarios.
 * Corresponde a la HU: Registro según mi rol.
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var viewModel: AutenticacionViewModel
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnCrearCuenta: Button
    private lateinit var tvIniciarSesion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        inicializarDependencias()
        inicializarVistas()
        configurarListeners()
    }

    private fun inicializarDependencias() {
        val repository = PeruvianServiceRepository.getInstance(this)
        viewModel = ViewModelProvider(this, AutenticacionViewModelFactory(repository))[AutenticacionViewModel::class.java]

        // Observar cambios en el usuario registrado
        viewModel.usuarioAutenticado.observe(this) { usuario ->
            if (usuario != null) {
                mostrarConfirmacion("Cuenta creada exitosamente")
                // Enviar registroExitoso(usuario) a PanelPrincipalActivity según diagrama UML
                enviarRegistroExitoso(usuario)
            }
        }

        // Observar mensajes de estado
        viewModel.mensajeEstado.observe(this) { mensaje ->
            mensaje?.let {
                mostrarError(it)
            }
        }
    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.et_nombre)
        etCorreo = findViewById(R.id.et_correo)
        etContrasena = findViewById(R.id.et_contrasena)
        btnCrearCuenta = findViewById(R.id.btn_crear_cuenta)
        tvIniciarSesion = findViewById(R.id.tv_iniciar_sesion)
    }

    private fun configurarListeners() {
        btnCrearCuenta.setOnClickListener {
            enviarDatosRegistro()
        }

        tvIniciarSesion.setOnClickListener {
            abrirLogin()
        }
    }

    /**
     * Envía los datos de registro al ViewModel.
     * Equivalente a enviarDatosRegistro(nombreCompleto, nombreUsuario, contrasena) del diagrama UML.
     */
    private fun enviarDatosRegistro() {
        val nombreCompleto = etNombre.text.toString().trim()
        val nombreUsuario = etCorreo.text.toString().trim() // En este sistema, nombreUsuario es el correo
        val contrasena = etContrasena.text.toString()

        if (nombreCompleto.isEmpty() || nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor, completa todos los campos")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(nombreUsuario).matches()) {
            mostrarError("Por favor, ingresa un correo válido")
            return
        }

        if (contrasena.length < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Enviar datos al ViewModel (no se envía rol, se asigna el por defecto)
        viewModel.registrarUsuario(nombreCompleto, nombreUsuario, contrasena)
    }

    /**
     * Envía el evento registroExitoso(usuario) a PanelPrincipalActivity.
     * Equivalente a registroExitoso(usuario) del diagrama UML.
     */
    private fun enviarRegistroExitoso(usuario: com.grupo4.appreservas.modelos.Usuario) {
        val intent = Intent(this, PanelPrincipalActivity::class.java)
        intent.putExtra("USUARIO_ID", usuario.usuarioId)
        intent.putExtra("ROL_ID", usuario.rolId)
        intent.putExtra("REGISTRO_EXITOSO", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun abrirLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarConfirmacion(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}

