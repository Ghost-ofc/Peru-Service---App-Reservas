package com.grupo4.appreservas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo4.appreservas.R
import com.grupo4.appreservas.repository.PeruvianServiceRepository

/**
 * Activity principal que muestra el dashboard según el rol del usuario.
 * Corresponde a la HU: Registro/Inicio de sesión según mi rol.
 */
class PanelPrincipalActivity : AppCompatActivity() {

    private var usuarioId: Int = 0
    private var rolId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_principal)

        usuarioId = intent.getIntExtra("USUARIO_ID", 0)
        rolId = intent.getIntExtra("ROL_ID", 0)

        if (usuarioId == 0 || rolId == 0) {
            Toast.makeText(this, "Error: No se proporcionó información del usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mostrarSegunRol(rolId)
    }

    /**
     * Muestra el contenido según el rol del usuario.
     * Equivalente a mostrarSegunRol(rol) del diagrama UML.
     */
    private fun mostrarSegunRol(rolId: Int) {
        when (rolId) {
            1 -> {
                // Administrador - redirigir al panel de guía
                val intent = Intent(this, PanelGuiaActivity::class.java)
                intent.putExtra("USUARIO_ID", usuarioId)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            2 -> {
                // Turista - redirigir al catálogo
                val intent = Intent(this, CatalogoActivity::class.java)
                intent.putExtra("USUARIO_ID", usuarioId)
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

