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
        viewModel.usuario.observe(this) { usuario ->
            if (usuario != null) {
                mostrarConfirmacion("Cuenta creada exitosamente")
                redirigirSegunRol(usuario)
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
            registrar()
        }

        tvIniciarSesion.setOnClickListener {
            abrirLogin()
        }
    }

    private fun registrar() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor, completa todos los campos")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError("Por favor, ingresa un correo válido")
            return
        }

        if (contrasena.length < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // El registro solo es para turistas (rolId = 2)
        val rolId = 2 // Turista

        lifecycleScope.launch {
            try {
                viewModel.registrar(nombre, correo, contrasena, rolId)
            } catch (e: Exception) {
                mostrarError("Error al crear la cuenta: ${e.message}")
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

