package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.CheckIn
import com.grupo4.appreservas.modelos.Clima
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.Logro
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.modelos.Pago
import com.grupo4.appreservas.modelos.PuntosUsuario
import com.grupo4.appreservas.modelos.Reserva
import com.grupo4.appreservas.modelos.Rol
import com.grupo4.appreservas.modelos.TipoLogro
import com.grupo4.appreservas.modelos.TipoNotificacion
import com.grupo4.appreservas.modelos.Tour
import com.grupo4.appreservas.modelos.TourSlot
import com.grupo4.appreservas.modelos.Usuario
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Único repository del sistema según el diagrama UML.
 * Maneja todas las operaciones de datos relacionadas con destinos turísticos y reservas.
 */
class PeruvianServiceRepository private constructor(private val dbHelper: DatabaseHelper) {

    companion object {
        @Volatile
        private var instance: PeruvianServiceRepository? = null

        fun getInstance(context: Context): PeruvianServiceRepository {
            return instance ?: synchronized(this) {
                val dbHelper = DatabaseHelper(context)
                val repo = PeruvianServiceRepository(dbHelper)
                instance ?: repo.also { instance = it }
            }
        }
    }

    /**
     * Obtiene todos los destinos disponibles.
     * Equivalente a buscarDestinos() del diagrama UML.
     */
    fun buscarDestinos(): List<Destino> {
        return dbHelper.obtenerTodosLosDestinos()
    }

    /**
     * Busca un destino por su ID.
     * Equivalente a buscarReservaPorId() pero para destinos.
     */
    fun buscarDestinoPorId(destinoId: String): Destino? {
        return dbHelper.obtenerDestinoPorId(destinoId)
    }

    /**
     * Guarda un destino en la base de datos.
     * Equivalente a guardarReserva() pero para destinos.
     */
    fun guardarDestino(destino: Destino) {
        dbHelper.insertarDestino(destino)
    }

    // ============= MÉTODOS PARA RESERVAS (según diagrama UML) =============

    /**
     * Consulta los cupos disponibles para un tour slot.
     * Equivalente a consultarCuposDisponibles(idTourSlot): int del diagrama UML.
     */
    fun consultarCuposDisponibles(idTourSlot: String): Int {
        val slot = dbHelper.obtenerTourSlotPorId(idTourSlot)
        return if (slot != null) {
            slot.cuposDisponibles()
        } else {
            // Si no existe el slot, crear uno basado en el tour
            // El idTourSlot tiene formato: destinoId_fecha
            val partes = idTourSlot.split("_")
            if (partes.size >= 3) {
                val destinoId = "${partes[0]}_${partes[1]}"
                val fechaStr = partes.subList(2, partes.size).joinToString("_")
                val destino = dbHelper.obtenerDestinoPorId(destinoId)
                destino?.maxPersonas ?: 0
            } else {
                0
            }
        }
    }

    /**
     * Bloquea asientos para un tour slot.
     * Equivalente a bloquearAsientos(idTourSlot, cantidadPasajeros): boolean del diagrama UML.
     */
    fun bloquearAsientos(idTourSlot: String, cantidadPasajeros: Int): Boolean {
        // Obtener o crear el slot
        var slot = dbHelper.obtenerTourSlotPorId(idTourSlot)
        
        if (slot == null) {
            // Crear el slot si no existe
            val partes = idTourSlot.split("_")
            if (partes.size >= 3) {
                val destinoId = "${partes[0]}_${partes[1]}"
                val fechaStr = partes.subList(2, partes.size).joinToString("_")
                val destino = dbHelper.obtenerDestinoPorId(destinoId)
                
                if (destino != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val fecha = try {
                        dateFormat.parse(fechaStr) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }
                    
                    slot = TourSlot(
                        tourSlotId = idTourSlot,
                        fecha = fecha,
                        capacidad = destino.maxPersonas,
                        ocupados = 0
                    )
                    dbHelper.insertarTourSlot(slot)
                } else {
                    return false
                }
            } else {
                return false
            }
        }

        // Verificar disponibilidad
        if (!slot.tieneCapacidad(cantidadPasajeros)) {
            return false
        }

        // Bloquear los asientos (incrementar ocupados)
        val slotActualizado = slot.copy(ocupados = slot.ocupados + cantidadPasajeros)
        dbHelper.actualizarTourSlot(slotActualizado)
        
        return true
    }

