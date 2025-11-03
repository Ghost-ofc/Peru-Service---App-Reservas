package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R
import com.grupo4.appreservas.repository.UsuarioRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var repositorioUsuarios: UsuarioRepository

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: Button
    private lateinit var tvRegistrate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inicializarDependencias()
        inicializarVistas()
        mostrarFormulario()
    }

    private fun inicializarDependencias() {
        repositorioUsuarios = UsuarioRepository(this)
    }

    private fun inicializarVistas() {
        etCorreo = findViewById(R.id.et_correo)
        etContrasena = findViewById(R.id.et_contrasena)
        btnIniciarSesion = findViewById(R.id.btn_iniciar_sesion)
        tvRegistrate = findViewById(R.id.tv_registrate)
    }

    private fun mostrarFormulario() {
        btnIniciarSesion.setOnClickListener {
            enviarCredenciales()
        }

        tvRegistrate.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun enviarCredenciales(correo: String, contrasena: String) {
        if (correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor completa todos los campos")
            return
        }

        if (!esCorreoValido(correo)) {
            mostrarError("Correo electrónico inválido")
            return
        }

        val usuario = repositorioUsuarios.validarCredenciales(correo, contrasena)

        if (usuario != null) {
            // Login exitoso, redirigir al panel principal
            if (usuario.rolId == 1) {
                val intent = Intent(this, PanelGuiaActivity::class.java)
                intent.putExtra("USUARIO_ID", usuario.usuarioId)
                intent.putExtra("ROL_ID", usuario.rolId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                Toast.makeText(this, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, CatalogoActivity::class.java)
                intent.putExtra("USUARIO_ID", usuario.usuarioId)
                intent.putExtra("ROL_ID", usuario.rolId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                Toast.makeText(this, "Bienvenido Usuario", Toast.LENGTH_SHORT).show()
            }

        } else {
            mostrarError("Credenciales incorrectas")
        }
    }

    private fun enviarCredenciales() {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()
        enviarCredenciales(correo, contrasena)
    }

    private fun esCorreoValido(correo: String): Boolean {
        val patronCorreo = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return patronCorreo.matches(correo)
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}