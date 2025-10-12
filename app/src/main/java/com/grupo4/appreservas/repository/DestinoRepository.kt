package com.grupo4.appreservas.repository

import com.grupo4.appreservas.modelos.Destino


class DestinoRepository private constructor() {

    private val destinosCache = mutableMapOf<String, Destino>()

    companion object {
        @Volatile
        private var instance: DestinoRepository? = null

        fun getInstance(): DestinoRepository {
            return instance ?: synchronized(this) {
                instance ?: DestinoRepository().also {
                    instance = it
                    it.cargarDatosIniciales()
                }
            }
        }
    }

    fun getDestinos(): List<Destino> {
        return destinosCache.values.toList()
    }

    fun getDetalle(destinoId: String): Destino? {
        return destinosCache[destinoId]
    }

    fun getDisponibilidad(destinoId: String, fecha: String): Map<String, Any>? {
        val destino = destinosCache[destinoId] ?: return null

        return mapOf(
            "destinoId" to destinoId,
            "fecha" to fecha,
            "cuposDisponibles" to 6,
            "cuposTotales" to destino.maxPersonas
        )
    }

    fun put(key: String, destino: Destino) {
        destinosCache[key] = destino
    }

    fun get(key: String): Destino? {
        return destinosCache[key]
    }

    private fun cargarDatosIniciales() {
        val destino1 = Destino(
            id = "dest_001",
            nombre = "Tour Machu Picchu Clásico",
            ubicacion = "Cusco, Perú",
            descripcion = "Descubre la majestuosa ciudadela inca de Machu Picchu con guía experto. Incluye transporte en tren panorámico y entrada al santuario histórico.",
            precio = 450.0,
            duracionHoras = 12,
            maxPersonas = 15,
            categorias = listOf("Cultura", "Arqueología", "Naturaleza"),
            imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Machu_Picchu%2C_Peru_%282018%29.jpg/1200px-Machu_Picchu%2C_Peru_%282018%29.jpg",
            calificacion = 4.8,
            numReseñas = 124,
            disponibleTodosDias = true,
            incluye = listOf(
                "Transporte en tren panorámico",
                "Guía profesional en español",
                "Entrada a Machu Picchu",
                "Almuerzo buffet",
                "Seguro de viaje"
            )
        )

        val destino2 = Destino(
            id = "dest_002",
            nombre = "Líneas de Nazca Tour Aéreo",
            ubicacion = "Ica, Perú",
            descripcion = "Sobrevuela las misteriosas líneas de Nazca y observa las impresionantes figuras desde el aire.",
            precio = 380.0,
            duracionHoras = 6,
            maxPersonas = 8,
            categorias = listOf("Aventura", "Arqueología", "Aéreo"),
            imagenUrl = "https://s3.abcstatics.com/abc/www/multimedia/internacional/2024/01/08/lineas-nazca-unsplash-klJF-U601062084005ad-1200x840@abc.jpg",
            calificacion = 4.6,
            numReseñas = 87,
            disponibleTodosDias = false,
            incluye = listOf(
                "Vuelo de 35 minutos",
                "Traslado hotel-aeródromo",
                "Certificado de vuelo",
                "Seguro de vuelo"
            )
        )

        destinosCache["dest_001"] = destino1
        destinosCache["dest_002"] = destino2
    }
}