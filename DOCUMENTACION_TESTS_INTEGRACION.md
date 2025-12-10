# Documentación de Tests de Integración (MVVM en Kotlin)

## HU-001: Visualización del Catálogo de Destinos Turísticos

### Test IT-CATALOGO-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-CATALOGO-001

**Objetivo**: Validar la integración completa: UI → Controller → Repository → DatabaseHelper (Success Case - Visualización del catálogo)

**Componentes**: `CatalogoController`, `DestinoService`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar lista de destinos con foto, nombre, precio y descripción.

**Pasos**:
1. Invocar `solicitarDestinos()` del `CatalogoController`.
2. El Controller solicita destinos al `DestinoService`.
3. El Service consulta al `PeruvianServiceRepository`.
4. El Repository invoca `obtenerTodosLosDestinos()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `obtenerTodosLosDestinos()` del `DatabaseHelper` es invocado.
2. Se retorna una lista de destinos con al menos 2 elementos.
3. Cada destino contiene: `id`, `nombre`, `ubicacion`, `descripcion`, `precio`, `imagenUrl`.
4. Los destinos se muestran correctamente con todas las propiedades requeridas.

**Evidencia Adjunta**: Captura de la aserción de que se obtuvieron los destinos correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-CATALOGO-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-CATALOGO-002

**Objetivo**: Validar la integración completa: UI → Controller → Repository → DatabaseHelper (Success Case - Detalle del destino)

**Componentes**: `ControlDetalleDestino`, `DestinoService`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar un destino específico con información ampliada, duración, itinerario y fotos.

**Pasos**:
1. Invocar `cargarDetalle(destinoId)` del `ControlDetalleDestino`.
2. El Controller solicita el detalle al `DestinoService`.
3. El Service consulta al `PeruvianServiceRepository`.
4. El Repository invoca `obtenerDestinoPorId(destinoId)` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `obtenerDestinoPorId()` del `DatabaseHelper` es invocado.
2. Se retorna un objeto `Destino` con información completa.
3. El destino contiene: `id`, `nombre`, `ubicacion`, `precio`, `duracionHoras`, `incluye`, `imagenUrl`, `calificacion`, `numReseñas`.
4. La información ampliada se muestra correctamente en la UI.

**Evidencia Adjunta**: Captura de la aserción de que se obtuvo el detalle correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## HU-002: Reservar un Tour Seleccionando Fecha, Hora y Número de Personas

### Test IT-RESERVA-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-RESERVA-001

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Consultar disponibilidad)

**Componentes**: `ReservaViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar un `TourSlot` con capacidad y ocupados definidos.

**Pasos**:
1. Invocar `consultarDisponibilidadAsientos(tourSlotId)` del `ReservaViewModel`.
2. El ViewModel consulta al `PeruvianServiceRepository`.
3. El Repository invoca `obtenerTourSlotPorId(tourSlotId)` del `DatabaseHelper`.
4. Se calculan los cupos disponibles (capacidad - ocupados).

**Resultados Esperados**:
1. El método `obtenerTourSlotPorId()` del `DatabaseHelper` es invocado.
2. El `LiveData` `cuposDisponibles` emite el valor correcto.
3. El `LiveData` `disponibilidad` emite `true` si hay cupos disponibles.
4. Los cupos disponibles se calculan correctamente.

**Evidencia Adjunta**: Captura de la aserción de que los cupos disponibles se calcularon correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-RESERVA-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-RESERVA-002

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Crear reserva)