    /**
     * Crea una reserva.
     * Equivalente a crearReserva(idUsuario, idTourSlot, cantidadPasajeros): Reserva del diagrama UML.
     */
    fun crearReserva(idUsuario: Int, idTourSlot: String, cantidadPasajeros: Int): Reserva {
        // Obtener información del destino desde el tourSlotId
        val partes = idTourSlot.split("_")
        val destinoId = if (partes.size >= 3) {
            "${partes[0]}_${partes[1]}"
        } else {
            ""
        }
        
        val destino = dbHelper.obtenerDestinoPorId(destinoId)
        val precioTotal = (destino?.precio ?: 0.0) * cantidadPasajeros
        
        // Obtener fecha y hora desde el tour
        val fechaStr = if (partes.size >= 3) {
            partes.subList(2, partes.size).joinToString("_")
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = try {
            dateFormat.parse(fechaStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        
        // Obtener hora del tour
        val tours = dbHelper.obtenerToursPorDestino(destinoId)
        val tour = tours.find { it.fecha == fechaStr }
        val horaInicio = tour?.hora ?: "09:00"
        
        // Generar ID de reserva
        val reservaId = "RES_${System.currentTimeMillis()}"
        
        // Generar código QR/confirmación
        val codigoQR = "QR_${System.currentTimeMillis()}"
        
        // Crear la reserva
        val reserva = Reserva(
            id = reservaId,
            reservaId = reservaId,
            userId = idUsuario.toString(),
            usuarioId = idUsuario,
            destinoId = destinoId,
            tourId = tour?.tourId ?: destinoId,
            tourSlotId = idTourSlot,
            nombreTurista = "", // Se llenará en la UI
            documento = "", // Se llenará en la UI
            destino = destino,
            fecha = fecha,
            horaInicio = horaInicio,
            numPersonas = cantidadPasajeros,
            precioTotal = precioTotal,
            estado = com.grupo4.appreservas.modelos.EstadoReserva.PENDIENTE,
            codigoConfirmacion = codigoQR,
            codigoQR = codigoQR
        )
        
        // Guardar en la base de datos
        dbHelper.insertarReserva(reserva)
        
        return reserva
    }

    /**
     * Obtiene las fechas disponibles para un destino.
     */
    fun obtenerFechasDisponibles(destinoId: String): List<String> {
        return dbHelper.obtenerFechasDisponiblesPorDestino(destinoId)
    }

    /**
     * Obtiene las horas disponibles para un destino y fecha.
     */
    fun obtenerHorasDisponibles(destinoId: String, fecha: String): List<String> {
        return dbHelper.obtenerHorasDisponiblesPorDestinoYFecha(destinoId, fecha)
    }

    // ============= MÉTODOS PARA RESERVAS Y PAGOS =============

    /**
     * Busca una reserva por su ID.
     * Equivalente a find(id) del diagrama UML (BookingRepository).
     */
    fun buscarReservaPorId(bookingId: String): Reserva? {
        return dbHelper.obtenerReservaPorId(bookingId)
    }

    /**
     * Guarda una reserva.
     * Equivalente a save(booking) del diagrama UML (BookingRepository).
     */
    fun guardarReserva(reserva: Reserva) {
        dbHelper.insertarReserva(reserva)
    }

    /**
     * Confirma el pago de una reserva.
     * Equivalente a confirmarPago(bookingId, payment): Booking del diagrama UML (BookingService).
     */
    fun confirmarPago(bookingId: String, pago: Pago): Reserva? {
        val reserva = buscarReservaPorId(bookingId) ?: return null

        // Actualizar el estado de la reserva a confirmado
        val reservaConfirmada = reserva.copy(
            estado = com.grupo4.appreservas.modelos.EstadoReserva.CONFIRMADO,
            metodoPago = pago.metodoPago.name
        )

        dbHelper.insertarReserva(reservaConfirmada)
        
        // Sumar puntos cuando se confirma el pago (reserva completada según HU-007)
        if (reserva.estado != com.grupo4.appreservas.modelos.EstadoReserva.CONFIRMADO) {
            sumarPuntosPorReserva(reserva.usuarioId, bookingId)
        }
        
        return reservaConfirmada
    }

    /**
     * Guarda un pago.
     * Equivalente a save(payment) del diagrama UML (PaymentRepository).
     */
    fun guardarPago(pago: Pago) {
        dbHelper.insertarPago(pago)
    }

    /**
     * Busca un pago por booking ID.
     * Equivalente a findByBooking(id) del diagrama UML (PaymentRepository).
     */
    fun buscarPagoPorBooking(bookingId: String): Pago? {
        return dbHelper.obtenerPagoPorBooking(bookingId)
    }

    // ============= MÉTODOS PARA AUTENTICACIÓN (según diagrama UML) =============

    /**
     * Crea un nuevo usuario.
     * Equivalente a crearUsuario(datos, rol): Usuario del diagrama UML.
     * El rol se asigna aquí según el diagrama.
     */
    fun crearUsuario(nombre: String, correo: String, contrasena: String, rolId: Int): Usuario {
        // Hashear la contraseña con SHA-256
        val contrasenaHash = hashSHA256(contrasena)

        // Crear el usuario
        val usuario = Usuario(
            nombreCompleto = nombre,
            correo = correo,
            contrasena = contrasenaHash,
            rolId = rolId,
            fechaCreacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        // Guardar en la base de datos
        val usuarioId = dbHelper.insertarUsuario(usuario)
        
        // Retornar el usuario con el ID asignado
        return usuario.copy(usuarioId = usuarioId.toInt())
    }

    /**
     * Valida las credenciales de un usuario.
     * Equivalente a validarCredenciales(correo, contrasena): Usuario del diagrama UML.
     */
    fun validarCredenciales(correo: String, contrasena: String): Usuario? {
        val usuario = dbHelper.buscarUsuarioPorCorreo(correo) ?: return null

        // Hashear la contraseña ingresada y comparar
        val contrasenaHash = hashSHA256(contrasena)
        
        if (usuario.contrasena == contrasenaHash) {
            return usuario
        }

        return null
    }

    /**
     * Obtiene el rol de un usuario.
     * Equivalente a obtenerRol(usuarioId): Rol del diagrama UML.
     */
    fun obtenerRol(usuarioId: Int): Rol? {
        val usuario = dbHelper.buscarUsuarioPorId(usuarioId) ?: return null
        return dbHelper.obtenerRol(usuario.rolId)
    }

    /**
     * Busca un usuario por su ID.
     */
    fun buscarUsuarioPorId(usuarioId: Int): com.grupo4.appreservas.modelos.Usuario? {
        return dbHelper.buscarUsuarioPorId(usuarioId)
    }

    /**
     * Hashea una contraseña usando SHA-256.
     */
    private fun hashSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    // ============= MÉTODOS PARA TOURS Y CHECK-IN (según diagrama UML) =============

    /**
     * Obtiene los tours del día para un guía.
     * Equivalente a obtenerToursDelDia(guiaId): List<Tour> del diagrama UML.
     */
    fun obtenerToursDelDia(guiaId: Int): List<Tour> {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dbHelper.obtenerToursDelGuia(guiaId, fechaHoy)
    }

    /**
     * Obtiene un tour por su ID.
     */
    fun obtenerTourPorId(tourId: String): Tour? {
        return dbHelper.obtenerTourPorId(tourId)
    }

    /**
     * Valida un código QR de reserva.
     * Equivalente a validarQR(codigoReserva): Boolean del diagrama UML.
     * Nota: Cambié el nombre del argumento según el diagrama.
     */
    fun validarQR(codigoReserva: String): Boolean {
        // Buscar la reserva por código QR o por ID
        val reservaPorQR = dbHelper.obtenerReservaPorQR(codigoReserva)
        val reservaPorId = dbHelper.obtenerReservaPorId(codigoReserva)
        
        return reservaPorQR != null || reservaPorId != null
    }

    /**
     * Obtiene el ID de reserva a partir del código QR.
     * Equivalente a obtenerReservaId(codigoReserva): String del diagrama UML.
     */
    fun obtenerReservaId(codigoReserva: String): String {
        // Primero intentar buscar por código QR
        val reservaPorQR = dbHelper.obtenerReservaPorQR(codigoReserva)
        if (reservaPorQR != null) {
            return reservaPorQR.reservaId
        }
        
        // Si no se encuentra, intentar buscar por ID directamente
        val reservaPorId = dbHelper.obtenerReservaPorId(codigoReserva)
        if (reservaPorId != null) {
            return reservaPorId.reservaId
        }
        
        return ""
    }

    /**
     * Verifica si una reserva pertenece a un tour específico.
     * Equivalente a perteneceATour(reservaId, tourId): Boolean del diagrama UML.
     */
    fun perteneceATour(reservaId: String, tourId: String): Boolean {
        val reserva = dbHelper.obtenerReservaPorId(reservaId) ?: return false
        return reserva.tourId == tourId
    }

    /**
     * Verifica si un código QR ya fue usado.
     * Equivalente a estaUsado(codigoReserva): Boolean del diagrama UML.
     * Nota: Cambié el nombre del argumento según el diagrama.
     */
    fun estaUsado(codigoReserva: String): Boolean {
        return dbHelper.estaReservaUsada(codigoReserva)
    }

    /**
     * Marca un código QR como usado.
     * Equivalente a marcarUsado(codigoReserva): void del diagrama UML.
     * Nota: Cambié el nombre del argumento según el diagrama.
     * 
     * En realidad, el código se marca como usado cuando se registra el check-in,
     * pero este método puede ser útil para marcar explícitamente.
     */
    fun marcarUsado(codigoReserva: String) {
        // El código se marca como usado automáticamente al registrar el check-in
        // Este método existe para cumplir con el diagrama UML
        // No necesita hacer nada adicional porque el check-in ya marca la reserva como usada
    }

    /**
     * Registra un check-in para una reserva.
     * Equivalente a registrarCheckIn(reservaId, guiaId, hora): Boolean del diagrama UML.
     */
    fun registrarCheckIn(reservaId: String, guiaId: Int, hora: String): Boolean {
        val checkIn = CheckIn(
            reservaId = reservaId,
            guiaId = guiaId,
            horaRegistro = hora,
            estado = "Confirmado"
        )
        
        val resultado = dbHelper.registrarCheckIn(checkIn)
        return resultado > 0
    }

    // ============= MÉTODOS PARA NOTIFICACIONES (según diagrama UML) =============

    /**
     * Obtiene los recordatorios/notificaciones de un usuario.
     * Equivalente a obtenerRecordatorios(usuarioId): List<Notification> del diagrama UML.
     */
    fun obtenerRecordatorios(usuarioId: Int): List<Notificacion> {
        return dbHelper.obtenerNotificacionesPorUsuario(usuarioId)
    }

    /**
     * Obtiene las notificaciones no leídas de un usuario.
     */
    fun obtenerNotificacionesNoLeidasPorUsuario(usuarioId: Int): List<Notificacion> {
        return dbHelper.obtenerNotificacionesNoLeidasPorUsuario(usuarioId)
    }

    /**
     * Obtiene tours con descuento (baja ocupación).
     * Equivalente a obtenerToursConDescuento(): List<Tour> del diagrama UML.
     */
    fun obtenerToursConDescuento(): List<Tour> {
        // Obtener todos los tours del día
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todosLosTours = dbHelper.obtenerTodosLosTours()
        
        // Filtrar tours con baja ocupación (menos del 50% de capacidad)
        return todosLosTours.filter { tour ->
            val porcentajeOcupacion = if (tour.capacidad > 0) {
                (tour.participantesConfirmados.toDouble() / tour.capacidad.toDouble()) * 100
            } else {
                0.0
            }
            porcentajeOcupacion < 50.0 && tour.fecha >= fechaHoy // Solo tours futuros
        }
    }

    /**
     * Obtiene condiciones climáticas y detecta cambios.
     * Equivalente a obtenerCondicionesYDetectarCambio(actualUbicacion): Boolean del diagrama UML.
     */
    fun obtenerCondicionesYDetectarCambio(actualUbicacion: String?): Boolean {
        // Simulación de detección de cambio climático
        // En una implementación real, esto consultaría un servicio de clima
        val ubicacion = actualUbicacion ?: "Cusco"
        
        // Obtener condiciones actuales (simulado)
        val climaActual = obtenerClimaActual(ubicacion)
        
        // Verificar si hay cambios significativos (lluvia, tormenta, etc.)
        val hayCambio = climaActual.condicion.contains("Lluvia", ignoreCase = true) ||
                       climaActual.condicion.contains("Tormenta", ignoreCase = true) ||
                       climaActual.condicion.contains("Nieve", ignoreCase = true)
        
        return hayCambio
    }

    /**
     * Obtiene el clima actual de una ubicación (simulado).
     */
    private fun obtenerClimaActual(ubicacion: String): Clima {
        // Simulación: en producción se consultaría un API de clima
        val condiciones = listOf("Soleado", "Nublado", "Lluvia ligera", "Lluvia intensa", "Tormenta")
        val random = Random()
        val condicionAleatoria = condiciones[random.nextInt(condiciones.size)]
        
        return Clima(
            ubicacion = ubicacion,
            temperatura = 15.0 + random.nextDouble() * 10.0, // Entre 15.0 y 25.0
            condicion = condicionAleatoria,
            humedad = 40 + random.nextInt(51) // Entre 40 y 90
        )
    }

    /**
     * Crea una notificación de recordatorio de horario.
     */
    fun crearNotificacionRecordatorio(usuarioId: Int, tourId: String, nombreTour: String, horaTour: String, puntoEncuentro: String) {
        val notificacion = Notificacion(
            id = "REC_${System.currentTimeMillis()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.RECORDATORIO,
            titulo = "Recordatorio: Tour próximo a iniciar",
            descripcion = "Tu tour '$nombreTour' está próximo a iniciar. Hora: $horaTour",
            fechaCreacion = Date(),
            tourId = tourId,
            destinoNombre = nombreTour,
            horaTour = horaTour,
            puntoEncuentro = puntoEncuentro
        )
        dbHelper.insertarNotificacion(notificacion)
    }

    /**
     * Crea una notificación de alerta climática.
     */
    fun crearNotificacionAlertaClimatica(usuarioId: Int, ubicacion: String, condiciones: String, recomendaciones: String) {
        val notificacion = Notificacion(
            id = "CLIMA_${System.currentTimeMillis()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.ALERTA_CLIMATICA,
            titulo = "Alerta Climática: $ubicacion",
            descripcion = "Se ha detectado un cambio en las condiciones climáticas: $condiciones",
            fechaCreacion = Date(),
            condicionesClima = condiciones,
            recomendaciones = recomendaciones
        )
        dbHelper.insertarNotificacion(notificacion)
    }

    /**
     * Crea una notificación de oferta de último minuto.
     */
    fun crearNotificacionOferta(usuarioId: Int, tourId: String, nombreTour: String, descuento: Int) {
        val notificacion = Notificacion(
            id = "OFERTA_${System.currentTimeMillis()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.OFERTA_ULTIMO_MINUTO,
            titulo = "¡Oferta de Último Minuto!",
            descripcion = "Aprovecha un $descuento% de descuento en el tour '$nombreTour'",
            fechaCreacion = Date(),
            tourId = tourId,
            destinoNombre = nombreTour,
            descuento = descuento
        )
        dbHelper.insertarNotificacion(notificacion)
    }

    /**
     * Marca una notificación como leída.
     */
    fun marcarNotificacionComoLeida(notificacionId: String): Boolean {
        return dbHelper.marcarNotificacionComoLeida(notificacionId)
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas.
     */
    fun marcarTodasLasNotificacionesComoLeidas(usuarioId: Int): Int {
        return dbHelper.marcarTodasComoLeidas(usuarioId)
    }

    /**
     * Obtiene las reservas de un usuario.
     */
    fun obtenerReservasPorUsuario(usuarioId: Int): List<Reserva> {
        return dbHelper.obtenerReservasPorUsuario(usuarioId)
    }

    /**
     * Obtiene todos los usuarios turistas.
     */
    fun obtenerTodosLosUsuariosTuristas(): List<Usuario> {
        val idsTuristas = dbHelper.obtenerTodosLosUsuariosTuristas()
        return idsTuristas.mapNotNull { id ->
            dbHelper.buscarUsuarioPorId(id)
        }
    }

    // ============= MÉTODOS PARA RECOMPENSAS Y LOGROS (según diagrama UML) =============

    /**
     * Obtiene los puntos acumulados de un usuario.
     * Equivalente a obtenerPuntos(usuarioId): Int del diagrama UML.
     */
    fun obtenerPuntos(usuarioId: Int): Int {
        // Inicializar puntos si no existen
        dbHelper.inicializarPuntos(usuarioId)
        return dbHelper.obtenerPuntos(usuarioId)
    }

    /**
     * Obtiene los logros de un usuario.
     * Equivalente a obtenerLogros(usuarioId): List<Logro> del diagrama UML.
     */
    fun obtenerLogros(usuarioId: Int): List<Logro> {
        val logros = dbHelper.obtenerLogros(usuarioId)
        
        // Si el usuario no tiene logros, inicializar los logros base
        if (logros.isEmpty()) {
            inicializarLogrosBase(usuarioId)
            return dbHelper.obtenerLogros(usuarioId)
        }
        
        return logros
    }

    /**
     * Obtiene las reservas confirmadas de un usuario.
     * Equivalente a obtenerReservasConfirmadas(usuarioId): List<Reserva> del diagrama UML.
     */
    fun obtenerReservasConfirmadas(usuarioId: Int): List<Reserva> {
        return dbHelper.obtenerReservasPorUsuario(usuarioId)
            .filter { it.estaConfirmado() }
    }

    /**
     * Suma puntos al usuario cuando completa una reserva.
     */
    fun sumarPuntosPorReserva(usuarioId: Int, reservaId: String) {
        // Verificar que la reserva existe y está confirmada
        val reserva = dbHelper.obtenerReservaPorId(reservaId)
        if (reserva != null && reserva.estaConfirmado()) {
            // Verificar que no se hayan sumado puntos ya para esta reserva
            // (esto se puede hacer con una tabla de puntos_por_reserva o verificando fecha)
            val puntos = PuntosUsuario.PUNTOS_POR_RESERVA
            dbHelper.sumarPuntos(usuarioId, puntos)
            
            // Verificar y desbloquear logros después de sumar puntos
            verificarYDesbloquearLogros(usuarioId)
        }
    }

    /**
     * Verifica y desbloquea logros según los criterios del usuario.
     */
    private fun verificarYDesbloquearLogros(usuarioId: Int) {
        val reservasConfirmadas = obtenerReservasConfirmadas(usuarioId)
        val puntosActuales = obtenerPuntos(usuarioId)
        
        // Verificar logro "Primer Viaje"
        if (reservasConfirmadas.size == 1) {
            desbloquearLogro(usuarioId, "PRIMER_VIAJE")
        }
        
        // Verificar logro "Viajero Frecuente" (5+ reservas)
        if (reservasConfirmadas.size >= 5) {
            desbloquearLogro(usuarioId, "VIAJERO_FRECUENTE")
        }
        
        // Verificar logro por tours completados (10+ reservas)
        if (reservasConfirmadas.size >= 10) {
            desbloquearLogro(usuarioId, "TOURS_10")
        }
        
        // Verificar logros por puntos acumulados
        when {
            puntosActuales >= 1000 -> desbloquearLogro(usuarioId, "PUNTOS_1000")
            puntosActuales >= 500 -> desbloquearLogro(usuarioId, "PUNTOS_500")
        }
    }

    /**
     * Desbloquea un logro para un usuario.
     */
    fun desbloquearLogro(usuarioId: Int, tipoLogro: String) {
        // Verificar si el logro ya existe y está desbloqueado
        val logroId = "${usuarioId}_${tipoLogro}"
        val logroExistente = dbHelper.obtenerLogros(usuarioId).find { it.id == logroId }
        if (logroExistente != null && logroExistente.desbloqueado) {
            return // Ya está desbloqueado, no hacer nada
        }
        
        // Crear el logro según el tipo
        val logro = when (tipoLogro) {
            "PRIMER_VIAJE" -> Logro(
                id = logroId,
                nombre = "Primer Viaje",
                descripcion = "Completa tu primera reserva",
                icono = "ic_trophy",
                tipo = TipoLogro.PRIMER_VIAJE,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.PRIMERA_RESERVA,
                    1
                ),
                fechaDesbloqueo = Date(),
                desbloqueado = true
            )
            "VIAJERO_FRECUENTE" -> Logro(
                id = logroId,
                nombre = "Viajero Frecuente",
                descripcion = "Completa 5 reservas",
                icono = "ic_trophy",
                tipo = TipoLogro.VIAJERO_FRECUENTE,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.TOURS_COMPLETADOS,
                    5
                ),
                fechaDesbloqueo = Date(),
                desbloqueado = true
            )
            "TOURS_10" -> Logro(
                id = logroId,
                nombre = "Explorador Experto",
                descripcion = "Completa 10 reservas",
                icono = "ic_trophy",
                tipo = TipoLogro.TOURS_COMPLETADOS,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.TOURS_COMPLETADOS,
                    10
                ),
                fechaDesbloqueo = Date(),
                desbloqueado = true
            )
            "PUNTOS_500" -> {
                val puntos = obtenerPuntos(usuarioId)
                if (puntos >= 500) {
                    Logro(
                        id = logroId,
                        nombre = "Acumulador de Puntos",
                        descripcion = "Acumula 500 puntos",
                        icono = "ic_trophy",
                        tipo = TipoLogro.PUNTOS_ACUMULADOS,
                        criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                            com.grupo4.appreservas.modelos.TipoCriterio.PUNTOS_ACUMULADOS,
                            500
                        ),
                        fechaDesbloqueo = Date(),
                        desbloqueado = true
                    )
                } else {
                    return // No cumple criterios
                }
            }
            "PUNTOS_1000" -> {
                val puntos = obtenerPuntos(usuarioId)
                if (puntos >= 1000) {
                    Logro(
                        id = logroId,
                        nombre = "Maestro Acumulador",
                        descripcion = "Acumula 1000 puntos",
                        icono = "ic_trophy",
                        tipo = TipoLogro.PUNTOS_ACUMULADOS,
                        criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                            com.grupo4.appreservas.modelos.TipoCriterio.PUNTOS_ACUMULADOS,
                            1000
                        ),
                        fechaDesbloqueo = Date(),
                        desbloqueado = true
                    )
                } else {
                    return // No cumple criterios
                }
            }
            else -> return // Tipo de logro no reconocido
        }
        
        dbHelper.insertarLogroParaUsuario(usuarioId, logro)
    }

