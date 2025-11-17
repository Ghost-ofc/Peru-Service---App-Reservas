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
        viewModel.usuario.observe(this) { usuario ->
            if (usuario != null) {
                redirigirSegunRol(usuario)
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
            iniciarSesion()
        }

        tvRegistrate.setOnClickListener {
            abrirRegistro()
        }
    }

    private fun iniciarSesion() {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor, completa todos los campos")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError("Por favor, ingresa un correo válido")
            return
        }

        lifecycleScope.launch {
            try {
                viewModel.iniciarSesion(correo, contrasena)
            } catch (e: Exception) {
                mostrarError("Error al iniciar sesión: ${e.message}")
            }
        }
    }

    private fun redirigirSegunRol(usuario: com.grupo4.appreservas.modelos.Usuario) {
        val intent = Intent(this, PanelPrincipalActivity::class.java)
        intent.putExtra("USUARIO_ID", usuario.usuarioId)
        intent.putExtra("ROL_ID", usuario.rolId)
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

