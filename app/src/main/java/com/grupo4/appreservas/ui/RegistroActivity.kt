package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.UsuarioRepository


class RegistroActivity : AppCompatActivity() {

    private lateinit var repositorioUsuarios: UsuarioRepository

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
        mostrarFormulario()
    }

    private fun inicializarDependencias() {
        repositorioUsuarios = UsuarioRepository(this)
    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.et_nombre)
        etCorreo = findViewById(R.id.et_correo)
        etContrasena = findViewById(R.id.et_contrasena)
        btnCrearCuenta = findViewById(R.id.btn_crear_cuenta)
        tvIniciarSesion = findViewById(R.id.tv_iniciar_sesion)
    }

    private fun mostrarFormulario() {
        btnCrearCuenta.setOnClickListener {
            enviarDatos()
        }

        tvIniciarSesion.setOnClickListener {
            finish() // Volver al login
        }
    }

    private fun enviarDatos(nombre: String, correo: String, contrasena: String, rol: Int) {
        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor completa todos los campos")
            return
        }

        if (!esCorreoValido(correo)) {
            mostrarError("Correo electrónico inválido")
            return
        }

        if (contrasena.length < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        val usercreate = Usuario(
            nombreCompleto = nombre,
            correo = correo,
            contrasena = contrasena,
            rolId = rol
        )

        val usuario = repositorioUsuarios.crearUsuario(usercreate)

        if (usuario != null) {
            mostrarConfirmacion("Cuenta creada exitosamente")

            val intent = Intent(this, CatalogoActivity::class.java)
            intent.putExtra("USUARIO_ID", usuario.usuarioId)
            intent.putExtra("ROL_ID", usuario.rolId)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            mostrarError("El correo ya está registrado")
        }
    }

    private fun enviarDatos() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()
        val rol = 2 // Siempre Turista para registro público

        enviarDatos(nombre, correo, contrasena, rol)
    }

    private fun esCorreoValido(correo: String): Boolean {
        val patronCorreo = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return patronCorreo.matches(correo)
    }

    private fun mostrarConfirmacion(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}