**Componentes**: `ReservaViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar `TourSlot` con cupos disponibles suficientes. Mock de inserción de reserva configurado.

**Pasos**:
1. Invocar `crearReserva(usuarioId, tourSlotId, cantidadPersonas)` del `ReservaViewModel`.
2. El ViewModel valida disponibilidad de cupos.
3. Se calcula el precio total (precio del destino × cantidad de personas).
4. Se genera código de confirmación y código QR.
5. Se invoca `insertarReserva()` del `DatabaseHelper`.
6. Se actualiza el `TourSlot` con los nuevos ocupados.

**Resultados Esperados**:
1. El método `obtenerTourSlotPorId()` del `DatabaseHelper` es invocado.
2. El método `insertarReserva()` del `DatabaseHelper` es invocado.
3. El método `actualizarTourSlot()` del `DatabaseHelper` es invocado.
4. Se retorna un objeto `Reserva` con: `usuarioId`, `destinoId`, `tourSlotId`, `numPersonas`, `estado` (PENDIENTE), `precioTotal`, `codigoConfirmacion`, `codigoQR`.
5. La reserva se guarda correctamente en la base de datos.

**Evidencia Adjunta**: Captura de la aserción de que la reserva se creó correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## HU-003: Pagar Reserva mediante Yape, Plin o Tarjeta y Recibir Comprobante Digital con Código QR

### Test IT-PAGO-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-PAGO-001

**Objetivo**: Validar la integración completa: UI → PaymentController → PaymentService → PaymentGateway (Success Case - Pago con Yape)

**Componentes**: `PaymentController`, `PaymentService`, `PaymentGateway` (Mockeado con éxito), `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `PaymentGateway` configurado para retornar `Pago` con estado `APROBADO`. Mock de `DatabaseHelper` configurado para insertar pago.

**Pasos**:
1. Invocar `pagar(bookingId, MetodoPago.YAPE, monto)` del `PaymentController`.
2. El Controller procesa el pago a través del `PaymentService`.
3. El Service invoca `charge()` del `PaymentGateway`.
4. Se guarda el pago en la base de datos mediante `insertarPago()`.

**Resultados Esperados**:
1. El método `charge()` del `PaymentGateway` es invocado.
2. Se retorna un objeto `Pago` con: `bookingId`, `monto`, `metodoPago` (YAPE), `estado` (APROBADO).
3. El método `insertarPago()` del `DatabaseHelper` es invocado.
4. El pago se guarda correctamente en la base de datos.

**Evidencia Adjunta**: Captura de la aserción de que el pago se procesó correctamente. Fragmento de log que muestra la invocación al `PaymentGateway`.

---

### Test IT-PAGO-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-PAGO-002

**Objetivo**: Validar la integración completa: UI → PaymentController → VoucherService (Success Case - Generar comprobante con QR)

**Componentes**: `PaymentController`, `VoucherService` (Mockeado con éxito), `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `VoucherService` configurado para retornar `Recibo` con código QR. Pago exitoso previamente procesado.

**Pasos**:
1. Invocar `generarComprobante(bookingId)` del `PaymentController`.
2. El Controller solicita el comprobante al `VoucherService`.
3. El Service invoca `emitir(bookingId)`.
4. Se retorna un `Recibo` con toda la información de la reserva y código QR.

**Resultados Esperados**:
1. El método `emitir()` del `VoucherService` es invocado.
2. Se retorna un objeto `Recibo` con: `bookingId`, `codigoConfirmacion`, `qrCode`, `destinoNombre`, `fecha`, `numPersonas`, `montoTotal`, `metodoPago`, `horaInicio`.
3. El código QR se genera correctamente.
4. El comprobante contiene toda la información requerida.

**Evidencia Adjunta**: Captura de la aserción de que el comprobante se generó correctamente. Fragmento de log que muestra la invocación al `VoucherService`.

---

### Test IT-PAGO-003

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-PAGO-003

**Objetivo**: Validar la integración completa: UI → PaymentController → PaymentService → PaymentGateway (Error Case - Pago fallido)

**Componentes**: `PaymentController`, `PaymentService`, `PaymentGateway` (Mockeado con error), `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `PaymentGateway` configurado para retornar `Pago` con estado `RECHAZADO`.

**Pasos**:
1. Invocar `pagar(bookingId, MetodoPago.YAPE, monto)` del `PaymentController`.
2. El Controller procesa el pago a través del `PaymentService`.
3. El Service invoca `charge()` del `PaymentGateway`.
4. El Gateway retorna un pago con estado `RECHAZADO`.

**Resultados Esperados**:
1. El método `charge()` del `PaymentGateway` es invocado.
2. Se retorna un objeto `Pago` con estado `RECHAZADO`.
3. El método `insertarPago()` del `DatabaseHelper` es invocado (para registrar el intento fallido).
4. La app notifica el error al usuario.

