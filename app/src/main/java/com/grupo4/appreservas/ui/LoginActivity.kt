package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.grupo4.appreservas.R
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.AutenticacionViewModel
import kotlinx.coroutines.launch

/**
 * Activity para iniciar sesión.
 * Corresponde a la HU: Inicio de sesión según mi rol.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AutenticacionViewModel
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: Button
    private lateinit var tvRegistrate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inicializarDependencias()
        inicializarVistas()
        configurarListeners()
    }

    private fun inicializarDependencias() {
        val repository = PeruvianServiceRepository.getInstance(this)
        viewModel = ViewModelProvider(this, AutenticacionViewModelFactory(repository))[AutenticacionViewModel::class.java]

        // Observar cambios en el usuario autenticado
        viewModel.usuarioAutenticado.observe(this) { usuario ->
            if (usuario != null) {
                // Enviar loginExitoso(usuario) a PanelPrincipalActivity según diagrama UML
                enviarLoginExitoso(usuario)
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
        etCorreo = findViewById(R.id.et_correo)
        etContrasena = findViewById(R.id.et_contrasena)
        btnIniciarSesion = findViewById(R.id.btn_iniciar_sesion)
        tvRegistrate = findViewById(R.id.tv_registrate)
    }

    private fun configurarListeners() {
        btnIniciarSesion.setOnClickListener {
            enviarCredenciales()
        }

        tvRegistrate.setOnClickListener {
            abrirRegistro()
        }
    }

    /**
     * Envía las credenciales al ViewModel.
     * Equivalente a enviarCredenciales(nombreUsuario, contrasena) del diagrama UML.
     */
    private fun enviarCredenciales() {
        val nombreUsuario = etCorreo.text.toString().trim() // En este sistema, nombreUsuario es el correo
        val contrasena = etContrasena.text.toString()

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor, completa todos los campos")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(nombreUsuario).matches()) {
            mostrarError("Por favor, ingresa un correo válido")
            return
        }

        // Enviar credenciales al ViewModel
        viewModel.iniciarSesion(nombreUsuario, contrasena)
    }

    /**
     * Envía el evento loginExitoso(usuario) a PanelPrincipalActivity.
     * Equivalente a loginExitoso(usuario) del diagrama UML.
     */
    private fun enviarLoginExitoso(usuario: com.grupo4.appreservas.modelos.Usuario) {
        val intent = Intent(this, PanelPrincipalActivity::class.java)
        intent.putExtra("USUARIO_ID", usuario.usuarioId)
        intent.putExtra("ROL_ID", usuario.rolId)
        intent.putExtra("LOGIN_EXITOSO", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun abrirRegistro() {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Factory para crear AutenticacionViewModel con dependencias.
 */
class AutenticacionViewModelFactory(
    private val repository: PeruvianServiceRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AutenticacionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AutenticacionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

