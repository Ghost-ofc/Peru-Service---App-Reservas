package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.grupo4.appreservas.R
import com.grupo4.appreservas.modelos.Usuario
import com.grupo4.appreservas.repository.PeruvianServiceRepository
import com.grupo4.appreservas.viewmodel.AutenticacionViewModel

/**
 * Activity principal que muestra el dashboard según el rol del usuario.
 * Equivalente a PanelPrincipalActivity del diagrama UML.
 * Recibe registroExitoso(usuario) de RegistroActivity y loginExitoso(usuario) de LoginActivity.
 */
class PanelPrincipalActivity : AppCompatActivity() {

    private lateinit var viewModel: AutenticacionViewModel
    private var usuarioId: Int = 0
    private var rolId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_principal)

        inicializarDependencias()
        
        // Obtener datos del usuario desde el Intent
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        rolId = intent.getIntExtra("ROL_ID", 0)

        // Verificar si se recibió registroExitoso o loginExitoso
        val registroExitoso = intent.getBooleanExtra("REGISTRO_EXITOSO", false)
        val loginExitoso = intent.getBooleanExtra("LOGIN_EXITOSO", false)

        if (usuarioId == 0 || rolId == 0) {
            // Si no hay datos en el Intent, intentar obtener el usuario actual del ViewModel
            val usuarioActual = viewModel.obtenerUsuarioActual()
            if (usuarioActual != null) {
                mostrarSegunRol(usuarioActual)
            } else {
                Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }

        // Crear un objeto Usuario temporal con los datos recibidos
        val usuario = Usuario(
            usuarioId = usuarioId,
            nombreCompleto = "",
            correo = "",
            contrasena = "",
            rolId = rolId
        )

        mostrarSegunRol(usuario)
    }

    private fun inicializarDependencias() {
        val repository = PeruvianServiceRepository.getInstance(this)
        viewModel = ViewModelProvider(this, AutenticacionViewModelFactory(repository))[AutenticacionViewModel::class.java]
    }

    /**
     * Muestra el contenido según el rol del usuario.
     * Equivalente a mostrarSegunRol(usuario) del diagrama UML.
     * 
     * @param usuario El usuario autenticado
     */
    private fun mostrarSegunRol(usuario: Usuario) {
        when (usuario.rolId) {
            1 -> {
                // Administrador/Guía - redirigir al panel de guía (VistaPanelGuia)
                val intent = Intent(this, PanelGuiaActivity::class.java)
                intent.putExtra("USUARIO_ID", usuario.usuarioId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            2 -> {
                // Turista - redirigir al catálogo (VistaPanelTurista)
                val intent = Intent(this, CatalogoActivity::class.java)
                intent.putExtra("USUARIO_ID", usuario.usuarioId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            else -> {
                Toast.makeText(this, "Error: Rol no reconocido", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