**Evidencia Adjunta**: Captura de la aserción de que el pago fue rechazado. Fragmento de log que muestra la invocación al `PaymentGateway`.

---

## HU-004: Registro e Inicio de Sesión

### Test IT-AUTH-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-AUTH-001

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Registro sin rol)

**Componentes**: `AutenticacionViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar que el usuario no existe. Mock de inserción de usuario configurado.

**Pasos**:
1. Invocar `registrarUsuario(nombreCompleto, correo, contrasena)` del `AutenticacionViewModel` (sin especificar rol).
2. El ViewModel valida que el usuario no existe.
3. Se hashea la contraseña con SHA-256.
4. Se asigna el rol por defecto (Turista, rolId = 2).
5. Se invoca `insertarUsuario()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `buscarUsuarioPorCorreo()` del `DatabaseHelper` es invocado.
2. El método `insertarUsuario()` del `DatabaseHelper` es invocado.
3. El usuario se crea con `rolId = 2` (Turista por defecto).
4. El `LiveData` `usuarioAutenticado` emite el usuario creado.
5. La contraseña se hashea correctamente con SHA-256.

**Evidencia Adjunta**: Captura de la aserción de que el usuario se creó con rol turista. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-AUTH-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-AUTH-002

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Inicio de sesión)

**Componentes**: `AutenticacionViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar un usuario existente con credenciales correctas.

**Pasos**:
1. Invocar `iniciarSesion(correo, contrasena)` del `AutenticacionViewModel`.
2. El ViewModel busca el usuario por correo.
3. Se hashea la contraseña ingresada con SHA-256.
4. Se compara con la contraseña almacenada.
5. Se obtiene el rol del usuario.

**Resultados Esperados**:
1. El método `buscarUsuarioPorCorreo()` del `DatabaseHelper` es invocado.
2. El método `obtenerRol()` del `DatabaseHelper` es invocado.
3. El `LiveData` `usuarioAutenticado` emite el usuario autenticado.
4. Se identifica el rol del usuario ("Turista" o "Administrador").
5. La navegación se dispara según el rol identificado.

**Evidencia Adjunta**: Captura de la aserción de que el usuario se autenticó correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## HU-005: Escaneo de Código QR

### Test IT-QR-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-QR-001

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Escaneo QR válido)

**Componentes**: `CheckInViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar una reserva válida con código QR. Mock de verificación de uso configurado para retornar que no está usada.

**Pasos**:
1. Invocar `procesarEscaneoQR(codigoQR, tourId, guiaId)` del `CheckInViewModel`.
2. El ViewModel valida el código QR mediante `validarCodigoQR()`.
3. Se busca la reserva por código QR.
4. Se verifica que el QR no haya sido usado.
5. Se verifica que la reserva pertenece al tour.
6. Se invoca `registrarCheckIn()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `obtenerReservaPorQR()` del `DatabaseHelper` es invocado.
2. El método `estaReservaUsada()` del `DatabaseHelper` es invocado.
3. El método `registrarCheckIn()` del `DatabaseHelper` es invocado.
4. El `LiveData` `resultadoCheckin` emite un `CheckIn` con estado "Confirmado".
5. La asistencia se marca como "Confirmada".

**Evidencia Adjunta**: Captura de la aserción de que el check-in se registró correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-QR-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-QR-002

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Error Case - QR inválido o ya usado)

**Componentes**: `CheckInViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar que el QR no existe o ya fue usado.

**Pasos**:
1. Invocar `procesarEscaneoQR(codigoQR, tourId, guiaId)` del `CheckInViewModel`.
2. El ViewModel valida el código QR mediante `validarCodigoQR()`.
3. Se busca la reserva por código QR (no se encuentra o ya está usada).
4. Se detecta que el QR es inválido o ya registrado.

**Resultados Esperados**:
1. El método `obtenerReservaPorQR()` del `DatabaseHelper` es invocado.
2. El método `estaReservaUsada()` del `DatabaseHelper` es invocado (si el QR existe).
3. El método `registrarCheckIn()` del `DatabaseHelper` NO es invocado.
4. El `LiveData` `mensajeEstado` emite un mensaje de error: "QR no válido o ya registrado".
5. La app muestra el mensaje de error al usuario.

