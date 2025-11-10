package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R
import com.grupo4.appreservas.controller.ControlAuth
import com.grupo4.appreservas.service.RolesService
import com.grupo4.appreservas.service.UsuariosService
import com.grupo4.appreservas.service.NotificacionesScheduler

/**
 * Panel Principal Activity según el diagrama UML.
 * Equivalente a PanelPrincipalActivity del diagrama.
 * 
 * Esta Activity muestra contenido según el rol del usuario.
 * En arquitectura MVC, usa ControlAuth para obtener el rol.
 */
class PanelPrincipalActivity : AppCompatActivity() {

    private lateinit var controlAuth: ControlAuth
    private var usuarioId: Int = 0
    private var rolId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_principal)

        obtenerDatosUsuario()
        inicializarDependencias()
        mostrarSegunRol(rolId)
    }

    private fun obtenerDatosUsuario() {
        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        rolId = intent.getIntExtra("ROL_ID", 0)

        if (usuarioId == 0 || rolId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
            redirigirALogin()
        }
    }

    private fun inicializarDependencias() {
        val usuariosService = UsuariosService(this)
        val rolesService = RolesService(this)
        controlAuth = ControlAuth(usuariosService, rolesService)

        // Programar notificaciones push periódicas
        val notificacionesScheduler = NotificacionesScheduler(this)
        notificacionesScheduler.programarNotificacionesPeriodicas()
    }

    /**
     * Muestra contenido según el rol del usuario.
     * Equivalente a mostrarSegunRol(rol) del diagrama UML.
     * 
     * @param rolId ID del rol del usuario (1 = Administrador, 2 = Turista)
     */
    private fun mostrarSegunRol(rolId: Int) {
        // Obtener el rol completo usando ControlAuth
        val rol = controlAuth.obtenerRol(usuarioId)

        when (rolId) {
            1 -> {
                // Administrador - Redirigir a PanelGuiaActivity
                val intent = Intent(this, PanelGuiaActivity::class.java)
                intent.putExtra("USUARIO_ID", usuarioId)
                intent.putExtra("ROL_ID", rolId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            2 -> {
                // Turista - Redirigir a CatalogoActivity
                val intent = Intent(this, CatalogoActivity::class.java)
                intent.putExtra("USUARIO_ID", usuarioId)
                intent.putExtra("ROL_ID", rolId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            else -> {
                Toast.makeText(this, "Rol no válido", Toast.LENGTH_SHORT).show()
                redirigirALogin()
            }
        }
    }

    private fun redirigirALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

