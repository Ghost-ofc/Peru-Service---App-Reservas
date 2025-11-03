package com.grupo4.appreservas.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.grupo4.appreservas.modelos.*
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "PeruvianService.db"
        private const val VERSION_BD = 2

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

        // Tabla CheckIns
        private const val TABLA_CHECKINS = "checkins"
        private const val COL_CHECKIN_ID = "checkin_id"
        private const val COL_CHECKIN_RESERVA_ID = "reserva_id"
        private const val COL_CHECKIN_GUIA_ID = "guia_id"
        private const val COL_CHECKIN_HORA = "hora_registro"
        private const val COL_CHECKIN_ESTADO = "estado"

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
                $COL_TOUR_ESTADO TEXT DEFAULT 'Pendiente'
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
                FOREIGN KEY ($COL_RESERVA_TOUR_ID) REFERENCES $TABLA_TOURS($COL_TOUR_ID),
                FOREIGN KEY ($COL_RESERVA_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COL_USUARIO_ID)
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
    }

    private fun insertDefaultData(db: SQLiteDatabase?) {
        // Insertar roles
        db?.execSQL("INSERT INTO $TABLA_ROLES VALUES (1, 'Administrador')")
        db?.execSQL("INSERT INTO $TABLA_ROLES VALUES (2, 'Turista')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_CHECKINS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_RESERVAS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_TOURS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
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
        }
        return db.insert(TABLA_RESERVAS, null, valores)
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
        val reserva = obtenerReservaPorQR(codigoQR)
        return reserva?.estaConfirmado() ?: false
    }

    // ============= MÉTODOS PARA CHECK-INS =============

    fun registrarCheckIn(checkIn: CheckIn): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_CHECKIN_RESERVA_ID, checkIn.reservaId)
            put(COL_CHECKIN_GUIA_ID, checkIn.guiaId)
            put(COL_CHECKIN_HORA, checkIn.horaRegistro)
            put(COL_CHECKIN_ESTADO, checkIn.estado)
        }
        return db.insert(TABLA_CHECKINS, null, valores)
    }

    // ============= MÉTODOS PARA TOURS =============

    fun obtenerToursDelGuia(guiaId: Int, fecha: String): List<Tour> {
        val db = readableDatabase
        val tours = mutableListOf<Tour>()

        val cursor = db.query(
            TABLA_TOURS,
            null,
            "$COL_TOUR_FECHA = ?",
            arrayOf(fecha),
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

    fun insertarTour(tour: Tour): Long {
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
        }
        return db.insertWithOnConflict(TABLA_TOURS, null, valores, SQLiteDatabase.CONFLICT_IGNORE)
    }


    // ============= CONVERTIDORES CURSOR =============

    private fun cursorToReserva(cursor: Cursor): Reserva {
        val estadoStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_ESTADO))

        return Reserva(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_ID)),
            reservaId = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_ID)),
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESERVA_USUARIO_ID)).toString(),
            usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESERVA_USUARIO_ID)),
            tourId = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_TOUR_ID)),
            nombreTurista = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_NOMBRE)),
            documento = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_DOC)),
            codigoQR = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_QR)),
            codigoConfirmacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_QR)),
            estado = EstadoReserva.fromString(estadoStr),
            estadoStr = estadoStr,
            horaRegistro = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_HORA)),
            fecha = Date(), // Obtener del tour si es necesario
            horaInicio = "", // Obtener del tour si es necesario
            numPersonas = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESERVA_PAX)),
            precioTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RESERVA_PRECIO))
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
            usuario = Usuario(
                usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USUARIO_ID)),
                nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_COMPLETO)),
                correo = cursor.getString(cursor.getColumnIndexOrThrow(COL_CORREO)),
                contrasena = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTRASENA)),
                rolId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ROL_ID_FK)),
                fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_CREACION))
            )
        }
        cursor.close()
        return usuario
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
}