**Evidencia Adjunta**: Captura de la aserción de que se mostró el mensaje de error. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## HU-006: Sistema de Notificaciones

### Test IT-NOTIF-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-NOTIF-001

**Objetivo**: Validar la integración completa: Repository → DatabaseHelper (Success Case - Recordatorio de horario)

**Componentes**: `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para insertar notificación. Tour próximo a iniciar (en 2 horas).

**Pasos**:
1. Invocar `crearNotificacionRecordatorio(usuarioId, tourId, nombreTour, hora, puntoEncuentro)` del `PeruvianServiceRepository`.
2. El Repository crea una notificación de tipo `RECORDATORIO`.
3. Se invoca `insertarNotificacion()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `insertarNotificacion()` del `DatabaseHelper` es invocado.
2. Se crea una notificación con: `usuarioId`, `tipo` (RECORDATORIO), `titulo`, `descripcion`, `tourId`, `horaTour`, `puntoEncuentro`.
3. La notificación se guarda correctamente en la base de datos.
4. El sistema envía una notificación push al turista.

**Evidencia Adjunta**: Captura de la aserción de que la notificación se creó correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-NOTIF-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-NOTIF-002

**Objetivo**: Validar la integración completa: Repository → DatabaseHelper (Success Case - Alerta climática)

**Componentes**: `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para insertar notificación. Cambio de clima detectado.

**Pasos**:
1. Invocar `crearNotificacionAlertaClimatica(usuarioId, ubicacion, titulo, descripcion)` del `PeruvianServiceRepository`.
2. El Repository crea una notificación de tipo `ALERTA_CLIMATICA`.
3. Se invoca `insertarNotificacion()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `insertarNotificacion()` del `DatabaseHelper` es invocado.
2. Se crea una notificación con: `usuarioId`, `tipo` (ALERTA_CLIMATICA), `titulo`, `descripcion`, `destinoNombre`.
3. La notificación se guarda correctamente en la base de datos.
4. El sistema envía una notificación automática con recomendaciones.

**Evidencia Adjunta**: Captura de la aserción de que la alerta climática se creó correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-NOTIF-003

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-NOTIF-003

**Objetivo**: Validar la integración completa: Repository → DatabaseHelper (Success Case - Oferta de último minuto)

**Componentes**: `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar tours con baja ocupación (< 30%). Mock de inserción de notificación configurado.

**Pasos**:
1. Invocar `obtenerToursConDescuento()` del `PeruvianServiceRepository`.
2. El Repository consulta tours con ocupación menor al 30%.
3. Invocar `crearNotificacionOferta(usuarioId, tourId, nombreTour, porcentajeDescuento)`.
4. Se invoca `insertarNotificacion()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `obtenerTodosLosTours()` del `DatabaseHelper` es invocado.
2. Se identifican tours con baja ocupación.
3. El método `insertarNotificacion()` del `DatabaseHelper` es invocado.
4. Se crea una notificación con: `usuarioId`, `tipo` (OFERTA), `titulo`, `descripcion`, `tourId`, `porcentajeDescuento`.
5. El sistema envía notificación con descuento disponible.

**Evidencia Adjunta**: Captura de la aserción de que la oferta se creó correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## HU-007: Sistema de Recompensas y Logros

### Test IT-RECOMP-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-RECOMP-001

**Objetivo**: Validar la integración completa: Repository → DatabaseHelper (Success Case - Acumulación de puntos)

**Componentes**: `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para inicializar y sumar puntos. Reserva confirmada previamente.

**Pasos**:
1. Invocar `sumarPuntosPorReserva(usuarioId, reservaId)` del `PeruvianServiceRepository`.
2. El Repository verifica si el usuario tiene puntos inicializados.
3. Se invoca `inicializarPuntos()` si es necesario.
4. Se invoca `sumarPuntos(usuarioId, PUNTOS_POR_RESERVA)` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `inicializarPuntos()` del `DatabaseHelper` es invocado (si es necesario).
2. El método `sumarPuntos()` del `DatabaseHelper` es invocado.
3. Se suman `PUNTOS_POR_RESERVA` puntos automáticamente.
4. Los puntos se guardan correctamente en la base de datos.
5. El nivel del usuario se calcula correctamente basado en los puntos acumulados.

**Evidencia Adjunta**: Captura de la aserción de que los puntos se sumaron correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-RECOMP-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-RECOMP-002

**Objetivo**: Validar la integración completa: ViewModel → Repository → DatabaseHelper (Success Case - Visualización de logros)

**Componentes**: `RecompensasViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar logros del usuario. Usuario con puntos acumulados.

