package com.grupo4.appreservas.repository

import android.content.Context
import com.grupo4.appreservas.modelos.CheckIn
import com.grupo4.appreservas.modelos.Clima
import com.grupo4.appreservas.modelos.Destino
import com.grupo4.appreservas.modelos.EncuestaRespuesta
import com.grupo4.appreservas.modelos.Logro
import com.grupo4.appreservas.modelos.Notificacion
import com.grupo4.appreservas.modelos.TipoNotificacion
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
     * Equivalente a crearUsuario(nombreCompleto, nombreUsuario, contrasena, rolOpcional): Usuario del diagrama UML.
     * Si no viene un rol en el body, le asigna el rol por defecto que es el de turista.
     * 
     * @param nombreCompleto El nombre completo del usuario
     * @param nombreUsuario El nombre de usuario (correo electrónico en este sistema)
     * @param contrasena La contraseña sin hashear
     * @param rolOpcional El rol opcional. Si es null, se asigna el rol por defecto (turista = 2)
     * @return El usuario creado con su ID asignado
     */
    fun crearUsuario(nombreCompleto: String, nombreUsuario: String, contrasena: String, rolOpcional: Int? = null): Usuario {
        // Si no viene un rol, asignar el rol por defecto (turista = 2)
        val rolId = rolOpcional ?: 2
        
        // Hashear la contraseña con SHA-256
        val contrasenaHash = hashSHA256(contrasena)

        // Crear el usuario
        // Nota: En este sistema, nombreUsuario se almacena como correo
        val usuario = Usuario(
            nombreCompleto = nombreCompleto,
            correo = nombreUsuario, // nombreUsuario se almacena como correo
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
     * Equivalente a validarCredenciales(nombreUsuario, contrasena): Usuario del diagrama UML.
     * 
     * @param nombreUsuario El nombre de usuario (correo electrónico en este sistema)
     * @param contrasena La contraseña sin hashear
     * @return El usuario si las credenciales son válidas, null en caso contrario
     */
    fun validarCredenciales(nombreUsuario: String, contrasena: String): Usuario? {
        // Buscar usuario por correo (nombreUsuario se almacena como correo)
        val usuario = dbHelper.buscarUsuarioPorCorreo(nombreUsuario) ?: return null

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
     * Valida un código QR de reserva y verifica que pertenezca al tour especificado.
     * Equivalente a validarCodigoQR(codigoQR, idTour): Reserva del diagrama UML.
     * 
     * @param codigoQR El código QR escaneado
     * @param idTour El ID del tour al que debe pertenecer la reserva
     * @return La Reserva si es válida y pertenece al tour, null en caso contrario
     */
    fun validarCodigoQR(codigoQR: String, idTour: String): Reserva? {
        // Buscar la reserva por código QR o por ID
        val reservaPorQR = dbHelper.obtenerReservaPorQR(codigoQR)
        val reservaPorId = dbHelper.obtenerReservaPorId(codigoQR)
        
        val reserva = reservaPorQR ?: reservaPorId
        
        // Verificar que la reserva existe y pertenece al tour
        if (reserva != null && reserva.tourId == idTour) {
            // Verificar que el código QR no haya sido usado ya
            if (!dbHelper.estaReservaUsada(codigoQR)) {
                return reserva
            }
        }
        
        return null
    }


    /**
     * Registra un check-in para una reserva.
     * Equivalente a registrarCheckIn(idReserva, idGuia, fechaHora): Checkin del diagrama UML.
     * 
     * @param idReserva El ID de la reserva
     * @param idGuia El ID del guía que realiza el check-in
     * @param fechaHora La fecha y hora del check-in
     * @return El objeto CheckIn registrado, o null si hubo un error
     */
    fun registrarCheckIn(idReserva: String, idGuia: Int, fechaHora: String): CheckIn? {
        val checkIn = CheckIn(
            reservaId = idReserva,
            guiaId = idGuia,
            horaRegistro = fechaHora,
            estado = "Confirmado"
        )
        
        val resultado = dbHelper.registrarCheckIn(checkIn)
        return if (resultado > 0) {
            // Obtener el check-in registrado con su ID
            val checkInRegistrado = dbHelper.obtenerCheckInPorReserva(idReserva)
            checkInRegistrado ?: checkIn.copy(checkInId = resultado.toInt())
        } else {
            null
        }
    }

    // ============= MÉTODOS PARA NOTIFICACIONES (según diagrama UML) =============

    /**
     * Obtiene los recordatorios de un usuario.
     * Equivalente a obtenerRecordatorios(usuarioId): List<Notificacion> del diagrama UML.
     */
    fun obtenerRecordatorios(usuarioId: Int): List<Notificacion> {
        return dbHelper.obtenerNotificacionesPorUsuario(usuarioId)
            .filter { it.tipo == TipoNotificacion.RECORDATORIO }
    }

    /**
     * Obtiene las alertas climáticas de un usuario.
     * Equivalente a obtenerAlertasClimaticas(usuarioId): List<Notificacion> del diagrama UML.
     */
    fun obtenerAlertasClimaticas(usuarioId: Int): List<Notificacion> {
        return dbHelper.obtenerNotificacionesPorUsuario(usuarioId)
            .filter { it.tipo == TipoNotificacion.ALERTA_CLIMATICA || it.tipo == TipoNotificacion.CLIMA_FAVORABLE }
    }

    /**
     * Obtiene las ofertas de último minuto de un usuario.
     * Equivalente a obtenerOfertasUltimoMinuto(usuarioId): List<Notificacion> del diagrama UML.
     */
    fun obtenerOfertasUltimoMinuto(usuarioId: Int): List<Notificacion> {
        return dbHelper.obtenerNotificacionesPorUsuario(usuarioId)
            .filter { it.tipo == TipoNotificacion.OFERTA_ULTIMO_MINUTO }
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
     * Equivalente a obtenerPuntosUsuario(usuariold): Int del diagrama UML.
     */
    fun obtenerPuntosUsuario(usuarioId: Int): Int {
        // Inicializar puntos si no existen
        dbHelper.inicializarPuntos(usuarioId)
        return dbHelper.obtenerPuntos(usuarioId)
    }

    /**
     * Obtiene los puntos acumulados de un usuario (método de compatibilidad).
     * @deprecated Usar obtenerPuntosUsuario en su lugar
     */
    @Deprecated("Usar obtenerPuntosUsuario", ReplaceWith("obtenerPuntosUsuario(usuarioId)"))
    fun obtenerPuntos(usuarioId: Int): Int {
        return obtenerPuntosUsuario(usuarioId)
    }

    /**
     * Obtiene los logros de un usuario.
     * Equivalente a obtenerLogrosUsuario(usuariold): List<Logro> del diagrama UML.
     */
    fun obtenerLogrosUsuario(usuarioId: Int): List<Logro> {
        val logros = dbHelper.obtenerLogros(usuarioId)
        
        // Si el usuario no tiene logros, inicializar los logros base
        if (logros.isEmpty()) {
            inicializarLogrosBase(usuarioId)
            return dbHelper.obtenerLogros(usuarioId)
        }
        
        return logros
    }

    /**
     * Obtiene los logros de un usuario (método de compatibilidad).
     * @deprecated Usar obtenerLogrosUsuario en su lugar
     */
    @Deprecated("Usar obtenerLogrosUsuario", ReplaceWith("obtenerLogrosUsuario(usuarioId)"))
    fun obtenerLogros(usuarioId: Int): List<Logro> {
        return obtenerLogrosUsuario(usuarioId)
    }

    /**
     * Obtiene las reservas completadas de un usuario.
     * Equivalente a obtenerReservasCompletadas(usuariold): List<Reserva> del diagrama UML.
     */
    fun obtenerReservasCompletadas(usuarioId: Int): List<Reserva> {
        return dbHelper.obtenerReservasPorUsuario(usuarioId)
            .filter { it.estaConfirmado() }
    }

    /**
     * Obtiene las reservas confirmadas de un usuario (método de compatibilidad).
     * @deprecated Usar obtenerReservasCompletadas en su lugar
     */
    @Deprecated("Usar obtenerReservasCompletadas", ReplaceWith("obtenerReservasCompletadas(usuarioId)"))
    fun obtenerReservasConfirmadas(usuarioId: Int): List<Reserva> {
        return obtenerReservasCompletadas(usuarioId)
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
        val reservasCompletadas = obtenerReservasCompletadas(usuarioId)
        val puntosActuales = obtenerPuntosUsuario(usuarioId)
        
        // Verificar logro "Primer Viaje"
        if (reservasCompletadas.size == 1) {
            desbloquearLogro(usuarioId, "PRIMER_VIAJE")
        }
        
        // Verificar logro "Viajero Frecuente" (5+ reservas)
        if (reservasCompletadas.size >= 5) {
            desbloquearLogro(usuarioId, "VIAJERO_FRECUENTE")
        }
        
        // Verificar logro por tours completados (10+ reservas)
        if (reservasCompletadas.size >= 10) {
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
                val puntos = obtenerPuntosUsuario(usuarioId)
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
                val puntos = obtenerPuntosUsuario(usuarioId)
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

    // ============= MÉTODOS PARA FOTOS DEL ÁLBUM (HU-008) =============

    /**
     * Obtiene las fotos aprobadas de un tour.
     * Equivalente a obtenerFotosPorTour(idTour): List<Foto> del diagrama UML.
     */
    fun obtenerFotosPorTour(idTour: String): List<com.grupo4.appreservas.modelos.Foto> {
        return dbHelper.obtenerFotosPorTour(idTour)
    }

    /**
     * Guarda una lista de fotos para un tour.
     * Equivalente a guardarFotosDeTour(idTour, listaFotos): Boolean del diagrama UML.
     */
    fun guardarFotosDeTour(idTour: String, listaFotos: List<com.grupo4.appreservas.modelos.Foto>): Boolean {
        return try {
            listaFotos.forEach { foto ->
                dbHelper.insertarFoto(foto)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ============= MÉTODOS PARA ENCUESTAS DE SATISFACCIÓN (HU-009) =============

    /**
     * Envía una encuesta automática al usuario después de finalizar un tour.
     * Equivalente a enviarEncuestaAutomatica(idTour, usuarioId): Boolean del diagrama UML.
     */
    fun enviarEncuestaAutomatica(idTour: String, usuarioId: Int): Boolean {
        return try {
            // Verificar que el usuario no haya respondido ya la encuesta
            if (dbHelper.existeEncuestaRespuesta(idTour, usuarioId.toString())) {
                return false // Ya respondió
            }

            // Obtener información del tour
            val tour = obtenerTourPorId(idTour)
            val nombreTour = tour?.nombre ?: "Tour"

            // Crear notificación de encuesta
            crearNotificacionEncuesta(usuarioId, idTour, nombreTour)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Guarda la respuesta de una encuesta.
     * Equivalente a guardarRespuestaEncuesta(idTour, usuarioId, calificacion, comentario): EncuestaRespuesta del diagrama UML.
     */
    fun guardarRespuestaEncuesta(idTour: String, usuarioId: Int, calificacion: Int, comentario: String): EncuestaRespuesta? {
        return try {
            // Validar calificación
            if (calificacion !in 1..5) {
                return null
            }

            // Verificar que no exista ya una respuesta
            val usuarioIdStr = usuarioId.toString()
            if (dbHelper.existeEncuestaRespuesta(idTour, usuarioIdStr)) {
                return null // Ya respondió
            }

            // Crear respuesta de encuesta
            val puntosOtorgados = 50 // Puntos por completar encuesta
            val encuesta = EncuestaRespuesta(
                idRespuesta = "ENC_${idTour}_${usuarioId}_${System.currentTimeMillis()}",
                idTour = idTour,
                usuarioId = usuarioIdStr,
                calificacion = calificacion,
                comentario = comentario,
                fechaRespuesta = Date(),
                puntosOtorgados = puntosOtorgados
            )

            // Guardar en la base de datos
            val resultado = dbHelper.insertarEncuestaRespuesta(encuesta)
            
            if (resultado > 0) {
                // Sumar puntos por completar encuesta
                dbHelper.sumarPuntos(usuarioId, puntosOtorgados)
                encuesta
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Crea una notificación de encuesta de satisfacción.
     */
    private fun crearNotificacionEncuesta(usuarioId: Int, tourId: String, nombreTour: String) {
        val notificacion = Notificacion(
            id = "ENCUESTA_${tourId}_${usuarioId}_${System.currentTimeMillis()}",
            usuarioId = usuarioId,
            tipo = TipoNotificacion.ENCUESTA_SATISFACCION,
            titulo = "Encuesta de Satisfacción",
            descripcion = "¡Cuéntanos sobre tu experiencia en $nombreTour!",
            fechaCreacion = Date(),
            tourId = tourId,
            destinoNombre = nombreTour
        )
        dbHelper.insertarNotificacion(notificacion)
    }

    /**
     * Obtiene la calificación promedio de un tour.
     */
    fun obtenerCalificacionPromedioTour(tourId: String): Double {
        return dbHelper.obtenerCalificacionPromedioTour(tourId)
    }

    /**
     * Verifica si un usuario ya respondió una encuesta para un tour.
     */
    fun yaRespondioEncuesta(tourId: String, usuarioId: Int): Boolean {
        return dbHelper.existeEncuestaRespuesta(tourId, usuarioId.toString())
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

