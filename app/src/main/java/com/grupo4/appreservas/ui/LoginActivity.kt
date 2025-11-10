package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R
import com.grupo4.appreservas.controller.ControlAuth
import com.grupo4.appreservas.repository.DestinoRepository
import com.grupo4.appreservas.service.RolesService
import com.grupo4.appreservas.service.UsuariosService

/**
 * Activity de Login según el diagrama UML.
 * Equivalente a LoginActivity del diagrama.
 * 
 * En arquitectura MVC, esta Activity (Vista) usa el ControlAuth (Controller)
 * para manejar la lógica de autenticación.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var controlAuth: ControlAuth

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: Button
    private lateinit var tvRegistrate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar DestinoRepository para asegurar que los destinos se carguen en la DB
        // Esto garantiza que Machu Picchu y Líneas de Nazca estén disponibles desde el inicio
        DestinoRepository.getInstance(this)
        
        inicializarDependencias()
        inicializarVistas()
        mostrarFormulario()
    }

    private fun inicializarDependencias() {
        // Inicializar servicios y controlador según arquitectura MVC
        val usuariosService = UsuariosService(this)
        val rolesService = RolesService(this)
        controlAuth = ControlAuth(usuariosService, rolesService)
    }

    private fun inicializarVistas() {
        etCorreo = findViewById(R.id.et_correo)
        etContrasena = findViewById(R.id.et_contrasena)
        btnIniciarSesion = findViewById(R.id.btn_iniciar_sesion)
        tvRegistrate = findViewById(R.id.tv_registrate)
    }

    /**
     * Muestra el formulario de login.
     * Equivalente a mostrarFormulario() del diagrama UML.
     */
    private fun mostrarFormulario() {
        btnIniciarSesion.setOnClickListener {
            enviarCredenciales()
        }

        tvRegistrate.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Envía las credenciales para autenticación.
     * Equivalente a enviarCredenciales(correo, contrasena) del diagrama UML.
     */
    private fun enviarCredenciales() {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()

        // Usar ControlAuth para iniciar sesión (patrón MVC)
        val resultado = controlAuth.iniciarSesion(correo, contrasena)

        when (resultado) {
            is ControlAuth.ResultadoAuth.Exito -> {
                val usuario = resultado.usuario
                // Redirigir según el rol usando el método del controlador
                redirigirSegunRol(usuario.rolId, usuario.usuarioId)
            }
            is ControlAuth.ResultadoAuth.Error -> {
                mostrarError(resultado.mensaje)
            }
        }
    }

    /**
     * Redirige al usuario según su rol.
     * Redirige a PanelPrincipalActivity que mostrará el contenido según el rol.
     */
    private fun redirigirSegunRol(rolId: Int, usuarioId: Int) {
        // Redirigir a PanelPrincipalActivity que mostrará contenido según el rol
        val intent = Intent(this, PanelPrincipalActivity::class.java)
        intent.putExtra("USUARIO_ID", usuarioId)
        intent.putExtra("ROL_ID", rolId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Muestra un mensaje de error.
     * Equivalente a mostrarError(mensaje) del diagrama UML.
     */
    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}