**Pasos**:
1. Invocar `cargarLogros(usuarioId)` del `RecompensasViewModel`.
2. El ViewModel consulta al `PeruvianServiceRepository`.
3. El Repository invoca `obtenerLogros(usuarioId)` del `DatabaseHelper`.
4. Se inicializan logros base si no existen.

**Resultados Esperados**:
1. El método `obtenerLogros()` del `DatabaseHelper` es invocado.
2. El método `insertarLogroParaUsuario()` del `DatabaseHelper` es invocado (si se crean logros base).
3. El `LiveData` `logros` emite la lista de logros.
4. Se muestran puntos acumulados y logros desbloqueados.
5. Los logros incluyen: `id`, `nombre`, `descripcion`, `tipo`, `desbloqueado`, `fechaDesbloqueo`.

**Evidencia Adjunta**: Captura de la aserción de que los logros se cargaron correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-RECOMP-003

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-RECOMP-003

**Objetivo**: Validar la integración completa: ViewModel → Repository → DatabaseHelper (Success Case - Desbloqueo de logro Primer Viaje)

**Componentes**: `RecompensasViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar una reserva confirmada. Usuario sin logros previos.

**Pasos**:
1. Invocar `actualizarPuntos(usuarioId, reservaId)` del `RecompensasViewModel`.
2. El ViewModel suma puntos por la reserva.
3. El Repository verifica y desbloquea logros mediante `verificarYDesbloquearLogros()`.
4. Se verifica si el usuario cumple con el criterio del logro "Primer Viaje".
5. Se invoca `insertarLogroParaUsuario()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `obtenerReservasPorUsuario()` del `DatabaseHelper` es invocado.
2. El método `existeLogro()` del `DatabaseHelper` es invocado.
3. El método `insertarLogroParaUsuario()` del `DatabaseHelper` es invocado.
4. Se crea/actualiza el logro "Primer Viaje" con `desbloqueado = true`.
5. El `LiveData` `logros` emite el logro desbloqueado.

**Evidencia Adjunta**: Captura de la aserción de que el logro se desbloqueó correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## HU-008: Subida y Visualización de Fotos del Álbum Grupal

### Test IT-ALBUM-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-ALBUM-001

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Subida de fotos)

**Componentes**: `AlbumTourViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para insertar fotos. Tour finalizado. Usuario con acceso al álbum.

**Pasos**:
1. Invocar `subirFotosSeleccionadas(tourId, rutasImagenes, nombreAutor)` del `AlbumTourViewModel`.
2. El ViewModel procesa cada ruta de imagen.
3. Se crea un objeto `Foto` para cada imagen.
4. Se invoca `insertarFoto()` del `DatabaseHelper` para cada foto.

**Resultados Esperados**:
1. El método `insertarFoto()` del `DatabaseHelper` es invocado (al menos 2 veces para 2 fotos).
2. Cada foto se guarda con: `idFoto`, `idTour`, `urlImagen`, `nombreAutor`, `fechaSubida`, `aprobada` (true).
3. El `LiveData` `mensajeEstado` emite un mensaje de éxito.
4. Las fotos se suben correctamente al álbum grupal.

**Evidencia Adjunta**: Captura de la aserción de que las fotos se guardaron correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-ALBUM-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-ALBUM-002

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Visualización de álbum)

**Componentes**: `AlbumTourViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar fotos aprobadas del tour. Fotos existentes en el álbum.

**Pasos**:
1. Invocar `cargarFotosAlbum(tourId)` del `AlbumTourViewModel`.
2. El ViewModel consulta al `PeruvianServiceRepository`.
3. El Repository invoca `obtenerFotosPorTour(tourId)` del `DatabaseHelper`.
4. Se filtran solo las fotos con `aprobada = true`.

