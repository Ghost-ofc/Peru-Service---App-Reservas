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
import com.grupo4.appreservas.service.RolesService
import com.grupo4.appreservas.service.UsuariosService

/**
 * Activity de Registro según el diagrama UML.
 * Equivalente a RegistroActivity del diagrama.
 * 
 * En arquitectura MVC, esta Activity (Vista) usa el ControlAuth (Controller)
 * para manejar la lógica de registro.
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var controlAuth: ControlAuth

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
        // Inicializar servicios y controlador según arquitectura MVC
        val usuariosService = UsuariosService(this)
        val rolesService = RolesService(this)
        controlAuth = ControlAuth(usuariosService, rolesService)
    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.et_nombre)
        etCorreo = findViewById(R.id.et_correo)
        etContrasena = findViewById(R.id.et_contrasena)
        btnCrearCuenta = findViewById(R.id.btn_crear_cuenta)
        tvIniciarSesion = findViewById(R.id.tv_iniciar_sesion)
    }

    /**
     * Muestra el formulario de registro.
     * Equivalente a mostrarFormulario() del diagrama UML.
     */
    private fun mostrarFormulario() {
        btnCrearCuenta.setOnClickListener {
            enviarDatos()
        }

        tvIniciarSesion.setOnClickListener {
            finish() // Volver al login
        }
    }

    /**
     * Envía los datos del formulario para registro.
     * Equivalente a enviarDatos(nombre, correo, contrasena, rol) del diagrama UML.
     */
    private fun enviarDatos() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()
        val rol = 2 // Siempre Turista para registro público

        // Usar ControlAuth para registrar usuario (patrón MVC)
        val resultado = controlAuth.registrar(nombre, correo, contrasena, rol)

        when (resultado) {
            is ControlAuth.ResultadoAuth.Exito -> {
                val usuario = resultado.usuario
                mostrarConfirmacion("Cuenta creada exitosamente")

                // Redirigir al catálogo después del registro
                val intent = Intent(this, CatalogoActivity::class.java)
                intent.putExtra("USUARIO_ID", usuario.usuarioId)
                intent.putExtra("ROL_ID", usuario.rolId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            is ControlAuth.ResultadoAuth.Error -> {
                mostrarError(resultado.mensaje)
            }
        }
    }

    /**
     * Muestra un mensaje de confirmación.
     * Equivalente a mostrarConfirmacion(mensaje) del diagrama UML.
     */
    private fun mostrarConfirmacion(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra un mensaje de error.
     */
    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}