    /**
     * Inicializa los logros base para un usuario (sin desbloquear).
     */
    private fun inicializarLogrosBase(usuarioId: Int) {
        val logrosBase = listOf(
            Logro(
                id = "${usuarioId}_PRIMER_VIAJE",
                nombre = "Primer Viaje",
                descripcion = "Completa tu primera reserva",
                icono = "ic_trophy",
                tipo = TipoLogro.PRIMER_VIAJE,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.PRIMERA_RESERVA,
                    1
                ),
                fechaDesbloqueo = null,
                desbloqueado = false
            ),
            Logro(
                id = "${usuarioId}_VIAJERO_FRECUENTE",
                nombre = "Viajero Frecuente",
                descripcion = "Completa 5 reservas",
                icono = "ic_trophy",
                tipo = TipoLogro.VIAJERO_FRECUENTE,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.TOURS_COMPLETADOS,
                    5
                ),
                fechaDesbloqueo = null,
                desbloqueado = false
            ),
            Logro(
                id = "${usuarioId}_TOURS_10",
                nombre = "Explorador Experto",
                descripcion = "Completa 10 reservas",
                icono = "ic_trophy",
                tipo = TipoLogro.TOURS_COMPLETADOS,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.TOURS_COMPLETADOS,
                    10
                ),
                fechaDesbloqueo = null,
                desbloqueado = false
            ),
            Logro(
                id = "${usuarioId}_PUNTOS_500",
                nombre = "Acumulador de Puntos",
                descripcion = "Acumula 500 puntos",
                icono = "ic_trophy",
                tipo = TipoLogro.PUNTOS_ACUMULADOS,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.PUNTOS_ACUMULADOS,
                    500
                ),
                fechaDesbloqueo = null,
                desbloqueado = false
            ),
            Logro(
                id = "${usuarioId}_PUNTOS_1000",
                nombre = "Maestro Acumulador",
                descripcion = "Acumula 1000 puntos",
                icono = "ic_trophy",
                tipo = TipoLogro.PUNTOS_ACUMULADOS,
                criterio = com.grupo4.appreservas.modelos.CriterioLogro(
                    com.grupo4.appreservas.modelos.TipoCriterio.PUNTOS_ACUMULADOS,
                    1000
                ),
                fechaDesbloqueo = null,
                desbloqueado = false
            )
        )
        
        for (logro in logrosBase) {
            dbHelper.insertarLogroParaUsuario(usuarioId, logro)
        }
    }
}