**Resultados Esperados**:
1. El método `obtenerFotosPorTour()` del `DatabaseHelper` es invocado.
2. Se retorna una lista de fotos aprobadas.
3. El `LiveData` `fotosAlbum` emite la lista de fotos.
4. Solo se muestran las fotos con `aprobada = true`.
5. Las fotos se muestran correctamente en la UI.

**Evidencia Adjunta**: Captura de la aserción de que se obtuvieron las fotos aprobadas. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## HU-009: Encuestas de Satisfacción

### Test IT-ENCUESTA-001

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-ENCUESTA-001

**Objetivo**: Validar la integración completa: Repository → DatabaseHelper (Success Case - Envío automático de encuesta)

**Componentes**: `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar que el tour existe y el usuario no ha respondido. Tour finalizado.

**Pasos**:
1. Invocar `enviarEncuestaAutomatica(tourId, usuarioId)` del `PeruvianServiceRepository`.
2. El Repository verifica que el tour existe.
3. Se verifica que el usuario no ha respondido mediante `existeEncuestaRespuesta()`.
4. Se crea una notificación de tipo `ENCUESTA_SATISFACCION`.
5. Se invoca `insertarNotificacion()` del `DatabaseHelper`.

**Resultados Esperados**:
1. El método `obtenerTourPorId()` del `DatabaseHelper` es invocado.
2. El método `existeEncuestaRespuesta()` del `DatabaseHelper` es invocado.
3. El método `insertarNotificacion()` del `DatabaseHelper` es invocado.
4. Se crea una notificación con: `usuarioId`, `tipo` (ENCUESTA_SATISFACCION), `titulo`, `descripcion`, `tourId`.
5. El sistema envía la notificación con enlace o formulario de encuesta.

**Evidencia Adjunta**: Captura de la aserción de que la encuesta se envió correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

### Test IT-ENCUESTA-002

**Campo**: Valor de Ejemplo

**ID de Prueba**: IT-ENCUESTA-002

**Objetivo**: Validar la integración completa: UI → ViewModel → Repository → DatabaseHelper (Success Case - Registro de respuesta)

**Componentes**: `EncuestaViewModel`, `PeruvianServiceRepository`, `DatabaseHelper` (Mockeado con éxito)

**Precondiciones**: Mock de `DatabaseHelper` configurado para retornar que el usuario no ha respondido. Encuesta enviada previamente.

**Pasos**:
1. Invocar `registrarRespuesta(tourId, usuarioId, calificacion, comentario)` del `EncuestaViewModel`.
2. El ViewModel valida que la calificación esté entre 1 y 5.
3. Se verifica que el usuario no ha respondido.
4. Se crea un objeto `EncuestaRespuesta`.
5. Se invoca `insertarEncuestaRespuesta()` del `DatabaseHelper`.
6. Se suman puntos por completar la encuesta (50 puntos).

**Resultados Esperados**:
1. El método `existeEncuestaRespuesta()` del `DatabaseHelper` es invocado.
2. El método `insertarEncuestaRespuesta()` del `DatabaseHelper` es invocado.
3. El método `sumarPuntos()` del `DatabaseHelper` es invocado (con 50 puntos).
4. Se guarda la respuesta con: `idTour`, `usuarioId`, `calificacion`, `comentario`, `fechaRespuesta`.
5. El `LiveData` `respuestaEncuesta` emite la respuesta guardada.
6. El `LiveData` `encuestaEnviada` emite `true`.
7. Se genera una métrica de satisfacción.

**Evidencia Adjunta**: Captura de la aserción de que la respuesta se registró correctamente. Fragmento de log que muestra la invocación al `DatabaseHelper`.

---

## Notas Generales

- Todos los tests utilizan mocks de `DatabaseHelper` para evitar acceso real a la base de datos.
- Se utiliza `InstantTaskExecutorRule` para ejecutar tareas de LiveData de forma síncrona.
- Se utiliza `StandardTestDispatcher` para manejar coroutines en los tests.
- Los tests verifican tanto el flujo exitoso como los casos de error.
- Cada test documenta la integración completa desde la UI hasta la base de datos.

