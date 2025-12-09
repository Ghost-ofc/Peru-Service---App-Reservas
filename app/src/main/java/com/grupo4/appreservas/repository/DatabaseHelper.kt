package com.grupo4.appreservas.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.grupo4.appreservas.modelos.*
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "PeruvianService.db"
        private const val VERSION_BD = 10  // Aumentada para agregar tabla de encuestas (HU-009)

        // Tabla Roles
        private const val TABLA_ROLES = "roles"
        private const val COL_ROL_ID = "rol_id"
        private const val COL_NOMBRE_ROL = "nombre_rol"

        // Tabla Usuarios
        private const val TABLA_USUARIOS = "usuarios"
        private const val COL_USUARIO_ID = "usuario_id"
        private const val COL_NOMBRE_COMPLETO = "nombre_completo"
        private const val COL_CORREO = "correo"
        private const val COL_CONTRASENA = "contrasena"
        private const val COL_ROL_ID_FK = "rol_id"
        private const val COL_FECHA_CREACION = "fecha_creacion"

        // Tabla Destinos
        private const val TABLA_DESTINOS = "destinos"
        private const val COL_DESTINO_ID = "destino_id"
        private const val COL_DESTINO_NOMBRE = "nombre"
        private const val COL_DESTINO_UBICACION = "ubicacion"
        private const val COL_DESTINO_DESCRIPCION = "descripcion"
        private const val COL_DESTINO_PRECIO = "precio"
        private const val COL_DESTINO_DURACION = "duracion_horas"
        private const val COL_DESTINO_MAX_PERSONAS = "max_personas"
        private const val COL_DESTINO_CATEGORIAS = "categorias"
        private const val COL_DESTINO_IMAGEN_URL = "imagen_url"
        private const val COL_DESTINO_CALIFICACION = "calificacion"
        private const val COL_DESTINO_NUM_RESENAS = "num_resenas"
        private const val COL_DESTINO_DISPONIBLE_TODOS_DIAS = "disponible_todos_dias"
        private const val COL_DESTINO_INCLUYE = "incluye"

        // Tabla Tours
        private const val TABLA_TOURS = "tours"
        private const val COL_TOUR_ID = "tour_id"
        private const val COL_TOUR_NOMBRE = "nombre"
        private const val COL_TOUR_FECHA = "fecha"
        private const val COL_TOUR_HORA = "hora"
        private const val COL_TOUR_PUNTO = "punto_encuentro"
        private const val COL_TOUR_CAPACIDAD = "capacidad"
        private const val COL_TOUR_CONFIRMADOS = "confirmados"
        private const val COL_TOUR_ESTADO = "estado"
        private const val COL_TOUR_GUIA_ID = "guia_id"

        // Tabla Tour Slots
        private const val TABLA_TOUR_SLOTS = "tour_slots"
        private const val COL_SLOT_ID = "slot_id"
        private const val COL_SLOT_FECHA = "fecha"
        private const val COL_SLOT_CAPACIDAD = "capacidad"
        private const val COL_SLOT_OCUPADOS = "ocupados"

        // Tabla Reservas
        private const val TABLA_RESERVAS = "reservas"
        private const val COL_RESERVA_ID = "reserva_id"
        private const val COL_RESERVA_TOUR_ID = "tour_id"
        private const val COL_RESERVA_USUARIO_ID = "usuario_id"
        private const val COL_RESERVA_NOMBRE = "nombre_turista"
        private const val COL_RESERVA_DOC = "documento"
        private const val COL_RESERVA_QR = "codigo_qr"
        private const val COL_RESERVA_ESTADO = "estado"
        private const val COL_RESERVA_HORA = "hora_registro"
        private const val COL_RESERVA_PRECIO = "precio_total"
        private const val COL_RESERVA_PAX = "num_personas"
        private const val COL_RESERVA_TOUR_SLOT_ID = "tour_slot_id"

        // Tabla CheckIns
        private const val TABLA_CHECKINS = "checkins"
        private const val COL_CHECKIN_ID = "checkin_id"
        private const val COL_CHECKIN_RESERVA_ID = "reserva_id"
        private const val COL_CHECKIN_GUIA_ID = "guia_id"
        private const val COL_CHECKIN_HORA = "hora_registro"
        private const val COL_CHECKIN_ESTADO = "estado"

        // Tabla Pagos
        private const val TABLA_PAGOS = "pagos"
        private const val COL_PAGO_ID = "pago_id"
        private const val COL_PAGO_BOOKING_ID = "booking_id"
        private const val COL_PAGO_MONTO = "monto"
        private const val COL_PAGO_METODO = "metodo_pago"
        private const val COL_PAGO_ESTADO = "estado"
        private const val COL_PAGO_FECHA = "fecha"
        private const val COL_PAGO_TRANSACCION_ID = "transaccion_id"

        // Tabla Notificaciones
        private const val TABLA_NOTIFICACIONES = "notificaciones"
        private const val COL_NOTIF_ID = "notif_id"
        private const val COL_NOTIF_USUARIO_ID = "usuario_id"
        private const val COL_NOTIF_TIPO = "tipo"
        private const val COL_NOTIF_TITULO = "titulo"
        private const val COL_NOTIF_DESCRIPCION = "descripcion"
        private const val COL_NOTIF_FECHA_CREACION = "fecha_creacion"
        private const val COL_NOTIF_FECHA_LEIDA = "fecha_leida"
        private const val COL_NOTIF_LEIDA = "leida"
        private const val COL_NOTIF_TOUR_ID = "tour_id"
        private const val COL_NOTIF_DESTINO_NOMBRE = "destino_nombre"
        private const val COL_NOTIF_PUNTO_ENCUENTRO = "punto_encuentro"
        private const val COL_NOTIF_HORA_TOUR = "hora_tour"
        private const val COL_NOTIF_DESCUENTO = "descuento"
        private const val COL_NOTIF_RECOMENDACIONES = "recomendaciones"
        private const val COL_NOTIF_CONDICIONES_CLIMA = "condiciones_clima"

        // Tabla Puntos (HU-007)
        private const val TABLA_PUNTOS = "puntos"
        private const val COL_PUNTOS_USUARIO_ID = "usuario_id"
        private const val COL_PUNTOS_ACUMULADOS = "puntos_acumulados"
        private const val COL_PUNTOS_FECHA_ACTUALIZACION = "fecha_actualizacion"

        // Tabla Logros (HU-007)
        private const val TABLA_LOGROS = "logros"
        private const val COL_LOGRO_ID = "logro_id"
        private const val COL_LOGRO_USUARIO_ID = "usuario_id"
        private const val COL_LOGRO_NOMBRE = "nombre"
        private const val COL_LOGRO_DESCRIPCION = "descripcion"
        private const val COL_LOGRO_TIPO = "tipo"
        private const val COL_LOGRO_ICONO = "icono"
        private const val COL_LOGRO_FECHA_DESBLOQUEO = "fecha_desbloqueo"
        private const val COL_LOGRO_DESBLOQUEADO = "desbloqueado"

        // Tabla Fotos (HU-008)
        private const val TABLA_FOTOS = "fotos"
        private const val COL_FOTO_ID = "foto_id"
        private const val COL_FOTO_TOUR_ID = "tour_id"
        private const val COL_FOTO_URL = "url_imagen"
        private const val COL_FOTO_AUTOR = "nombre_autor"
        private const val COL_FOTO_FECHA_SUBIDA = "fecha_subida"
        private const val COL_FOTO_APROBADA = "aprobada"

        // Tabla Encuestas (HU-009)
        private const val TABLA_ENCUESTAS = "encuestas"
        private const val COL_ENCUESTA_ID = "encuesta_id"
        private const val COL_ENCUESTA_TOUR_ID = "tour_id"
        private const val COL_ENCUESTA_USUARIO_ID = "usuario_id"
        private const val COL_ENCUESTA_CALIFICACION = "calificacion"
        private const val COL_ENCUESTA_COMENTARIO = "comentario"
        private const val COL_ENCUESTA_FECHA_RESPUESTA = "fecha_respuesta"

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private const val SEPARADOR_LISTA = "|||" // Separador para listas en BD
    }

    override fun onCreate(db: SQLiteDatabase?) {
        createTables(db)
        insertDefaultData(db)
    }

    private fun createTables(db: SQLiteDatabase?) {
        // Crear tabla Roles
        db?.execSQL("""
            CREATE TABLE $TABLA_ROLES (
                $COL_ROL_ID INTEGER PRIMARY KEY,
                $COL_NOMBRE_ROL TEXT NOT NULL UNIQUE
            )
        """)

        // Crear tabla Usuarios
        db?.execSQL("""
            CREATE TABLE $TABLA_USUARIOS (
                $COL_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE_COMPLETO TEXT NOT NULL,
                $COL_CORREO TEXT NOT NULL UNIQUE,
                $COL_CONTRASENA TEXT NOT NULL,
                $COL_ROL_ID_FK INTEGER NOT NULL,
                $COL_FECHA_CREACION TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_ROL_ID_FK) REFERENCES $TABLA_ROLES($COL_ROL_ID)
            )
        """)

        // Crear tabla Destinos
        db?.execSQL("""
            CREATE TABLE $TABLA_DESTINOS (
                $COL_DESTINO_ID TEXT PRIMARY KEY,
                $COL_DESTINO_NOMBRE TEXT NOT NULL,
                $COL_DESTINO_UBICACION TEXT NOT NULL,
                $COL_DESTINO_DESCRIPCION TEXT,
                $COL_DESTINO_PRECIO REAL NOT NULL,
                $COL_DESTINO_DURACION INTEGER NOT NULL,
                $COL_DESTINO_MAX_PERSONAS INTEGER NOT NULL,
                $COL_DESTINO_CATEGORIAS TEXT,
                $COL_DESTINO_IMAGEN_URL TEXT,
                $COL_DESTINO_CALIFICACION REAL DEFAULT 0,
                $COL_DESTINO_NUM_RESENAS INTEGER DEFAULT 0,
                $COL_DESTINO_DISPONIBLE_TODOS_DIAS INTEGER DEFAULT 1,
                $COL_DESTINO_INCLUYE TEXT
            )
        """)

        // Crear tabla Tours
        db?.execSQL("""
            CREATE TABLE $TABLA_TOURS (
                $COL_TOUR_ID TEXT PRIMARY KEY,
                $COL_TOUR_NOMBRE TEXT NOT NULL,
                $COL_TOUR_FECHA TEXT NOT NULL,
                $COL_TOUR_HORA TEXT NOT NULL,
                $COL_TOUR_PUNTO TEXT NOT NULL,
                $COL_TOUR_CAPACIDAD INTEGER NOT NULL,
                $COL_TOUR_CONFIRMADOS INTEGER DEFAULT 0,
                $COL_TOUR_ESTADO TEXT DEFAULT 'Pendiente',
                $COL_TOUR_GUIA_ID INTEGER DEFAULT 1,
                FOREIGN KEY ($COL_TOUR_GUIA_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
            )
        """)

        // Crear tabla Tour Slots
        db?.execSQL("""
            CREATE TABLE $TABLA_TOUR_SLOTS (
                $COL_SLOT_ID TEXT PRIMARY KEY,
                $COL_SLOT_FECHA TEXT NOT NULL,
                $COL_SLOT_CAPACIDAD INTEGER NOT NULL,
                $COL_SLOT_OCUPADOS INTEGER DEFAULT 0
            )
        """)

        // Crear tabla Reservas
        db?.execSQL("""
            CREATE TABLE $TABLA_RESERVAS (
                $COL_RESERVA_ID TEXT PRIMARY KEY,
                $COL_RESERVA_TOUR_ID TEXT NOT NULL,
                $COL_RESERVA_USUARIO_ID INTEGER NOT NULL,
                $COL_RESERVA_NOMBRE TEXT NOT NULL,
                $COL_RESERVA_DOC TEXT NOT NULL,
                $COL_RESERVA_QR TEXT UNIQUE NOT NULL,
                $COL_RESERVA_ESTADO TEXT DEFAULT 'Pendiente',
                $COL_RESERVA_HORA TEXT,
                $COL_RESERVA_PRECIO REAL DEFAULT 0,
                $COL_RESERVA_PAX INTEGER DEFAULT 1,
                $COL_RESERVA_TOUR_SLOT_ID TEXT,
                FOREIGN KEY ($COL_RESERVA_TOUR_ID) REFERENCES $TABLA_TOURS($COL_TOUR_ID),
                FOREIGN KEY ($COL_RESERVA_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID),
                FOREIGN KEY ($COL_RESERVA_TOUR_SLOT_ID) REFERENCES $TABLA_TOUR_SLOTS($COL_SLOT_ID)
            )
        """)

        // Crear tabla CheckIns
        db?.execSQL("""
            CREATE TABLE $TABLA_CHECKINS (
                $COL_CHECKIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CHECKIN_RESERVA_ID TEXT NOT NULL,
                $COL_CHECKIN_GUIA_ID INTEGER NOT NULL,
                $COL_CHECKIN_HORA TEXT NOT NULL,
                $COL_CHECKIN_ESTADO TEXT DEFAULT 'Confirmado',
                FOREIGN KEY ($COL_CHECKIN_RESERVA_ID) REFERENCES $TABLA_RESERVAS($COL_RESERVA_ID),
                FOREIGN KEY ($COL_CHECKIN_GUIA_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
            )
        """)

        // Crear tabla Pagos
        db?.execSQL("""
            CREATE TABLE $TABLA_PAGOS (
                $COL_PAGO_ID TEXT PRIMARY KEY,
                $COL_PAGO_BOOKING_ID TEXT NOT NULL,
                $COL_PAGO_MONTO REAL NOT NULL,
                $COL_PAGO_METODO TEXT NOT NULL,
                $COL_PAGO_ESTADO TEXT NOT NULL,
                $COL_PAGO_FECHA TEXT NOT NULL,
                $COL_PAGO_TRANSACCION_ID TEXT,
                FOREIGN KEY ($COL_PAGO_BOOKING_ID) REFERENCES $TABLA_RESERVAS($COL_RESERVA_ID)
            )
        """)

        // Crear tabla Notificaciones
        db?.execSQL("""
            CREATE TABLE $TABLA_NOTIFICACIONES (
                $COL_NOTIF_ID TEXT PRIMARY KEY,
                $COL_NOTIF_USUARIO_ID INTEGER NOT NULL,
                $COL_NOTIF_TIPO TEXT NOT NULL,
                $COL_NOTIF_TITULO TEXT NOT NULL,
                $COL_NOTIF_DESCRIPCION TEXT NOT NULL,
                $COL_NOTIF_FECHA_CREACION TEXT NOT NULL,
                $COL_NOTIF_FECHA_LEIDA TEXT,
                $COL_NOTIF_LEIDA INTEGER DEFAULT 0,
                $COL_NOTIF_TOUR_ID TEXT,
                $COL_NOTIF_DESTINO_NOMBRE TEXT,
                $COL_NOTIF_PUNTO_ENCUENTRO TEXT,
                $COL_NOTIF_HORA_TOUR TEXT,
                $COL_NOTIF_DESCUENTO INTEGER,
                $COL_NOTIF_RECOMENDACIONES TEXT,
                $COL_NOTIF_CONDICIONES_CLIMA TEXT,
                FOREIGN KEY ($COL_NOTIF_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
            )
        """)

        // Crear tabla Puntos (HU-007)
        db?.execSQL("""
            CREATE TABLE $TABLA_PUNTOS (
                $COL_PUNTOS_USUARIO_ID INTEGER PRIMARY KEY,
                $COL_PUNTOS_ACUMULADOS INTEGER DEFAULT 0,
                $COL_PUNTOS_FECHA_ACTUALIZACION TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_PUNTOS_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
            )
        """)

        // Crear tabla Logros (HU-007)
        db?.execSQL("""
            CREATE TABLE $TABLA_LOGROS (
                $COL_LOGRO_ID TEXT PRIMARY KEY,
                $COL_LOGRO_USUARIO_ID INTEGER NOT NULL,
                $COL_LOGRO_NOMBRE TEXT NOT NULL,
                $COL_LOGRO_DESCRIPCION TEXT NOT NULL,
                $COL_LOGRO_TIPO TEXT NOT NULL,
                $COL_LOGRO_ICONO TEXT,
                $COL_LOGRO_FECHA_DESBLOQUEO TEXT,
                $COL_LOGRO_DESBLOQUEADO INTEGER DEFAULT 0,
                FOREIGN KEY ($COL_LOGRO_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
            )
        """)

        // Crear tabla Fotos (HU-008)
        db?.execSQL("""
            CREATE TABLE $TABLA_FOTOS (
                $COL_FOTO_ID TEXT PRIMARY KEY,
                $COL_FOTO_TOUR_ID TEXT NOT NULL,
                $COL_FOTO_URL TEXT NOT NULL,
                $COL_FOTO_AUTOR TEXT NOT NULL,
                $COL_FOTO_FECHA_SUBIDA TEXT NOT NULL,
                $COL_FOTO_APROBADA INTEGER DEFAULT 0,
                FOREIGN KEY ($COL_FOTO_TOUR_ID) REFERENCES $TABLA_TOURS($COL_TOUR_ID)
            )
        """)

        // Crear tabla Encuestas (HU-009)
        db?.execSQL("""
            CREATE TABLE $TABLA_ENCUESTAS (
                $COL_ENCUESTA_ID TEXT PRIMARY KEY,
                $COL_ENCUESTA_TOUR_ID TEXT NOT NULL,
                $COL_ENCUESTA_USUARIO_ID TEXT NOT NULL,
                $COL_ENCUESTA_CALIFICACION INTEGER NOT NULL,
                $COL_ENCUESTA_COMENTARIO TEXT,
                $COL_ENCUESTA_FECHA_RESPUESTA TEXT NOT NULL,
                FOREIGN KEY ($COL_ENCUESTA_TOUR_ID) REFERENCES $TABLA_TOURS($COL_TOUR_ID)
            )
        """)
    }

    private fun insertDefaultData(db: SQLiteDatabase?) {
        // Insertar roles
        db?.execSQL("INSERT OR IGNORE INTO $TABLA_ROLES VALUES (1, 'Administrador')")
        db?.execSQL("INSERT OR IGNORE INTO $TABLA_ROLES VALUES (2, 'Turista')")
        
        // Crear usuario administrador por defecto (guía 1)
        // Contraseña: "admin123" (hash SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
        val contrasenaAdmin = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"
        
        // Verificar si ya existe un usuario administrador
        val cursor = db?.rawQuery("SELECT COUNT(*) FROM $TABLA_USUARIOS WHERE $COL_ROL_ID_FK = 1", null)
        val existeAdmin = cursor?.let {
            if (it.moveToFirst()) {
                it.getInt(0) > 0
            } else {
                false
            }
        } ?: false
        cursor?.close()
        
        // Solo crear si no existe
        if (!existeAdmin) {
            // Insertar usuario administrador con ID específico (1)
            // Usar INSERT con el ID especificado para garantizar que sea el guía 1
            db?.execSQL("""
                INSERT INTO $TABLA_USUARIOS 
                ($COL_USUARIO_ID, $COL_NOMBRE_COMPLETO, $COL_CORREO, $COL_CONTRASENA, $COL_ROL_ID_FK) 
                VALUES (1, 'Carlos Guía', 'admin@peruvianservice.com', '$contrasenaAdmin', 1)
            """)
            
            // Resetear el AUTOINCREMENT para que el próximo usuario tenga ID 2
            db?.execSQL("DELETE FROM sqlite_sequence WHERE name='$TABLA_USUARIOS'")
            db?.execSQL("INSERT INTO sqlite_sequence(name, seq) VALUES('$TABLA_USUARIOS', 1)")
        }
        
        // Cargar destinos iniciales (Machu Picchu y Líneas de Nazca) si la tabla está vacía
        val cursorDestinos = db?.rawQuery("SELECT COUNT(*) FROM $TABLA_DESTINOS", null)
        val tieneDestinos = cursorDestinos?.let {
            if (it.moveToFirst()) {
                it.getInt(0) > 0
            } else {
                false
            }
        } ?: false
        cursorDestinos?.close()
        
        if (!tieneDestinos) {
            cargarDestinosIniciales(db)
        }
    }
    
    /**
     * Carga los destinos iniciales (Machu Picchu y Líneas de Nazca) y sus tours asociados.
     * Este método se ejecuta cuando la base de datos se crea por primera vez.
     */
    private fun cargarDestinosIniciales(db: SQLiteDatabase?) {
        // Obtener el ID del primer administrador (debe ser 1 si se acaba de crear)
        val adminId = obtenerIdPrimerAdministradorDesdeDB(db)
        
        // Crear tours para múltiples fechas (próximos 14 días)
        val calendario = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        // Destino 1: Tour Machu Picchu Clásico
        val destino1Id = "dest_001"
        db?.execSQL("""
            INSERT OR IGNORE INTO $TABLA_DESTINOS (
                $COL_DESTINO_ID, $COL_DESTINO_NOMBRE, $COL_DESTINO_UBICACION, $COL_DESTINO_DESCRIPCION,
                $COL_DESTINO_PRECIO, $COL_DESTINO_DURACION, $COL_DESTINO_MAX_PERSONAS,
                $COL_DESTINO_CATEGORIAS, $COL_DESTINO_IMAGEN_URL, $COL_DESTINO_CALIFICACION,
                $COL_DESTINO_NUM_RESENAS, $COL_DESTINO_DISPONIBLE_TODOS_DIAS, $COL_DESTINO_INCLUYE
            ) VALUES (
                '$destino1Id',
                'Tour Machu Picchu Clásico',
                'Cusco, Perú',
                'Descubre la majestuosa ciudadela inca de Machu Picchu, una de las siete maravillas del mundo moderno. Este tour incluye transporte en tren panorámico, guía profesional, entrada a Machu Picchu y almuerzo buffet.',
                450.0,
                12,
                15,
                'Cultura|||Arqueología|||Naturaleza',
                'https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Machu_Picchu%2C_Peru_%282018%29.jpg/1200px-Machu_Picchu%2C_Peru_%282018%29.jpg',
                4.8,
                124,
                1,
                'Transporte en tren panorámico|||Guía profesional en español|||Entrada a Machu Picchu|||Almuerzo buffet|||Seguro de viaje'
            )
        """)
        
        // Crear tours para Machu Picchu (próximos 14 días)
        for (dia in 0..13) {
            calendario.time = java.util.Date()
            calendario.add(java.util.Calendar.DAY_OF_MONTH, dia)
            val fechaTour = dateFormat.format(calendario.time)
            val tourId = "${destino1Id}_$fechaTour"
            val hora = if (dia % 2 == 0) "09:00" else "14:00"
            val estado = if (dia == 0) "Disponible" else "Pendiente"
            
            db?.execSQL("""
                INSERT OR IGNORE INTO $TABLA_TOURS (
                    $COL_TOUR_ID, $COL_TOUR_NOMBRE, $COL_TOUR_FECHA, $COL_TOUR_HORA,
                    $COL_TOUR_PUNTO, $COL_TOUR_CAPACIDAD, $COL_TOUR_CONFIRMADOS,
                    $COL_TOUR_ESTADO, $COL_TOUR_GUIA_ID
                ) VALUES (
                    '$tourId',
                    'Tour Machu Picchu Clásico',
                    '$fechaTour',
                    '$hora',
                    'Cusco, Perú',
                    15,
                    0,
                    '$estado',
                    $adminId
                )
            """)
        }
        
        // Destino 2: Líneas de Nazca Tour Aéreo
        val destino2Id = "dest_002"
        db?.execSQL("""
            INSERT OR IGNORE INTO $TABLA_DESTINOS (
                $COL_DESTINO_ID, $COL_DESTINO_NOMBRE, $COL_DESTINO_UBICACION, $COL_DESTINO_DESCRIPCION,
                $COL_DESTINO_PRECIO, $COL_DESTINO_DURACION, $COL_DESTINO_MAX_PERSONAS,
                $COL_DESTINO_CATEGORIAS, $COL_DESTINO_IMAGEN_URL, $COL_DESTINO_CALIFICACION,
                $COL_DESTINO_NUM_RESENAS, $COL_DESTINO_DISPONIBLE_TODOS_DIAS, $COL_DESTINO_INCLUYE
            ) VALUES (
                '$destino2Id',
                'Líneas de Nazca Tour Aéreo',
                'Ica, Perú',
                'Sobrevuela las misteriosas líneas de Nazca, uno de los mayores enigmas arqueológicos del mundo. Disfruta de un vuelo de 35 minutos sobre estos geoglifos milenarios declarados Patrimonio de la Humanidad por la UNESCO.',
                380.0,
                6,
                8,
                'Aventura|||Arqueología|||Aéreo',
                'https://s3.abcstatics.com/abc/www/multimedia/internacional/2024/01/08/lineas-nazca-unsplash-klJF-U601062084005ad-1200x840@abc.jpg',
                4.6,
                87,
                0,
                'Vuelo de 35 minutos|||Traslado hotel-aeródromo|||Certificado de vuelo|||Seguro de vuelo'
            )
        """)
        
        // Crear tours para Líneas de Nazca (próximos 14 días)
        for (dia in 0..13) {
            calendario.time = java.util.Date()
            calendario.add(java.util.Calendar.DAY_OF_MONTH, dia)
            val fechaTour = dateFormat.format(calendario.time)
            val tourId = "${destino2Id}_$fechaTour"
            val hora = if (dia % 2 == 0) "09:00" else "14:00"
            val estado = if (dia == 0) "Disponible" else "Pendiente"
            
            db?.execSQL("""
                INSERT OR IGNORE INTO $TABLA_TOURS (
                    $COL_TOUR_ID, $COL_TOUR_NOMBRE, $COL_TOUR_FECHA, $COL_TOUR_HORA,
                    $COL_TOUR_PUNTO, $COL_TOUR_CAPACIDAD, $COL_TOUR_CONFIRMADOS,
                    $COL_TOUR_ESTADO, $COL_TOUR_GUIA_ID
                ) VALUES (
                    '$tourId',
                    'Líneas de Nazca Tour Aéreo',
                    '$fechaTour',
                    '$hora',
                    'Ica, Perú',
                    8,
                    0,
                    '$estado',
                    $adminId
                )
            """)
        }
    }
    
    /**
     * Obtiene el ID del primer administrador desde la base de datos.
     * Si no existe, retorna 1 por defecto.
     */
    private fun obtenerIdPrimerAdministradorDesdeDB(db: SQLiteDatabase?): Int {
        return try {
            val cursor = db?.rawQuery(
                "SELECT $COL_USUARIO_ID FROM $TABLA_USUARIOS WHERE $COL_ROL_ID_FK = 1 ORDER BY $COL_USUARIO_ID ASC LIMIT 1",
                null
            )
            val adminId = if (cursor?.moveToFirst() == true) {
                cursor.getInt(0)
            } else {
                1 // Valor por defecto
            }
            cursor?.close()
            adminId
        } catch (e: Exception) {
            1 // Valor por defecto en caso de error
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            // Crear nuevas tablas si no existen
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_DESTINOS (
                    $COL_DESTINO_ID TEXT PRIMARY KEY,
                    $COL_DESTINO_NOMBRE TEXT NOT NULL,
                    $COL_DESTINO_UBICACION TEXT NOT NULL,
                    $COL_DESTINO_DESCRIPCION TEXT,
                    $COL_DESTINO_PRECIO REAL NOT NULL,
                    $COL_DESTINO_DURACION INTEGER NOT NULL,
                    $COL_DESTINO_MAX_PERSONAS INTEGER NOT NULL,
                    $COL_DESTINO_CATEGORIAS TEXT,
                    $COL_DESTINO_IMAGEN_URL TEXT,
                    $COL_DESTINO_CALIFICACION REAL DEFAULT 0,
                    $COL_DESTINO_NUM_RESENAS INTEGER DEFAULT 0,
                    $COL_DESTINO_DISPONIBLE_TODOS_DIAS INTEGER DEFAULT 1,
                    $COL_DESTINO_INCLUYE TEXT
                )
            """)
            
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_TOUR_SLOTS (
                    $COL_SLOT_ID TEXT PRIMARY KEY,
                    $COL_SLOT_FECHA TEXT NOT NULL,
                    $COL_SLOT_CAPACIDAD INTEGER NOT NULL,
                    $COL_SLOT_OCUPADOS INTEGER DEFAULT 0
                )
            """)
            
            // Agregar columna tour_slot_id a reservas si no existe
            try {
                db?.execSQL("ALTER TABLE $TABLA_RESERVAS ADD COLUMN $COL_RESERVA_TOUR_SLOT_ID TEXT")
            } catch (e: Exception) {
                // La columna ya existe, ignorar
            }
        }
        
        if (oldVersion < 4) {
            // Crear tabla de pagos
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_PAGOS (
                    $COL_PAGO_ID TEXT PRIMARY KEY,
                    $COL_PAGO_BOOKING_ID TEXT NOT NULL,
                    $COL_PAGO_MONTO REAL NOT NULL,
                    $COL_PAGO_METODO TEXT NOT NULL,
                    $COL_PAGO_ESTADO TEXT NOT NULL,
                    $COL_PAGO_FECHA TEXT NOT NULL,
                    $COL_PAGO_TRANSACCION_ID TEXT,
                    FOREIGN KEY ($COL_PAGO_BOOKING_ID) REFERENCES $TABLA_RESERVAS($COL_RESERVA_ID)
                )
            """)
        }
        
        if (oldVersion < 5) {
            // Agregar columna guia_id a tours si no existe
            try {
                db?.execSQL("ALTER TABLE $TABLA_TOURS ADD COLUMN $COL_TOUR_GUIA_ID INTEGER DEFAULT 1")
            } catch (e: Exception) {
                // La columna ya existe, ignorar
            }
            
            // Asociar todos los tours existentes al primer administrador encontrado
            // Si no hay admin, usar ID 1 por defecto
            try {
                val cursor = db?.rawQuery(
                    "SELECT $COL_USUARIO_ID FROM $TABLA_USUARIOS WHERE $COL_ROL_ID_FK = 1 ORDER BY $COL_USUARIO_ID ASC LIMIT 1",
                    null
                )
                val adminId = if (cursor?.moveToFirst() == true) {
                    cursor.getInt(0)
                } else {
                    1 // Valor por defecto
                }
                cursor?.close()
                
                db?.execSQL("UPDATE $TABLA_TOURS SET $COL_TOUR_GUIA_ID = $adminId WHERE $COL_TOUR_GUIA_ID IS NULL OR $COL_TOUR_GUIA_ID = 0")
            } catch (e: Exception) {
                // Si hay error, usar ID 1 por defecto
                db?.execSQL("UPDATE $TABLA_TOURS SET $COL_TOUR_GUIA_ID = 1 WHERE $COL_TOUR_GUIA_ID IS NULL OR $COL_TOUR_GUIA_ID = 0")
            }
        }
        
        if (oldVersion < 6) {
            // Crear tabla de check-ins si no existe
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_CHECKINS (
                    $COL_CHECKIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_CHECKIN_RESERVA_ID TEXT NOT NULL,
                    $COL_CHECKIN_GUIA_ID INTEGER NOT NULL,
                    $COL_CHECKIN_HORA TEXT NOT NULL,
                    $COL_CHECKIN_ESTADO TEXT NOT NULL,
                    FOREIGN KEY ($COL_CHECKIN_RESERVA_ID) REFERENCES $TABLA_RESERVAS($COL_RESERVA_ID),
                    FOREIGN KEY ($COL_CHECKIN_GUIA_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
                )
            """)
        }
        
        if (oldVersion < 7) {
            // Crear tabla de notificaciones
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_NOTIFICACIONES (
                    $COL_NOTIF_ID TEXT PRIMARY KEY,
                    $COL_NOTIF_USUARIO_ID INTEGER NOT NULL,
                    $COL_NOTIF_TIPO TEXT NOT NULL,
                    $COL_NOTIF_TITULO TEXT NOT NULL,
                    $COL_NOTIF_DESCRIPCION TEXT NOT NULL,
                    $COL_NOTIF_FECHA_CREACION TEXT NOT NULL,
                    $COL_NOTIF_FECHA_LEIDA TEXT,
                    $COL_NOTIF_LEIDA INTEGER DEFAULT 0,
                    $COL_NOTIF_TOUR_ID TEXT,
                    $COL_NOTIF_DESTINO_NOMBRE TEXT,
                    $COL_NOTIF_PUNTO_ENCUENTRO TEXT,
                    $COL_NOTIF_HORA_TOUR TEXT,
                    $COL_NOTIF_DESCUENTO INTEGER,
                    $COL_NOTIF_RECOMENDACIONES TEXT,
                    $COL_NOTIF_CONDICIONES_CLIMA TEXT,
                    FOREIGN KEY ($COL_NOTIF_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
                )
            """)
        }
        
        if (oldVersion < 8) {
            // Crear tabla de puntos (HU-007)
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_PUNTOS (
                    $COL_PUNTOS_USUARIO_ID INTEGER PRIMARY KEY,
                    $COL_PUNTOS_ACUMULADOS INTEGER DEFAULT 0,
                    $COL_PUNTOS_FECHA_ACTUALIZACION TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY ($COL_PUNTOS_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
                )
            """)
            
            // Crear tabla de logros (HU-007)
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_LOGROS (
                    $COL_LOGRO_ID TEXT PRIMARY KEY,
                    $COL_LOGRO_USUARIO_ID INTEGER NOT NULL,
                    $COL_LOGRO_NOMBRE TEXT NOT NULL,
                    $COL_LOGRO_DESCRIPCION TEXT NOT NULL,
                    $COL_LOGRO_TIPO TEXT NOT NULL,
                    $COL_LOGRO_ICONO TEXT,
                    $COL_LOGRO_FECHA_DESBLOQUEO TEXT,
                    $COL_LOGRO_DESBLOQUEADO INTEGER DEFAULT 0,
                    FOREIGN KEY ($COL_LOGRO_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
                )
            """)
        }
        
        if (oldVersion < 9) {
            // Crear tabla de fotos (HU-008)
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_FOTOS (
                    $COL_FOTO_ID TEXT PRIMARY KEY,
                    $COL_FOTO_TOUR_ID TEXT NOT NULL,
                    $COL_FOTO_URL TEXT NOT NULL,
                    $COL_FOTO_AUTOR TEXT NOT NULL,
                    $COL_FOTO_FECHA_SUBIDA TEXT NOT NULL,
                    $COL_FOTO_APROBADA INTEGER DEFAULT 0,
                    FOREIGN KEY ($COL_FOTO_TOUR_ID) REFERENCES $TABLA_TOURS($COL_TOUR_ID)
                )
            """)
        }
        
        if (oldVersion < 10) {
            // Crear tabla de encuestas (HU-009)
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLA_ENCUESTAS (
                    $COL_ENCUESTA_ID TEXT PRIMARY KEY,
                    $COL_ENCUESTA_TOUR_ID TEXT NOT NULL,
                    $COL_ENCUESTA_USUARIO_ID TEXT NOT NULL,
                    $COL_ENCUESTA_CALIFICACION INTEGER NOT NULL,
                    $COL_ENCUESTA_COMENTARIO TEXT,
                    $COL_ENCUESTA_FECHA_RESPUESTA TEXT NOT NULL,
                    FOREIGN KEY ($COL_ENCUESTA_TOUR_ID) REFERENCES $TABLA_TOURS($COL_TOUR_ID)
                )
            """)
        }
    }

    // ============= MÉTODOS PARA RESERVAS =============

    fun insertarReserva(reserva: Reserva): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_RESERVA_ID, reserva.id)
            put(COL_RESERVA_TOUR_ID, reserva.tourId)
            put(COL_RESERVA_USUARIO_ID, reserva.usuarioId)
            put(COL_RESERVA_NOMBRE, reserva.nombreTurista)
            put(COL_RESERVA_DOC, reserva.documento)
            put(COL_RESERVA_QR, reserva.codigoQR)
            put(COL_RESERVA_ESTADO, reserva.estadoStr)
            put(COL_RESERVA_HORA, reserva.horaRegistro)
            put(COL_RESERVA_PRECIO, reserva.precioTotal)
            put(COL_RESERVA_PAX, reserva.numPersonas)
            if (reserva.tourSlotId.isNotEmpty()) {
                put(COL_RESERVA_TOUR_SLOT_ID, reserva.tourSlotId)
            }
        }
        // Usar CONFLICT_REPLACE para actualizar si la reserva ya existe
        return db.insertWithOnConflict(TABLA_RESERVAS, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerReservaPorQR(codigoQR: String): Reserva? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_RESERVAS,
            null,
            "$COL_RESERVA_QR = ?",
            arrayOf(codigoQR),
            null, null, null
        )

        var reserva: Reserva? = null
        if (cursor.moveToFirst()) {
            reserva = cursorToReserva(cursor)
        }
        cursor.close()
        return reserva
    }

    fun obtenerReservasPorTour(tourId: String): List<Reserva> {
        val db = readableDatabase
        val reservas = mutableListOf<Reserva>()
        val cursor = db.query(
            TABLA_RESERVAS,
            null,
            "$COL_RESERVA_TOUR_ID = ?",
            arrayOf(tourId),
            null, null,
            "$COL_RESERVA_NOMBRE ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                reservas.add(cursorToReserva(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return reservas
    }

    fun obtenerReservasPorUsuario(usuarioId: Int): List<Reserva> {
        val db = readableDatabase
        val reservas = mutableListOf<Reserva>()
        val cursor = db.query(
            TABLA_RESERVAS,
            null,
            "$COL_RESERVA_USUARIO_ID = ?",
            arrayOf(usuarioId.toString()),
            null, null,
            "$COL_RESERVA_HORA DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                reservas.add(cursorToReserva(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return reservas
    }

    fun obtenerReservaPorId(reservaId: String): Reserva? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_RESERVAS,
            null,
            "$COL_RESERVA_ID = ?",
            arrayOf(reservaId),
            null, null, null
        )

        var reserva: Reserva? = null
        if (cursor.moveToFirst()) {
            reserva = cursorToReserva(cursor)
        }
        cursor.close()
        return reserva
    }

    fun marcarReservaUsada(reservaId: String, hora: String): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_RESERVA_ESTADO, EstadoReserva.CONFIRMADO.valor)
            put(COL_RESERVA_HORA, hora)
        }
        val resultado = db.update(
            TABLA_RESERVAS,
            valores,
            "$COL_RESERVA_ID = ?",
            arrayOf(reservaId)
        )
        return resultado > 0
    }

    fun estaReservaUsada(codigoQR: String): Boolean {
        Log.d("DatabaseHelper", "Verificando si reserva está usada: codigoQR=$codigoQR")
        
        // Primero intentar buscar por reservaId (formato nuevo)
        val reservaPorId = obtenerReservaPorId(codigoQR)
        if (reservaPorId != null) {
            Log.d("DatabaseHelper", "Reserva encontrada por reservaId: ${reservaPorId.reservaId}")
            // Verificar si ya tiene un check-in registrado
            val db = readableDatabase
            val cursor = db.query(
                TABLA_CHECKINS,
                null,
                "$COL_CHECKIN_RESERVA_ID = ?",
                arrayOf(reservaPorId.reservaId),
                null, null,
                null
            )
            val tieneCheckIn = cursor.count > 0
            cursor.close()
            Log.d("DatabaseHelper", "Reserva ${reservaPorId.reservaId}: tieneCheckIn=$tieneCheckIn, estaConfirmado=${reservaPorId.estaConfirmado()}")
            return tieneCheckIn || reservaPorId.estaConfirmado()
        }
        
        // Si no se encuentra, buscar por código QR (formato antiguo)
        val reserva = obtenerReservaPorQR(codigoQR)
        if (reserva != null) {
            Log.d("DatabaseHelper", "Reserva encontrada por código QR: ${reserva.reservaId}")
            // Verificar si ya tiene un check-in registrado
            val db = readableDatabase
            val cursor = db.query(
                TABLA_CHECKINS,
                null,
                "$COL_CHECKIN_RESERVA_ID = ?",
                arrayOf(reserva.reservaId),
                null, null,
                null
            )
            val tieneCheckIn = cursor.count > 0
            cursor.close()
            Log.d("DatabaseHelper", "Reserva ${reserva.reservaId}: tieneCheckIn=$tieneCheckIn, estaConfirmado=${reserva.estaConfirmado()}")
            return tieneCheckIn || reserva.estaConfirmado()
        }
        
        Log.w("DatabaseHelper", "Reserva no encontrada: codigoQR=$codigoQR")
        return false
    }

    // ============= MÉTODOS PARA CHECK-INS =============

    fun registrarCheckIn(checkIn: CheckIn): Long {
        val db = writableDatabase
        try {
            // Verificar que la reserva existe antes de insertar
            val reserva = obtenerReservaPorId(checkIn.reservaId)
            if (reserva == null) {
                Log.e("DatabaseHelper", "Error: La reserva ${checkIn.reservaId} no existe")
                return -1L
            }
            
            // Verificar que el guía existe
            val guia = buscarUsuarioPorId(checkIn.guiaId)
            if (guia == null) {
                Log.e("DatabaseHelper", "Error: El guía ${checkIn.guiaId} no existe")
                return -1L
            }
            
            // Verificar si ya existe un check-in para esta reserva
            val cursorExistente = db.query(
                TABLA_CHECKINS,
                arrayOf(COL_CHECKIN_ID),
                "$COL_CHECKIN_RESERVA_ID = ?",
                arrayOf(checkIn.reservaId),
                null, null, null
            )
            val yaExiste = cursorExistente.count > 0
            cursorExistente.close()
            
            if (yaExiste) {
                Log.w("DatabaseHelper", "Ya existe un check-in para la reserva ${checkIn.reservaId}")
                // Retornar éxito (1L) aunque ya exista, para evitar errores
                return 1L
            }
            
            val valores = ContentValues().apply {
                put(COL_CHECKIN_RESERVA_ID, checkIn.reservaId)
                put(COL_CHECKIN_GUIA_ID, checkIn.guiaId)
                put(COL_CHECKIN_HORA, checkIn.horaRegistro)
                put(COL_CHECKIN_ESTADO, checkIn.estado)
            }
            
            Log.d("DatabaseHelper", "Intentando insertar check-in: reservaId=${checkIn.reservaId}, guiaId=${checkIn.guiaId}, hora=${checkIn.horaRegistro}")
            val resultado = db.insert(TABLA_CHECKINS, null, valores)
            
            if (resultado == -1L) {
                Log.e("DatabaseHelper", "Error al insertar check-in: reservaId=${checkIn.reservaId}, guiaId=${checkIn.guiaId}")
                // Intentar obtener más información del error
                try {
                    db.execSQL("PRAGMA foreign_keys = ON")
                } catch (e: Exception) {
                    Log.e("DatabaseHelper", "Error al habilitar foreign keys: ${e.message}")
                }
            } else {
                Log.d("DatabaseHelper", "Check-in registrado exitosamente: ID=$resultado, reservaId=${checkIn.reservaId}")
            }
            
            return resultado
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Excepción al registrar check-in: ${e.message}", e)
            e.printStackTrace()
            return -1L
        }
    }

    /**
     * Obtiene un check-in por el ID de la reserva.
     */
    fun obtenerCheckInPorReserva(reservaId: String): CheckIn? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_CHECKINS,
            null,
            "$COL_CHECKIN_RESERVA_ID = ?",
            arrayOf(reservaId),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val checkIn = CheckIn(
                checkInId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHECKIN_ID)),
                reservaId = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHECKIN_RESERVA_ID)),
                guiaId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHECKIN_GUIA_ID)),
                horaRegistro = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHECKIN_HORA)),
                estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHECKIN_ESTADO))
            )
            cursor.close()
            checkIn
        } else {
            cursor.close()
            null
        }
    }

    // ============= MÉTODOS PARA TOURS =============

    fun obtenerToursDelGuia(guiaId: Int, fecha: String): List<Tour> {
        val db = readableDatabase
        val tours = mutableListOf<Tour>()

        // Filtrar por guía y fecha
        val cursor = db.query(
            TABLA_TOURS,
            null,
            "$COL_TOUR_GUIA_ID = ? AND $COL_TOUR_FECHA = ?",
            arrayOf(guiaId.toString(), fecha),
            null, null,
            "$COL_TOUR_HORA ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                tours.add(cursorToTour(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tours
    }
    
    /**
     * Obtiene todos los tours asignados a un guía, ordenados por fecha ascendente.
     * Filtra solo tours con fechas futuras o del día actual.
     * 
     * @param guiaId ID del guía
     * @return Lista de todos los tours del guía ordenados por fecha (ascendente) y hora
     */
    fun obtenerTodosLosToursDelGuia(guiaId: Int): List<Tour> {
        val db = readableDatabase
        val tours = mutableListOf<Tour>()

        // Obtener fecha de hoy para filtrar tours futuros o del día actual
        val fechaHoy = dateOnlyFormat.format(Date())

        // Filtrar por guía y fechas >= hoy, ordenar por fecha ascendente y luego por hora
        val cursor = db.query(
            TABLA_TOURS,
            null,
            "$COL_TOUR_GUIA_ID = ? AND $COL_TOUR_FECHA >= ?",
            arrayOf(guiaId.toString(), fechaHoy),
            null, null,
            "$COL_TOUR_FECHA ASC, $COL_TOUR_HORA ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                tours.add(cursorToTour(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tours
    }

    fun insertarTour(tour: Tour, guiaId: Int = 1): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_TOUR_ID, tour.tourId)
            put(COL_TOUR_NOMBRE, tour.nombre)
            put(COL_TOUR_FECHA, tour.fecha)
            put(COL_TOUR_HORA, tour.hora)
            put(COL_TOUR_PUNTO, tour.puntoEncuentro)
            put(COL_TOUR_CAPACIDAD, tour.capacidad)
            put(COL_TOUR_CONFIRMADOS, tour.participantesConfirmados)
            put(COL_TOUR_ESTADO, tour.estado)
            put(COL_TOUR_GUIA_ID, guiaId)
        }
        return db.insertWithOnConflict(TABLA_TOURS, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }
    
    fun asociarTourAGuia(tourId: String, guiaId: Int): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_TOUR_GUIA_ID, guiaId)
        }
        val resultado = db.update(
            TABLA_TOURS,
            valores,
            "$COL_TOUR_ID = ?",
            arrayOf(tourId)
        )
        return resultado > 0
    }
    
    fun asociarTodosLosToursAGuia(guiaId: Int): Int {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_TOUR_GUIA_ID, guiaId)
        }
        return db.update(
            TABLA_TOURS,
            valores,
            null,
            null
        )
    }
    
    /**
     * Asocia todos los tours a los usuarios administradores.
     * Si hay múltiples administradores, se asocian al primero encontrado.
     * 
     * @return Número de tours actualizados
     */
    fun asociarTodosLosToursAAdministradores(): Int {
        val adminId = obtenerIdPrimerAdministrador()
        return asociarTodosLosToursAGuia(adminId)
    }

    fun obtenerTourPorId(tourId: String): Tour? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_TOURS,
            null,
            "$COL_TOUR_ID = ?",
            arrayOf(tourId),
            null, null, null
        )

        var tour: Tour? = null
        if (cursor.moveToFirst()) {
            tour = cursorToTour(cursor)
        }
        cursor.close()
        return tour
    }
    
    /**
     * Obtiene todos los tours disponibles.
     * 
     * @return Lista de todos los tours
     */
    fun obtenerTodosLosTours(): List<Tour> {
        val db = readableDatabase
        val tours = mutableListOf<Tour>()
        val cursor = db.query(
            TABLA_TOURS,
            null,
            null,
            null,
            null, null,
            "$COL_TOUR_FECHA ASC, $COL_TOUR_HORA ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                tours.add(cursorToTour(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tours
    }
    
    /**
     * Obtiene todos los tours disponibles para un destino específico.
     * Filtra solo tours con fechas futuras o del día actual.
     * 
     * @param destinoId ID del destino (ej: "dest_001")
     * @return Lista de tours del destino ordenados por fecha (ascendente) y hora
     */
    fun obtenerToursPorDestino(destinoId: String): List<Tour> {
        val db = readableDatabase
        val tours = mutableListOf<Tour>()
        
        // Obtener fecha de hoy para filtrar tours futuros o del día actual
        val fechaHoy = dateOnlyFormat.format(Date())
        
        // Filtrar por destinoId (el tourId empieza con destinoId_) y fechas >= hoy
        // El tourId tiene formato: destinoId_fecha (ej: "dest_001_2025-11-09")
        val cursor = db.rawQuery(
            """
            SELECT * FROM $TABLA_TOURS 
            WHERE $COL_TOUR_ID LIKE ? AND $COL_TOUR_FECHA >= ?
            ORDER BY $COL_TOUR_FECHA ASC, $COL_TOUR_HORA ASC
            """,
            arrayOf("$destinoId%", fechaHoy)
        )
        
        if (cursor.moveToFirst()) {
            do {
                tours.add(cursorToTour(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tours
    }
    
    /**
     * Obtiene las fechas disponibles (con tours) para un destino específico.
     * 
     * @param destinoId ID del destino
     * @return Lista de fechas únicas (formato "yyyy-MM-dd") ordenadas ascendentemente
     */
    fun obtenerFechasDisponiblesPorDestino(destinoId: String): List<String> {
        val db = readableDatabase
        val fechas = mutableSetOf<String>()
        
        // Obtener fecha de hoy para filtrar tours futuros o del día actual
        val fechaHoy = dateOnlyFormat.format(Date())
        
        // Obtener fechas únicas de tours disponibles para este destino
        val cursor = db.rawQuery(
            """
            SELECT DISTINCT $COL_TOUR_FECHA 
            FROM $TABLA_TOURS 
            WHERE $COL_TOUR_ID LIKE ? AND $COL_TOUR_FECHA >= ?
            ORDER BY $COL_TOUR_FECHA ASC
            """,
            arrayOf("$destinoId%", fechaHoy)
        )
        
        if (cursor.moveToFirst()) {
            do {
                fechas.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fechas.sorted()
    }
    
    /**
     * Obtiene las horas disponibles para un destino y fecha específicos.
     * 
     * @param destinoId ID del destino
     * @param fecha Fecha en formato "yyyy-MM-dd"
     * @return Lista de horas únicas ordenadas ascendentemente
     */
    fun obtenerHorasDisponiblesPorDestinoYFecha(destinoId: String, fecha: String): List<String> {
        val db = readableDatabase
        val horas = mutableSetOf<String>()
        
        // Obtener horas únicas de tours disponibles para este destino y fecha
        val cursor = db.rawQuery(
            """
            SELECT DISTINCT $COL_TOUR_HORA 
            FROM $TABLA_TOURS 
            WHERE $COL_TOUR_ID LIKE ? AND $COL_TOUR_FECHA = ?
            ORDER BY $COL_TOUR_HORA ASC
            """,
            arrayOf("$destinoId%", fecha)
        )
        
        if (cursor.moveToFirst()) {
            do {
                horas.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return horas.sorted()
    }


    // ============= CONVERTIDORES CURSOR =============

    private fun cursorToReserva(cursor: Cursor): Reserva {
        val estadoStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_ESTADO))
        val tourSlotIdIndex = cursor.getColumnIndex(COL_RESERVA_TOUR_SLOT_ID)
        val tourSlotId = if (tourSlotIdIndex >= 0 && !cursor.isNull(tourSlotIdIndex)) {
            cursor.getString(tourSlotIdIndex)
        } else {
            ""
        }
        
        val tourId = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_TOUR_ID))
        
        // Extraer destinoId del tourId (puede ser "dest_001" o "dest_001_2025-11-09")
        val destinoId = if (tourId.contains("_") && tourId.count { it == '_' } >= 2) {
            // Formato: dest_001_2025-11-09 -> extraer "dest_001"
            val partes = tourId.split("_")
            "${partes[0]}_${partes[1]}"
        } else {
            // Formato antiguo: solo "dest_001"
            tourId
        }
        
        // Obtener fecha desde tourSlotId si es posible
        // El tourSlotId tiene formato: destinoId_fecha (ej: "dest_001_2025-11-10")
        var fecha = Date()
        var horaInicio = ""
        
        if (tourSlotId.isNotEmpty()) {
            // Intentar obtener fecha desde el tourSlotId (formato: destinoId_fecha)
            val partesSlotId = tourSlotId.split("_")
            if (partesSlotId.size >= 3) {
                // Formato: dest_001_2025-11-10 -> extraer fecha "2025-11-10"
                val fechaStr = partesSlotId.subList(2, partesSlotId.size).joinToString("_")
                try {
                    val fechaParseada = dateOnlyFormat.parse(fechaStr)
                    if (fechaParseada != null) {
                        fecha = fechaParseada
                    }
                } catch (e: Exception) {
                    // Si falla, intentar obtener desde el slot
                    val slot = obtenerTourSlotPorId(tourSlotId)
                    if (slot != null) {
                        fecha = slot.fecha
                    }
                }
            } else {
                // Si no tiene el formato esperado, buscar el slot
                val slot = obtenerTourSlotPorId(tourSlotId)
                if (slot != null) {
                    fecha = slot.fecha
                }
            }
            
            // Intentar obtener la hora desde el tourId que puede tener formato destinoId_fecha
            // o desde los tours disponibles para esta fecha
            if (tourId.contains("_") && tourId.count { it == '_' } >= 2) {
                // El tourId tiene formato: dest_001_2025-11-10
                // Buscar el tour correspondiente para obtener la hora
                val tour = obtenerTourPorId(tourId)
                if (tour != null) {
                    horaInicio = tour.hora
                }
            }
        } else {
            // Si no hay tourSlotId, intentar obtener fecha y hora desde el tourId
            if (tourId.contains("_") && tourId.count { it == '_' } >= 2) {
                val partesTourId = tourId.split("_")
                if (partesTourId.size >= 3) {
                    val fechaStr = partesTourId.subList(2, partesTourId.size).joinToString("_")
                    try {
                        val fechaParseada = dateOnlyFormat.parse(fechaStr)
                        if (fechaParseada != null) {
                            fecha = fechaParseada
                        }
                    } catch (e: Exception) {
                        // Usar fecha actual como fallback
                    }
                }
                
                // Obtener la hora del tour
                val tour = obtenerTourPorId(tourId)
                if (tour != null) {
                    horaInicio = tour.hora
                }
            }
        }
        
        // Obtener destino para incluir en la reserva usando el destinoId extraído
        val destino = obtenerDestinoPorId(destinoId)

        return Reserva(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_ID)),
            reservaId = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_ID)),
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESERVA_USUARIO_ID)).toString(),
            usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESERVA_USUARIO_ID)),
            destinoId = destinoId, // Usar destinoId extraído (sin fecha)
            tourId = tourId, // Usar tourId completo desde la BD (puede tener formato destinoId_fecha)
            tourSlotId = tourSlotId,
            nombreTurista = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_NOMBRE)),
            documento = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_DOC)),
            codigoQR = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_QR)),
            codigoConfirmacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_QR)),
            estado = EstadoReserva.fromString(estadoStr),
            estadoStr = estadoStr,
            horaRegistro = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_HORA)),
            fecha = fecha,
            horaInicio = horaInicio,
            numPersonas = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESERVA_PAX)),
            precioTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RESERVA_PRECIO)),
            destino = destino // Incluir destino para el voucher
        )
    }
    
    private fun cursorToDestino(cursor: Cursor): Destino {
        val categoriasStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINO_CATEGORIAS)) ?: ""
        val incluyeStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINO_INCLUYE)) ?: ""
        
        return Destino(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINO_ID)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINO_NOMBRE)),
            ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINO_UBICACION)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINO_DESCRIPCION)) ?: "",
            precio = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DESTINO_PRECIO)),
            duracionHoras = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DESTINO_DURACION)),
            maxPersonas = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DESTINO_MAX_PERSONAS)),
            categorias = if (categoriasStr.isNotEmpty()) categoriasStr.split(SEPARADOR_LISTA) else emptyList(),
            imagenUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINO_IMAGEN_URL)) ?: "",
            calificacion = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DESTINO_CALIFICACION)),
            numReseñas = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DESTINO_NUM_RESENAS)),
            disponibleTodosDias = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DESTINO_DISPONIBLE_TODOS_DIAS)) == 1,
            incluye = if (incluyeStr.isNotEmpty()) incluyeStr.split(SEPARADOR_LISTA) else emptyList()
        )
    }
    
    private fun cursorToTourSlot(cursor: Cursor): TourSlot {
        val fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_SLOT_FECHA))
        val fecha = try {
            dateOnlyFormat.parse(fechaStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        
        return TourSlot(
            tourSlotId = cursor.getString(cursor.getColumnIndexOrThrow(COL_SLOT_ID)),
            fecha = fecha,
            capacidad = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SLOT_CAPACIDAD)),
            ocupados = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SLOT_OCUPADOS))
        )
    }

    private fun cursorToTour(cursor: Cursor): Tour {
        return Tour(
            tourId = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOUR_ID)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOUR_NOMBRE)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOUR_FECHA)),
            hora = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOUR_HORA)),
            puntoEncuentro = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOUR_PUNTO)),
            capacidad = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOUR_CAPACIDAD)),
            participantesConfirmados = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOUR_CONFIRMADOS)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOUR_ESTADO))
        )
    }

    // ============= MÉTODOS PARA DESTINOS =============

    fun insertarDestino(destino: Destino): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_DESTINO_ID, destino.id)
            put(COL_DESTINO_NOMBRE, destino.nombre)
            put(COL_DESTINO_UBICACION, destino.ubicacion)
            put(COL_DESTINO_DESCRIPCION, destino.descripcion)
            put(COL_DESTINO_PRECIO, destino.precio)
            put(COL_DESTINO_DURACION, destino.duracionHoras)
            put(COL_DESTINO_MAX_PERSONAS, destino.maxPersonas)
            put(COL_DESTINO_CATEGORIAS, destino.categorias.joinToString(SEPARADOR_LISTA))
            put(COL_DESTINO_IMAGEN_URL, destino.imagenUrl)
            put(COL_DESTINO_CALIFICACION, destino.calificacion)
            put(COL_DESTINO_NUM_RESENAS, destino.numReseñas)
            put(COL_DESTINO_DISPONIBLE_TODOS_DIAS, if (destino.disponibleTodosDias) 1 else 0)
            put(COL_DESTINO_INCLUYE, destino.incluye.joinToString(SEPARADOR_LISTA))
        }
        return db.insertWithOnConflict(TABLA_DESTINOS, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerTodosLosDestinos(): List<Destino> {
        val db = readableDatabase
        val destinos = mutableListOf<Destino>()
        val cursor = db.query(
            TABLA_DESTINOS,
            null,
            null,
            null,
            null, null,
            "$COL_DESTINO_NOMBRE ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                destinos.add(cursorToDestino(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return destinos
    }

    fun obtenerDestinoPorId(destinoId: String): Destino? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_DESTINOS,
            null,
            "$COL_DESTINO_ID = ?",
            arrayOf(destinoId),
            null, null, null
        )

        var destino: Destino? = null
        if (cursor.moveToFirst()) {
            destino = cursorToDestino(cursor)
        }
        cursor.close()
        return destino
    }

    fun actualizarDestino(destino: Destino): Int {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_DESTINO_NOMBRE, destino.nombre)
            put(COL_DESTINO_UBICACION, destino.ubicacion)
            put(COL_DESTINO_DESCRIPCION, destino.descripcion)
            put(COL_DESTINO_PRECIO, destino.precio)
            put(COL_DESTINO_DURACION, destino.duracionHoras)
            put(COL_DESTINO_MAX_PERSONAS, destino.maxPersonas)
            put(COL_DESTINO_CATEGORIAS, destino.categorias.joinToString(SEPARADOR_LISTA))
            put(COL_DESTINO_IMAGEN_URL, destino.imagenUrl)
            put(COL_DESTINO_CALIFICACION, destino.calificacion)
            put(COL_DESTINO_NUM_RESENAS, destino.numReseñas)
            put(COL_DESTINO_DISPONIBLE_TODOS_DIAS, if (destino.disponibleTodosDias) 1 else 0)
            put(COL_DESTINO_INCLUYE, destino.incluye.joinToString(SEPARADOR_LISTA))
        }
        return db.update(
            TABLA_DESTINOS,
            valores,
            "$COL_DESTINO_ID = ?",
            arrayOf(destino.id)
        )
    }

    fun eliminarDestino(destinoId: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLA_DESTINOS,
            "$COL_DESTINO_ID = ?",
            arrayOf(destinoId)
        )
    }

    fun contarDestinos(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLA_DESTINOS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    // ============= MÉTODOS PARA TOUR SLOTS =============

    fun insertarTourSlot(slot: TourSlot): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_SLOT_ID, slot.tourSlotId)
            put(COL_SLOT_FECHA, dateOnlyFormat.format(slot.fecha))
            put(COL_SLOT_CAPACIDAD, slot.capacidad)
            put(COL_SLOT_OCUPADOS, slot.ocupados)
        }
        return db.insertWithOnConflict(TABLA_TOUR_SLOTS, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerTourSlotPorId(slotId: String): TourSlot? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_TOUR_SLOTS,
            null,
            "$COL_SLOT_ID = ?",
            arrayOf(slotId),
            null, null, null
        )

        var slot: TourSlot? = null
        if (cursor.moveToFirst()) {
            slot = cursorToTourSlot(cursor)
        }
        cursor.close()
        return slot
    }

    fun actualizarTourSlot(slot: TourSlot): Int {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_SLOT_FECHA, dateOnlyFormat.format(slot.fecha))
            put(COL_SLOT_CAPACIDAD, slot.capacidad)
            put(COL_SLOT_OCUPADOS, slot.ocupados)
        }
        return db.update(
            TABLA_TOUR_SLOTS,
            valores,
            "$COL_SLOT_ID = ?",
            arrayOf(slot.tourSlotId)
        )
    }

    fun obtenerTourSlotsPorFecha(fecha: String): List<TourSlot> {
        val db = readableDatabase
        val slots = mutableListOf<TourSlot>()
        val cursor = db.query(
            TABLA_TOUR_SLOTS,
            null,
            "$COL_SLOT_FECHA = ?",
            arrayOf(fecha),
            null, null,
            "$COL_SLOT_ID ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                slots.add(cursorToTourSlot(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return slots
    }

    // ============= MÉTODOS PARA USUARIOS =============

    fun insertarUsuario(usuario: Usuario): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_NOMBRE_COMPLETO, usuario.nombreCompleto)
            put(COL_CORREO, usuario.correo)
            put(COL_CONTRASENA, usuario.contrasena)
            put(COL_ROL_ID_FK, usuario.rolId)
        }
        return db.insert(TABLA_USUARIOS, null, valores)
    }

    fun buscarUsuarioPorCorreo(correo: String): Usuario? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_USUARIOS,
            null,
            "$COL_CORREO = ?",
            arrayOf(correo),
            null, null, null
        )

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = cursorToUsuario(cursor)
        }
        cursor.close()
        return usuario
    }

    fun buscarUsuarioPorId(usuarioId: Int): Usuario? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_USUARIOS,
            null,
            "$COL_USUARIO_ID = ?",
            arrayOf(usuarioId.toString()),
            null, null, null
        )

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = cursorToUsuario(cursor)
        }
        cursor.close()
        return usuario
    }

    private fun cursorToUsuario(cursor: Cursor): Usuario {
        return Usuario(
            usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USUARIO_ID)),
            nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_COMPLETO)),
            correo = cursor.getString(cursor.getColumnIndexOrThrow(COL_CORREO)),
            contrasena = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTRASENA)),
            rolId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ROL_ID_FK)),
            fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_CREACION))
        )
    }

    fun obtenerRol(rolId: Int): Rol? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_ROLES,
            null,
            "$COL_ROL_ID = ?",
            arrayOf(rolId.toString()),
            null, null, null
        )

        var rol: Rol? = null
        if (cursor.moveToFirst()) {
            rol = Rol(
                rolId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ROL_ID)),
                nombreRol = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_ROL))
            )
        }
        cursor.close()
        return rol
    }
    
    /**
     * Obtiene el primer usuario administrador (rolId = 1).
     * 
     * @return Usuario administrador o null si no existe
     */
    fun obtenerPrimerUsuarioAdministrador(): Usuario? {
        val db = readableDatabase
        // Usar rawQuery para poder usar LIMIT 1
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLA_USUARIOS WHERE $COL_ROL_ID_FK = 1 ORDER BY $COL_USUARIO_ID ASC LIMIT 1",
            null
        )

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = cursorToUsuario(cursor)
        }
        cursor.close()
        return usuario
    }
    
    /**
     * Obtiene el ID del primer usuario administrador.
     * 
     * @return ID del usuario administrador o 1 por defecto si no existe
     */
    fun obtenerIdPrimerAdministrador(): Int {
        val admin = obtenerPrimerUsuarioAdministrador()
        return admin?.usuarioId ?: 1
    }

    /**
     * Obtiene todos los usuarios turistas (rolId = 2).
     * 
     * @return Lista de IDs de usuarios turistas
     */
    fun obtenerTodosLosUsuariosTuristas(): List<Int> {
        val db = readableDatabase
        val usuarios = mutableListOf<Int>()
        val cursor = db.query(
            TABLA_USUARIOS,
            arrayOf(COL_USUARIO_ID),
            "$COL_ROL_ID_FK = ?",
            arrayOf("2"), // Rol turista
            null, null,
            "$COL_USUARIO_ID ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                usuarios.add(cursor.getInt(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return usuarios
    }

    // ============= MÉTODOS PARA PAGOS =============

    fun insertarPago(pago: Pago): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_PAGO_ID, pago.id)
            put(COL_PAGO_BOOKING_ID, pago.bookingId)
            put(COL_PAGO_MONTO, pago.monto)
            put(COL_PAGO_METODO, pago.metodoPago.name)
            put(COL_PAGO_ESTADO, pago.estado.name)
            put(COL_PAGO_FECHA, dateFormat.format(pago.fecha))
            if (pago.transaccionId.isNotEmpty()) {
                put(COL_PAGO_TRANSACCION_ID, pago.transaccionId)
            }
        }
        return db.insertWithOnConflict(TABLA_PAGOS, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerPagoPorId(pagoId: String): Pago? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_PAGOS,
            null,
            "$COL_PAGO_ID = ?",
            arrayOf(pagoId),
            null, null, null
        )

        var pago: Pago? = null
        if (cursor.moveToFirst()) {
            pago = cursorToPago(cursor)
        }
        cursor.close()
        return pago
    }

    fun obtenerPagoPorBooking(bookingId: String): Pago? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_PAGOS,
            null,
            "$COL_PAGO_BOOKING_ID = ?",
            arrayOf(bookingId),
            null, null,
            "$COL_PAGO_FECHA DESC"
        )

        var pago: Pago? = null
        if (cursor.moveToFirst()) {
            pago = cursorToPago(cursor)
        }
        cursor.close()
        return pago
    }

    private fun cursorToPago(cursor: Cursor): Pago {
        val fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAGO_FECHA))
        val fecha = try {
            dateFormat.parse(fechaStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        val metodoStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAGO_METODO))
        val metodoPago = try {
            MetodoPago.valueOf(metodoStr)
        } catch (e: Exception) {
            MetodoPago.TARJETA
        }

        val estadoStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAGO_ESTADO))
        val estadoPago = try {
            EstadoPago.valueOf(estadoStr)
        } catch (e: Exception) {
            EstadoPago.PENDIENTE
        }

        val transaccionIdIndex = cursor.getColumnIndex(COL_PAGO_TRANSACCION_ID)
        val transaccionId = if (transaccionIdIndex >= 0 && !cursor.isNull(transaccionIdIndex)) {
            cursor.getString(transaccionIdIndex)
        } else {
            ""
        }

        return Pago(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAGO_ID)),
            bookingId = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAGO_BOOKING_ID)),
            monto = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PAGO_MONTO)),
            metodoPago = metodoPago,
            estado = estadoPago,
            fecha = fecha,
            transaccionId = transaccionId
        )
    }

    // ============= MÉTODOS PARA NOTIFICACIONES =============

    fun insertarNotificacion(notificacion: Notificacion): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_NOTIF_ID, notificacion.id)
            put(COL_NOTIF_USUARIO_ID, notificacion.usuarioId)
            put(COL_NOTIF_TIPO, notificacion.tipo.valor)
            put(COL_NOTIF_TITULO, notificacion.titulo)
            put(COL_NOTIF_DESCRIPCION, notificacion.descripcion)
            put(COL_NOTIF_FECHA_CREACION, dateFormat.format(notificacion.fechaCreacion))
            put(COL_NOTIF_LEIDA, if (notificacion.leida) 1 else 0)
            
            notificacion.fechaLeida?.let {
                put(COL_NOTIF_FECHA_LEIDA, dateFormat.format(it))
            }
            notificacion.tourId?.let { put(COL_NOTIF_TOUR_ID, it) }
            notificacion.destinoNombre?.let { put(COL_NOTIF_DESTINO_NOMBRE, it) }
            notificacion.puntoEncuentro?.let { put(COL_NOTIF_PUNTO_ENCUENTRO, it) }
            notificacion.horaTour?.let { put(COL_NOTIF_HORA_TOUR, it) }
            notificacion.descuento?.let { put(COL_NOTIF_DESCUENTO, it) }
            notificacion.recomendaciones?.let { put(COL_NOTIF_RECOMENDACIONES, it) }
            notificacion.condicionesClima?.let { put(COL_NOTIF_CONDICIONES_CLIMA, it) }
        }
        return db.insertWithOnConflict(TABLA_NOTIFICACIONES, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerNotificacionesPorUsuario(usuarioId: Int): List<Notificacion> {
        val db = readableDatabase
        val notificaciones = mutableListOf<Notificacion>()
        val cursor = db.query(
            TABLA_NOTIFICACIONES,
            null,
            "$COL_NOTIF_USUARIO_ID = ?",
            arrayOf(usuarioId.toString()),
            null, null,
            "$COL_NOTIF_FECHA_CREACION DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                notificaciones.add(cursorToNotificacion(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notificaciones
    }

    fun obtenerNotificacionesNoLeidasPorUsuario(usuarioId: Int): List<Notificacion> {
        val db = readableDatabase
        val notificaciones = mutableListOf<Notificacion>()
        val cursor = db.query(
            TABLA_NOTIFICACIONES,
            null,
            "$COL_NOTIF_USUARIO_ID = ? AND $COL_NOTIF_LEIDA = ?",
            arrayOf(usuarioId.toString(), "0"),
            null, null,
            "$COL_NOTIF_FECHA_CREACION DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                notificaciones.add(cursorToNotificacion(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notificaciones
    }

    fun marcarNotificacionComoLeida(notificacionId: String): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_NOTIF_LEIDA, 1)
            put(COL_NOTIF_FECHA_LEIDA, dateFormat.format(Date()))
        }
        val resultado = db.update(
            TABLA_NOTIFICACIONES,
            valores,
            "$COL_NOTIF_ID = ?",
            arrayOf(notificacionId)
        )
        return resultado > 0
    }

    fun marcarTodasComoLeidas(usuarioId: Int): Int {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_NOTIF_LEIDA, 1)
            put(COL_NOTIF_FECHA_LEIDA, dateFormat.format(Date()))
        }
        return db.update(
            TABLA_NOTIFICACIONES,
            valores,
            "$COL_NOTIF_USUARIO_ID = ? AND $COL_NOTIF_LEIDA = ?",
            arrayOf(usuarioId.toString(), "0")
        )
    }

    fun obtenerNotificacionPorId(notificacionId: String): Notificacion? {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_NOTIFICACIONES,
            null,
            "$COL_NOTIF_ID = ?",
            arrayOf(notificacionId),
            null, null, null
        )

        var notificacion: Notificacion? = null
        if (cursor.moveToFirst()) {
            notificacion = cursorToNotificacion(cursor)
        }
        cursor.close()
        return notificacion
    }

    fun eliminarNotificacion(notificacionId: String): Boolean {
        val db = writableDatabase
        val resultado = db.delete(
            TABLA_NOTIFICACIONES,
            "$COL_NOTIF_ID = ?",
            arrayOf(notificacionId)
        )
        return resultado > 0
    }

    private fun cursorToNotificacion(cursor: Cursor): Notificacion {
        val fechaCreacionStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIF_FECHA_CREACION))
        val fechaCreacion = try {
            dateFormat.parse(fechaCreacionStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        val fechaLeidaIndex = cursor.getColumnIndex(COL_NOTIF_FECHA_LEIDA)
        val fechaLeida = if (fechaLeidaIndex >= 0 && !cursor.isNull(fechaLeidaIndex)) {
            try {
                dateFormat.parse(cursor.getString(fechaLeidaIndex))
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val tipoStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIF_TIPO))
        val tipo = TipoNotificacion.fromString(tipoStr)

        val tourIdIndex = cursor.getColumnIndex(COL_NOTIF_TOUR_ID)
        val tourId = if (tourIdIndex >= 0 && !cursor.isNull(tourIdIndex)) {
            cursor.getString(tourIdIndex)
        } else {
            null
        }

        val destinoNombreIndex = cursor.getColumnIndex(COL_NOTIF_DESTINO_NOMBRE)
        val destinoNombre = if (destinoNombreIndex >= 0 && !cursor.isNull(destinoNombreIndex)) {
            cursor.getString(destinoNombreIndex)
        } else {
            null
        }

        val puntoEncuentroIndex = cursor.getColumnIndex(COL_NOTIF_PUNTO_ENCUENTRO)
        val puntoEncuentro = if (puntoEncuentroIndex >= 0 && !cursor.isNull(puntoEncuentroIndex)) {
            cursor.getString(puntoEncuentroIndex)
        } else {
            null
        }

        val horaTourIndex = cursor.getColumnIndex(COL_NOTIF_HORA_TOUR)
        val horaTour = if (horaTourIndex >= 0 && !cursor.isNull(horaTourIndex)) {
            cursor.getString(horaTourIndex)
        } else {
            null
        }

        val descuentoIndex = cursor.getColumnIndex(COL_NOTIF_DESCUENTO)
        val descuento = if (descuentoIndex >= 0 && !cursor.isNull(descuentoIndex)) {
            cursor.getInt(descuentoIndex)
        } else {
            null
        }

        val recomendacionesIndex = cursor.getColumnIndex(COL_NOTIF_RECOMENDACIONES)
        val recomendaciones = if (recomendacionesIndex >= 0 && !cursor.isNull(recomendacionesIndex)) {
            cursor.getString(recomendacionesIndex)
        } else {
            null
        }

        val condicionesClimaIndex = cursor.getColumnIndex(COL_NOTIF_CONDICIONES_CLIMA)
        val condicionesClima = if (condicionesClimaIndex >= 0 && !cursor.isNull(condicionesClimaIndex)) {
            cursor.getString(condicionesClimaIndex)
        } else {
            null
        }

        return Notificacion(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIF_ID)),
            usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIF_USUARIO_ID)),
            tipo = tipo,
            titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIF_TITULO)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIF_DESCRIPCION)),
            fechaCreacion = fechaCreacion,
            fechaLeida = fechaLeida,
            leida = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIF_LEIDA)) == 1,
            tourId = tourId,
            destinoNombre = destinoNombre,
            puntoEncuentro = puntoEncuentro,
            horaTour = horaTour,
            descuento = descuento,
            recomendaciones = recomendaciones,
            condicionesClima = condicionesClima
        )
    }

    // ============= MÉTODOS PARA PUNTOS (HU-007) =============

    /**
     * Obtiene los puntos acumulados de un usuario.
     * Si el usuario no tiene registro de puntos, retorna 0.
     */
    fun obtenerPuntos(usuarioId: Int): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_PUNTOS,
            arrayOf(COL_PUNTOS_ACUMULADOS),
            "$COL_PUNTOS_USUARIO_ID = ?",
            arrayOf(usuarioId.toString()),
            null, null, null
        )

        val puntos = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        return puntos
    }

    /**
     * Suma puntos a un usuario.
     * Si el usuario no tiene registro, lo crea.
     */
    fun sumarPuntos(usuarioId: Int, puntos: Int): Boolean {
        val db = writableDatabase
        val puntosActuales = obtenerPuntos(usuarioId)
        val nuevosPuntos = puntosActuales + puntos

        val valores = ContentValues().apply {
            put(COL_PUNTOS_USUARIO_ID, usuarioId)
            put(COL_PUNTOS_ACUMULADOS, nuevosPuntos)
            put(COL_PUNTOS_FECHA_ACTUALIZACION, dateFormat.format(Date()))
        }

        val resultado = db.insertWithOnConflict(
            TABLA_PUNTOS,
            null,
            valores,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        return resultado != -1L
    }

    /**
     * Inicializa los puntos de un usuario si no existen.
     */
    fun inicializarPuntos(usuarioId: Int) {
        val puntosActuales = obtenerPuntos(usuarioId)
        if (puntosActuales == 0) {
            val db = writableDatabase
            val valores = ContentValues().apply {
                put(COL_PUNTOS_USUARIO_ID, usuarioId)
                put(COL_PUNTOS_ACUMULADOS, 0)
                put(COL_PUNTOS_FECHA_ACTUALIZACION, dateFormat.format(Date()))
            }
            db.insertWithOnConflict(
                TABLA_PUNTOS,
                null,
                valores,
                SQLiteDatabase.CONFLICT_IGNORE
            )
        }
    }

    // ============= MÉTODOS PARA LOGROS (HU-007) =============

    /**
     * Obtiene todos los logros de un usuario.
     */
    fun obtenerLogros(usuarioId: Int): List<Logro> {
        val db = readableDatabase
        val logros = mutableListOf<Logro>()
        val cursor = db.query(
            TABLA_LOGROS,
            null,
            "$COL_LOGRO_USUARIO_ID = ?",
            arrayOf(usuarioId.toString()),
            null, null,
            "$COL_LOGRO_FECHA_DESBLOQUEO DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                logros.add(cursorToLogro(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return logros
    }

    /**
     * Obtiene los logros desbloqueados de un usuario.
     */
    fun obtenerLogrosDesbloqueados(usuarioId: Int): List<Logro> {
        val db = readableDatabase
        val logros = mutableListOf<Logro>()
        val cursor = db.query(
            TABLA_LOGROS,
            null,
            "$COL_LOGRO_USUARIO_ID = ? AND $COL_LOGRO_DESBLOQUEADO = ?",
            arrayOf(usuarioId.toString(), "1"),
            null, null,
            "$COL_LOGRO_FECHA_DESBLOQUEO DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                logros.add(cursorToLogro(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return logros
    }

    /**
     * Inserta o actualiza un logro para un usuario.
     */
    fun insertarLogroParaUsuario(usuarioId: Int, logro: Logro): Long {
        val db = writableDatabase
        val fechaDesbloqueoStr = logro.fechaDesbloqueo?.let { dateFormat.format(it) }
        
        val valores = ContentValues().apply {
            put(COL_LOGRO_ID, logro.id)
            put(COL_LOGRO_USUARIO_ID, usuarioId)
            put(COL_LOGRO_NOMBRE, logro.nombre)
            put(COL_LOGRO_DESCRIPCION, logro.descripcion)
            put(COL_LOGRO_TIPO, logro.tipo.valor)
            put(COL_LOGRO_ICONO, logro.icono)
            put(COL_LOGRO_DESBLOQUEADO, if (logro.desbloqueado) 1 else 0)
            fechaDesbloqueoStr?.let { put(COL_LOGRO_FECHA_DESBLOQUEO, it) }
        }

        return db.insertWithOnConflict(
            TABLA_LOGROS,
            null,
            valores,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Verifica si un logro ya existe para un usuario.
     */
    fun existeLogro(usuarioId: Int, logroId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_LOGROS,
            arrayOf(COL_LOGRO_ID),
            "$COL_LOGRO_USUARIO_ID = ? AND $COL_LOGRO_ID = ?",
            arrayOf(usuarioId.toString(), logroId),
            null, null, null
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    /**
     * Obtiene el número de reservas confirmadas de un usuario.
     */
    fun obtenerNumeroReservasConfirmadas(usuarioId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLA_RESERVAS WHERE $COL_RESERVA_USUARIO_ID = ? AND $COL_RESERVA_ESTADO = ?",
            arrayOf(usuarioId.toString(), EstadoReserva.CONFIRMADO.valor)
        )
        val count = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        return count
    }

    /**
     * Convierte un Cursor a un objeto Logro.
     */
    private fun cursorToLogro(cursor: Cursor): Logro {
        val fechaDesbloqueoIndex = cursor.getColumnIndex(COL_LOGRO_FECHA_DESBLOQUEO)
        val fechaDesbloqueo = if (fechaDesbloqueoIndex >= 0 && !cursor.isNull(fechaDesbloqueoIndex)) {
            try {
                dateFormat.parse(cursor.getString(fechaDesbloqueoIndex))
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val tipoStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOGRO_TIPO))
        val tipo = TipoLogro.fromString(tipoStr)

        val iconoIndex = cursor.getColumnIndex(COL_LOGRO_ICONO)
        val icono = if (iconoIndex >= 0 && !cursor.isNull(iconoIndex)) {
            cursor.getString(iconoIndex)
        } else {
            ""
        }

        val desbloqueado = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOGRO_DESBLOQUEADO)) == 1

        return Logro(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOGRO_ID)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOGRO_NOMBRE)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOGRO_DESCRIPCION)),
            icono = icono,
            puntosRequeridos = 0, // No se almacena en BD, se calcula
            tipo = tipo,
            criterio = CriterioLogro(TipoCriterio.TOURS_COMPLETADOS, 0), // Se calcula dinámicamente
            fechaDesbloqueo = fechaDesbloqueo,
            desbloqueado = desbloqueado
        )
    }

    // ============= MÉTODOS PARA FOTOS (HU-008) =============

    /**
     * Inserta una foto en la base de datos.
     */
    fun insertarFoto(foto: Foto): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_FOTO_ID, foto.idFoto)
            put(COL_FOTO_TOUR_ID, foto.idTour)
            put(COL_FOTO_URL, foto.urlImagen)
            put(COL_FOTO_AUTOR, foto.nombreAutor)
            put(COL_FOTO_FECHA_SUBIDA, dateFormat.format(foto.fechaSubida))
            put(COL_FOTO_APROBADA, if (foto.aprobada) 1 else 0)
        }
        return db.insertWithOnConflict(TABLA_FOTOS, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Obtiene todas las fotos aprobadas de un tour.
     */
    fun obtenerFotosPorTour(tourId: String): List<Foto> {
        val db = readableDatabase
        val fotos = mutableListOf<Foto>()
        val cursor = db.query(
            TABLA_FOTOS,
            null,
            "$COL_FOTO_TOUR_ID = ? AND $COL_FOTO_APROBADA = ?",
            arrayOf(tourId, "1"), // Solo fotos aprobadas
            null, null,
            "$COL_FOTO_FECHA_SUBIDA DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                fotos.add(cursorToFoto(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fotos
    }

    /**
     * Convierte un Cursor a un objeto Foto.
     */
    private fun cursorToFoto(cursor: Cursor): Foto {
        val fechaSubidaStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOTO_FECHA_SUBIDA))
        val fechaSubida = try {
            dateFormat.parse(fechaSubidaStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        return Foto(
            idFoto = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOTO_ID)),
            idTour = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOTO_TOUR_ID)),
            urlImagen = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOTO_URL)),
            nombreAutor = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOTO_AUTOR)),
            fechaSubida = fechaSubida,
            aprobada = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FOTO_APROBADA)) == 1
        )
    }

    // ============= MÉTODOS PARA ENCUESTAS (HU-009) =============

    /**
     * Inserta una respuesta de encuesta en la base de datos.
     */
    fun insertarEncuestaRespuesta(encuesta: EncuestaRespuesta): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_ENCUESTA_ID, encuesta.idRespuesta)
            put(COL_ENCUESTA_TOUR_ID, encuesta.idTour)
            put(COL_ENCUESTA_USUARIO_ID, encuesta.usuarioId)
            put(COL_ENCUESTA_CALIFICACION, encuesta.calificacion)
            put(COL_ENCUESTA_COMENTARIO, encuesta.comentario)
            put(COL_ENCUESTA_FECHA_RESPUESTA, dateFormat.format(encuesta.fechaRespuesta))
        }
        return db.insertWithOnConflict(TABLA_ENCUESTAS, null, valores, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Verifica si un usuario ya respondió una encuesta para un tour.
     */
    fun existeEncuestaRespuesta(tourId: String, usuarioId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLA_ENCUESTAS,
            arrayOf(COL_ENCUESTA_ID),
            "$COL_ENCUESTA_TOUR_ID = ? AND $COL_ENCUESTA_USUARIO_ID = ?",
            arrayOf(tourId, usuarioId),
            null, null, null
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    /**
     * Obtiene todas las respuestas de encuestas para un tour.
     */
    fun obtenerEncuestasPorTour(tourId: String): List<EncuestaRespuesta> {
        val db = readableDatabase
        val encuestas = mutableListOf<EncuestaRespuesta>()
        val cursor = db.query(
            TABLA_ENCUESTAS,
            null,
            "$COL_ENCUESTA_TOUR_ID = ?",
            arrayOf(tourId),
            null, null,
            "$COL_ENCUESTA_FECHA_RESPUESTA DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                encuestas.add(cursorToEncuestaRespuesta(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return encuestas
    }

    /**
     * Obtiene la calificación promedio de un tour.
     */
    fun obtenerCalificacionPromedioTour(tourId: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT AVG($COL_ENCUESTA_CALIFICACION) FROM $TABLA_ENCUESTAS WHERE $COL_ENCUESTA_TOUR_ID = ?",
            arrayOf(tourId)
        )
        val promedio = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getDouble(0)
        } else {
            0.0
        }
        cursor.close()
        return promedio
    }

    /**
     * Convierte un Cursor a un objeto EncuestaRespuesta.
     */
    private fun cursorToEncuestaRespuesta(cursor: Cursor): EncuestaRespuesta {
        val fechaRespuestaStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENCUESTA_FECHA_RESPUESTA))
        val fechaRespuesta = try {
            dateFormat.parse(fechaRespuestaStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        return EncuestaRespuesta(
            idRespuesta = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENCUESTA_ID)),
            idTour = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENCUESTA_TOUR_ID)),
            usuarioId = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENCUESTA_USUARIO_ID)),
            calificacion = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ENCUESTA_CALIFICACION)),
            comentario = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENCUESTA_COMENTARIO)) ?: "",
            fechaRespuesta = fechaRespuesta
        )
    }
}