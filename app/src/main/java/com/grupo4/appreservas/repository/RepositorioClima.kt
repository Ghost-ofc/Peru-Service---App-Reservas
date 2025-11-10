package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Clima
import java.util.*

/**
 * Repositorio de Clima según el diagrama UML.
 * Equivalente a RepositorioClima del diagrama.
 * 
 * NOTA: Esta es una implementación simulada. En una aplicación real,
 * se conectaría a un servicio de clima (como OpenWeatherMap, WeatherAPI, etc.)
 */
class RepositorioClima(context: Context) {
    
    // Almacenar condiciones climáticas previas para detectar cambios
    private val condicionesPrevias = mutableMapOf<String, Clima>()

    /**
     * Obtiene las condiciones climáticas actuales para una ubicación.
     * Equivalente a obtenerCondiciones(actualUbicacion): Clima del diagrama UML.
     * 
     * @param actualUbicacion Ubicación para la cual obtener el clima
     * @return Condiciones climáticas actuales
     */
    fun obtenerCondiciones(actualUbicacion: String): Clima {
        // SIMULACIÓN: En una aplicación real, esto haría una llamada a una API de clima
        // Por ahora, simulamos condiciones climáticas aleatorias basadas en la ubicación
        
        val condiciones = when {
            actualUbicacion.contains("Cusco", ignoreCase = true) -> {
                // Cusco tiene clima variable (soleado por la mañana, lluvioso por la tarde)
                val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (hora in 6..12) {
                    Clima(actualUbicacion, 18.0, "Soleado", 45, Date())
                } else {
                    Clima(actualUbicacion, 12.0, "Lluvioso", 75, Date())
                }
            }
            actualUbicacion.contains("Ica", ignoreCase = true) || actualUbicacion.contains("Nazca", ignoreCase = true) -> {
                // Nazca/Ica tiene clima desértico (soleado y seco)
                Clima(actualUbicacion, 25.0, "Soleado", 30, Date())
            }
            actualUbicacion.contains("Puno", ignoreCase = true) || actualUbicacion.contains("Titicaca", ignoreCase = true) -> {
                // Lago Titicaca tiene clima frío y puede ser lluvioso
                Clima(actualUbicacion, 10.0, "Nublado", 60, Date())
            }
            else -> {
                // Clima genérico
                Clima(actualUbicacion, 20.0, "Parcialmente nublado", 50, Date())
            }
        }
        
        // Almacenar condiciones previas para detectar cambios
        condicionesPrevias[actualUbicacion] = condiciones
        
        return condiciones
    }

    /**
     * Detecta si hay un cambio significativo en las condiciones climáticas.
     * Equivalente a detectarCambio(condiciones): Boolean del diagrama UML.
     * 
     * @param condiciones Condiciones climáticas actuales
     * @return true si hay un cambio significativo, false en caso contrario
     */
    fun detectarCambio(condiciones: Clima): Boolean {
        val condicionesPrevia = condicionesPrevias[condiciones.ubicacion]
        
        if (condicionesPrevia == null) {
            // Primera vez que se consulta esta ubicación, no hay cambio
            return false
        }
        
        // Detectar cambios significativos:
        // 1. Cambio en la condición (soleado -> lluvioso, etc.)
        val cambioCondicion = condicionesPrevia.condicion != condiciones.condicion
        
        // 2. Cambio significativo en la temperatura (más de 5 grados)
        val cambioTemperatura = Math.abs(condicionesPrevia.temperatura - condiciones.temperatura) > 5.0
        
        // 3. Cambio significativo en la humedad (más de 20%)
        val cambioHumedad = Math.abs(condicionesPrevia.humedad - condiciones.humedad) > 20
        
        // Considerar cambio significativo si hay cambio en condición o cambios grandes en temperatura/humedad
        return cambioCondicion || cambioTemperatura || cambioHumedad
    }

    /**
     * Obtiene las condiciones climáticas previas para una ubicación.
     * 
     * @param ubicacion Ubicación
     * @return Condiciones climáticas previas o null si no existen
     */
    fun obtenerCondicionesPrevias(ubicacion: String): Clima? {
        return condicionesPrevias[ubicacion]
    }
}

