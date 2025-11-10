package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.Tour
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DestinoRepository private constructor(private val dbHelper: DatabaseHelper) {

    companion object {
        @Volatile
        private var instance: DestinoRepository? = null

        fun getInstance(context: Context): DestinoRepository {
            return instance ?: synchronized(this) {
                val dbHelper = DatabaseHelper(context)
                // Crear instancia - la carga de datos ya se hace en DatabaseHelper.insertDefaultData()
                // Solo cargar aquí si por alguna razón no se cargaron en onCreate
                val repo = DestinoRepository(dbHelper)
                instance ?: repo.also {
                    instance = it
                    // Verificar y cargar datos si es necesario (respaldo por si acaso)
                    it.cargarDatosInicialesSiNecesario()
                }
            }
        }
    }

    fun getDestinos(): List<Destino> {
        return dbHelper.obtenerTodosLosDestinos()
    }

    fun getDetalle(destinoId: String): Destino? {
        return dbHelper.obtenerDestinoPorId(destinoId)
    }

    fun getDisponibilidad(destinoId: String, fecha: String): Map<String, Any>? {
        val destino = dbHelper.obtenerDestinoPorId(destinoId) ?: return null
        
        // Obtener tour slots para esta fecha y destino
        val slots = dbHelper.obtenerTourSlotsPorFecha(fecha)
        val slotDestino = slots.find { it.tourSlotId.startsWith(destinoId) }
        
        val cuposDisponibles = if (slotDestino != null) {
            slotDestino.cuposDisponibles()
        } else {
            destino.maxPersonas // Si no hay slot, asumimos capacidad completa
        }

        return mapOf(
            "destinoId" to destinoId,
            "fecha" to fecha,
            "cuposDisponibles" to cuposDisponibles,
            "cuposTotales" to destino.maxPersonas
        )
    }

    fun put(key: String, destino: Destino) {
        dbHelper.insertarDestino(destino)
    }

    fun get(key: String): Destino? {
        return dbHelper.obtenerDestinoPorId(key)
    }

    fun actualizarDestino(destino: Destino): Boolean {
        return dbHelper.actualizarDestino(destino) > 0
    }

    fun eliminarDestino(destinoId: String): Boolean {
        return dbHelper.eliminarDestino(destinoId) > 0
    }

    private fun cargarDatosInicialesSiNecesario() {
        // Solo cargar datos si la tabla está vacía
        if (dbHelper.contarDestinos() == 0) {
            val destinos = listOf(
                Destino(
                    id = "dest_001",
                    nombre = "Tour Machu Picchu Clásico",
                    ubicacion = "Cusco, Perú",
                    descripcion = "Descubre la majestuosa ciudadela inca...",
                    precio = 450.0,
                    duracionHoras = 12,
                    maxPersonas = 15,
                    categorias = listOf("Cultura", "Arqueología", "Naturaleza"),
                    imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Machu_Picchu%2C_Peru_%282018%29.jpg/1200px-Machu_Picchu%2C_Peru_%282018%29.jpg",
                    calificacion = 4.8,
                    numReseñas = 124,
                    disponibleTodosDias = true,
                    incluye = listOf("Transporte en tren panorámico", "Guía profesional en español", "Entrada a Machu Picchu", "Almuerzo buffet", "Seguro de viaje")
                ),
                Destino(
                    id = "dest_002",
                    nombre = "Líneas de Nazca Tour Aéreo",
                    ubicacion = "Ica, Perú",
                    descripcion = "Sobrevuela las misteriosas líneas de Nazca...",
                    precio = 380.0,
                    duracionHoras = 6,
                    maxPersonas = 8,
                    categorias = listOf("Aventura", "Arqueología", "Aéreo"),
                    imagenUrl = "https://s3.abcstatics.com/abc/www/multimedia/internacional/2024/01/08/lineas-nazca-unsplash-klJF-U601062084005ad-1200x840@abc.jpg",
                    calificacion = 4.6,
                    numReseñas = 87,
                    disponibleTodosDias = false,
                    incluye = listOf("Vuelo de 35 minutos", "Traslado hotel-aeródromo", "Certificado de vuelo", "Seguro de vuelo")
                )
            )

            // Obtener el primer usuario administrador para asociar los tours
            val adminId = dbHelper.obtenerIdPrimerAdministrador()
            
            // Crear tours para múltiples fechas (próximos 14 días) para cada destino
            val calendario = java.util.Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            destinos.forEach { destino ->
                dbHelper.insertarDestino(destino)

                // Crear un tour para cada uno de los próximos 14 días
                for (dia in 0..13) {
                    calendario.time = Date()
                    calendario.add(java.util.Calendar.DAY_OF_MONTH, dia)
                    val fechaTour = dateFormat.format(calendario.time)
                    
                    // Crear un tour único para cada fecha
                    // El tourId será: destinoId_fecha (ej: dest_001_2025-11-09)
                    val tourId = "${destino.id}_$fechaTour"
                    
                    // Horarios diferentes para variar (mañana, tarde)
                    val hora = if (dia % 2 == 0) "09:00" else "14:00"
                    
                    val tour = Tour(
                        tourId = tourId,
                        nombre = destino.nombre,
                        fecha = fechaTour,
                        hora = hora,
                        puntoEncuentro = destino.ubicacion,
                        capacidad = destino.maxPersonas,
                        participantesConfirmados = 0,
                        estado = if (dia == 0) "Disponible" else "Pendiente"
                    )

                    // Insertar tour asociado al primer administrador
                    dbHelper.insertarTour(tour, guiaId = adminId)
                }
            }
            
            // Asegurar que todos los tours existentes estén asociados al primer administrador
            dbHelper.asociarTodosLosToursAAdministradores()
        }
    }
}