# Mapeo de Código por Historia de Usuario (HU)

Este documento detalla qué código del proyecto (incluyendo archivos Kotlin y XML) pertenece a cada Historia de Usuario (HU).

---

## HU-001: Visualización del Catálogo de Destinos Turísticos

**Descripción:** Como turista, deseo visualizar el catálogo de destinos turísticos con fotos, precios y detalles.

### Escenario 1: Visualización del catálogo
**Flujo:** El turista accede a la opción "Destinos" desde el menú principal → La app carga la lista de destinos disponibles → Se muestran los destinos con su foto, nombre, precio y breve descripción.

### Escenario 2: Detalle del destino
**Flujo:** El turista selecciona un destino del listado → Se abre la vista de detalle del destino → Se muestran información ampliada, duración, itinerario y fotos del lugar.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/CatalogoActivity.kt`** (Líneas 1-273)
  - Líneas 32-76: Clase principal y configuración inicial
  - Líneas 152-161: Configuración del RecyclerView para mostrar destinos
  - Líneas 163-184: Configuración del buscador y filtrado por texto
  - Líneas 186-199: Carga de destinos desde el controlador
  - Líneas 201-243: Configuración de chips de categorías para filtrado
  - Líneas 260-265: Navegación al detalle del destino

- **`app/src/main/java/com/grupo4/appreservas/ui/DetalleDestinoActivity.kt`** (Líneas 1-167)
  - Líneas 20-51: Clase principal y configuración inicial
  - Líneas 59-83: Obtención del destino desde el intent
  - Líneas 99-114: Inicialización de vistas y botón de reserva
  - Líneas 116-120: Navegación a la pantalla de reserva
  - Líneas 122-165: Mostrar detalle completo del destino (precio, duración, descripción, etc.)

#### **Controller**
- **`app/src/main/java/com/grupo4/appreservas/controller/CatalogoController.kt`** (Líneas 1-20)
  - Líneas 10-19: Método `solicitarDestinos()` que obtiene la lista de destinos

- **`app/src/main/java/com/grupo4/appreservas/controller/ControlDetalleDestino.kt`**
  - Método `cargarDetalle(idDestino)` para obtener información detallada del destino

#### **Service**
- **`app/src/main/java/com/grupo4/appreservas/service/DestinoService.kt`**
  - Método `listarDestinos()` que retorna la lista de destinos disponibles

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Métodos relacionados con la obtención de destinos

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Líneas 1423-1443: Método `insertarDestino(destino: Destino)`
  - Líneas 1445-1464: Método `obtenerTodosLosDestinos()`
  - Líneas 1466-1478: Método `obtenerDestinoPorId(destinoId: String)`
  - Líneas 1373-1392: Método `cursorToDestino(cursor: Cursor)` para convertir cursor a objeto Destino

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/Destino.kt`** (Líneas 1-19)
  - Data class completa con todos los campos del destino (id, nombre, ubicación, descripción, precio, duración, etc.)

#### **Adapters**
- **`app/src/main/java/com/grupo4/appreservas/adapter/DestinosAdapter.kt`**
  - Adaptador para mostrar la lista de destinos en el RecyclerView

#### **Layouts XML**
- **`app/src/main/res/layout/activity_catalog.xml`**
  - Layout completo de la pantalla de catálogo con RecyclerView, buscador y chips de categorías

- **`app/src/main/res/layout/activity_destination_detail.xml`**
  - Layout completo de la pantalla de detalle del destino con imagen, precio, descripción, etc.

- **`app/src/main/res/layout/item_destino.xml`**
  - Layout del item individual del RecyclerView que muestra cada destino en la lista

#### **AndroidManifest.xml**
- Líneas 73-77: Declaración de `CatalogoActivity`
- Líneas 79-83: Declaración de `DetalleDestinoActivity`

---

## HU-002: Reserva de Tours

**Descripción:** Como turista, deseo reservar un tour seleccionando fecha, hora y número de personas.

### Escenario 1: Selección de tour
**Flujo:** El turista visualiza el detalle de un destino → Selecciona "Reservar" e indica fecha, hora y cantidad de personas → La app muestra la disponibilidad y permite confirmar la reserva.

### Escenario 2: Confirmación de reserva
**Flujo:** El turista confirma los datos de la reserva → Da clic en "Confirmar" → La app genera un resumen de lo que el usuario ha elegido junto con los distintos métodos de pago.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/ReservasActivity.kt`** (Líneas 1-275)
  - Líneas 28-70: Clase principal y configuración inicial
  - Líneas 72-85: Inicialización de dependencias y observadores del ViewModel
  - Líneas 87-103: Inicialización de vistas
  - Líneas 105-116: Carga del destino seleccionado
  - Líneas 118-132: Configuración del spinner de número de personas
  - Líneas 134-144: Configuración de listeners (selección de fecha, confirmación)
  - Líneas 146-170: Selector de fecha con DatePickerDialog
  - Líneas 172-195: Carga de horas disponibles según la fecha seleccionada
  - Líneas 197-200: Actualización del tourSlotId
  - Líneas 202-206: Consulta de disponibilidad de asientos
  - Líneas 208-216: Actualización de cupos disponibles en la UI
  - Líneas 218-221: Actualización del precio total
  - Líneas 223-257: Confirmación de reserva y navegación a pago

#### **ViewModel**
- **`app/src/main/java/com/grupo4/appreservas/viewmodel/ReservaViewModel.kt`**
  - Lógica de negocio para crear reservas y consultar disponibilidad

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Líneas 146-185: Método `crearReserva(idUsuario, idTourSlot, cantidadPasajeros): Reserva`
  - Métodos relacionados con consulta de disponibilidad y horarios

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Métodos para insertar y consultar reservas en la base de datos

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/Reserva.kt`** (Líneas 1-66)
  - Data class completa con todos los campos de la reserva (id, userId, destinoId, fecha, hora, numPersonas, estado, etc.)
  - Enum `EstadoReserva` (PENDIENTE, CONFIRMADO, CANCELADO)

- **`app/src/main/java/com/grupo4/appreservas/modelos/Tour.kt`**
  - Modelo de Tour relacionado con las reservas

#### **Layouts XML**
- **`app/src/main/res/layout/activity_reservation.xml`**
  - Layout completo de la pantalla de reserva con selectores de fecha, hora, número de personas y botón de confirmación

- **`app/src/main/res/layout/dialog_calendar_picker.xml`**
  - Layout del diálogo de selección de fecha

- **`app/src/main/res/layout/item_calendar_date.xml`**
  - Layout de item del calendario

#### **AndroidManifest.xml**
- Líneas 85-88: Declaración de `ReservasActivity`

---

## HU-003: Pago de Reservas

**Descripción:** Como turista, deseo pagar mi reserva mediante Yape, Plin o tarjeta y recibir un comprobante digital con código QR.

### Escenario 1: Selección del método de pago
**Flujo:** El turista está en la interfaz de resumen y pago → Elige Yape, Plin o tarjeta como método de pago → Se muestra la pasarela correspondiente.

### Escenario 2: Pago exitoso
**Flujo:** El turista completa el pago → Presiona el botón de Pagar → Lo redirige a la pantalla de reserva confirmada en donde se muestra el resumen de lo que ha reservado y un código QR junto con un botón de volver a inicio.

### Escenario 3: Pago fallido
**Flujo:** La transacción no se completa → La pasarela devuelve error → La app notifica el error.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/PaymentActivity.kt`** (Líneas 1-272)
  - Líneas 32-72: Clase principal y configuración inicial
  - Líneas 74-81: Inicialización de dependencias (PaymentController, PaymentService, etc.)
  - Líneas 83-99: Inicialización de vistas
  - Líneas 101-124: Carga de información de la reserva
  - Líneas 126-144: Configuración de listeners para selección de método de pago
  - Líneas 146-159: Selección de método de pago (Yape, Plin, Tarjeta)
  - Líneas 161-166: Actualización de bordes de las cards de métodos de pago
  - Líneas 168-223: Procesamiento del pago (pago exitoso/fallido)
  - Líneas 232-261: Programación del cambio automático de estado de reserva a CONFIRMADO
  - Líneas 263-270: Navegación a VoucherActivity con comprobante

- **`app/src/main/java/com/grupo4/appreservas/ui/VoucherActivity.kt`** (Líneas 1-187)
  - Líneas 26-51: Clase principal y configuración inicial
  - Líneas 53-68: Inicialización de vistas y listeners
  - Líneas 74-124: Mostrar resumen de reserva y generar código QR
  - Líneas 130-138: Mostrar mensaje de puntos ganados
  - Líneas 160-185: Generación del código QR como Bitmap

#### **Controller**
- **`app/src/main/java/com/grupo4/appreservas/controller/PaymentController.kt`** (Líneas 1-44)
  - Líneas 14-17: Constructor con PaymentService y VoucherService
  - Líneas 23-34: Método `pagar(bookingId, metodo, monto): Pago?`
  - Líneas 40-42: Método `generarComprobante(bookingId): Recibo?`

#### **Service**
- **`app/src/main/java/com/grupo4/appreservas/service/PaymentService.kt`** (Líneas 1-74)
  - Líneas 12-15: Constructor con repository y paymentGateway
  - Líneas 21-34: Método `payYapePlin(request): Pago` para Yape
  - Líneas 40-53: Método `payPlin(request): Pago` para Plin
  - Líneas 59-72: Método `payCard(request): Pago` para tarjeta

- **`app/src/main/java/com/grupo4/appreservas/service/PaymentGateway.kt`**
  - Pasarela de pago que procesa las transacciones

- **`app/src/main/java/com/grupo4/appreservas/service/VoucherService.kt`**
  - Servicio para generar comprobantes/vouchers

- **`app/src/main/java/com/grupo4/appreservas/service/QRService.kt`**
  - Servicio para generar códigos QR

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/Pago.kt`**
  - Data class con información del pago (id, monto, método, estado, etc.)

- **`app/src/main/java/com/grupo4/appreservas/modelos/MetodoPago.kt`** (enum)
  - Enum con valores: YAPE, PLIN, TARJETA

- **`app/src/main/java/com/grupo4/appreservas/modelos/Recibo.kt`**
  - Data class con información del comprobante (bookingId, destinoNombre, fecha, hora, monto, código QR, etc.)

#### **Layouts XML**
- **`app/src/main/res/layout/activity_payment.xml`**
  - Layout completo de la pantalla de pago con cards de métodos de pago (Yape, Plin, Tarjeta) y botón de pagar

- **`app/src/main/res/layout/activity_voucher.xml`**
  - Layout completo del comprobante con resumen de reserva, código QR y botón de volver a inicio

- **`app/src/main/res/drawable/bg_yape.xml`**
  - Background drawable para el método de pago Yape

- **`app/src/main/res/drawable/bg_plin.xml`**
  - Background drawable para el método de pago Plin

- **`app/src/main/res/drawable/bg_tarjeta.xml`**
  - Background drawable para el método de pago Tarjeta

#### **AndroidManifest.xml**
- Líneas 90-93: Declaración de `PaymentActivity`
- Líneas 95-98: Declaración de `VoucherActivity`

---

## HU-004: Registro e Inicio de Sesión

**Descripción:** Como turista, deseo poder registrarme o iniciar sesión en la app.

### Escenario 1: Registro de cuenta de usuario
**Flujo:** El usuario está en la pantalla de Registro → Completa el formulario de registro con datos válidos y presiona "Crear cuenta" → El sistema crea un nuevo usuario, si no viene un rol en el body, le asigna el rol por defecto que es el de turista y lo redirige al dashboard correspondiente a ese rol.

### Escenario 2: Inicio de sesión
**Flujo:** Dado que un usuario registrado está en la pantalla de "Login" e ingresa sus credenciales correctas → Completa los campos requeridos y el usuario presiona "Iniciar Sesión" → Entonces el sistema valida las credenciales, identifica el rol ("Turista" o "Administrador") y lo redirige a la pantalla principal correspondiente según su rol.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/RegistroActivity.kt`** (Líneas 1-134)
  - Líneas 21-37: Clase principal y configuración inicial
  - Líneas 39-58: Inicialización de dependencias y observadores del ViewModel
  - Líneas 60-66: Inicialización de vistas
  - Líneas 68-76: Configuración de listeners
  - Líneas 82-104: Envío de datos de registro al ViewModel
  - Líneas 110-118: Navegación a PanelPrincipalActivity después de registro exitoso
  - Líneas 120-124: Navegación a LoginActivity

- **`app/src/main/java/com/grupo4/appreservas/ui/LoginActivity.kt`** (Líneas 1-135)
  - Líneas 20-35: Clase principal y configuración inicial
  - Líneas 37-55: Inicialización de dependencias y observadores del ViewModel
  - Líneas 57-62: Inicialización de vistas
  - Líneas 64-72: Configuración de listeners
  - Líneas 78-94: Envío de credenciales al ViewModel
  - Líneas 100-108: Navegación a PanelPrincipalActivity después de login exitoso
  - Líneas 110-113: Navegación a RegistroActivity

- **`app/src/main/java/com/grupo4/appreservas/ui/PanelPrincipalActivity.kt`** (Líneas 1-98)
  - Líneas 18-60: Clase principal y configuración inicial
  - Líneas 73-96: Método `mostrarSegunRol(usuario)` que redirige según el rol (Turista → CatalogoActivity, Administrador → PanelGuiaActivity)

#### **ViewModel**
- **`app/src/main/java/com/grupo4/appreservas/viewmodel/AutenticacionViewModel.kt`**
  - Lógica de negocio para registro e inicio de sesión
  - Métodos: `registrarUsuario()`, `iniciarSesion()`

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Líneas 381-398: Método `crearUsuario(nombreCompleto, nombreUsuario, contrasena, rolId): Usuario`
  - Líneas 408-420: Método `validarCredenciales(nombreUsuario, contrasena): Usuario?`
  - Líneas 426-429: Método `obtenerRol(usuarioId): Rol?`

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Métodos para insertar y consultar usuarios en la base de datos
  - Métodos para hash de contraseñas (SHA256)

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/Usuario.kt`**
  - Data class con información del usuario (usuarioId, nombreCompleto, correo, contrasena, rolId, etc.)

- **`app/src/main/java/com/grupo4/appreservas/modelos/Rol.kt`**
  - Data class con información del rol (rolId, nombre, descripcion)

#### **Layouts XML**
- **`app/src/main/res/layout/activity_registro.xml`**
  - Layout completo de la pantalla de registro con campos de nombre, correo, contraseña y botón de crear cuenta

- **`app/src/main/res/layout/activity_login.xml`**
  - Layout completo de la pantalla de login con campos de correo, contraseña y botón de iniciar sesión

- **`app/src/main/res/layout/activity_panel_principal.xml`**
  - Layout del panel principal que redirige según el rol

#### **AndroidManifest.xml**
- Líneas 33-37: Declaración de `LoginActivity`
- Líneas 39-44: Declaración de `RegistroActivity`
- Líneas 46-51: Declaración de `PanelPrincipalActivity`

---

## HU-005: Escaneo de Código QR

**Descripción:** Como guía turístico, deseo escanear el código QR de los turistas el día del tour.

### Escenario 1: Escaneo de QR válido
**Flujo:** El guía abre la opción de escanear QR → Escanea el código del turista → El sistema valida el QR y marca la asistencia como "Confirmada".

### Escenario 2: Escaneo de QR inválido
**Flujo:** El código ya fue usado o no corresponde → El guía intenta escanearlo → El sistema muestra mensaje "QR no válido o ya registrado".

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/EscaneoQRActivity.kt`** (Líneas 1-182)
  - Líneas 24-58: Clase principal y configuración inicial
  - Líneas 60-63: Inicialización de vistas (barcodeView, tvResultado)
  - Líneas 65-67: Configuración del escaneo continuo
  - Líneas 73-75: Inicio de la cámara para escanear
  - Líneas 77-93: Callback del escáner de códigos QR
  - Líneas 99-102: Envío del código QR escaneado para procesamiento
  - Líneas 104-125: Observadores del ViewModel para resultados y errores
  - Líneas 131-135: Mostrar resultado de check-in exitoso
  - Líneas 141-145: Mostrar mensaje de error
  - Líneas 147-162: Manejo de permisos de cámara
  - Líneas 164-176: Ciclo de vida (onResume, onPause) para controlar la cámara

#### **ViewModel**
- **`app/src/main/java/com/grupo4/appreservas/viewmodel/CheckInViewModel.kt`** (Líneas 1-47)
  - Líneas 20-28: Propiedades LiveData para resultado de check-in y mensajes
  - Líneas 34-47: Método `procesarEscaneoQR(codigoQR, idTour, idGuia)` que valida y registra el check-in

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Líneas 473-489: Método `validarCodigoQR(codigoQR, idTour): Reserva?` que valida el código QR
  - Líneas 492-520: Método `registrarCheckIn(reservaId, tourId, guiaId): CheckIn?` que registra la asistencia

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Métodos para consultar reservas por código QR
  - Métodos para verificar si un código QR ya fue usado
  - Métodos para insertar check-ins

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/CheckIn.kt`**
  - Data class con información del check-in (id, reservaId, tourId, guiaId, horaRegistro, etc.)

#### **Layouts XML**
- **`app/src/main/res/layout/activity_escaneo_qr.xml`**
  - Layout completo de la pantalla de escaneo QR con vista de cámara y área de resultado

#### **AndroidManifest.xml**
- Líneas 6-7: Permisos de cámara (`CAMERA`)
- Líneas 9-10: Features de cámara (no requeridas)
- Líneas 66-71: Declaración de `EscaneoQRActivity`

---

## HU-006: Notificaciones Push

**Descripción:** Como turista, deseo recibir recordatorios, alertas climáticas y promociones de último minuto.

### Escenario 1: Recordatorio de horario
**Flujo:** El tour está próximo a iniciar → Se activa una notificación push → El turista recibe el recordatorio con hora y punto de encuentro.

### Escenario 2: Alerta climática
**Flujo:** Se detecta un cambio de clima → El sistema envía una notificación automática → El turista recibe un aviso con recomendaciones.

### Escenario 3: Oferta de último minuto
**Flujo:** Un tour tiene baja ocupación → El sistema activa una promoción → Se envía notificación con descuento disponible.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/NotificacionesActivity.kt`** (Líneas 1-194)
  - Líneas 21-50: Clase principal y configuración inicial
  - Líneas 52-57: Inicialización de vistas
  - Líneas 59-73: Configuración del RecyclerView de notificaciones
  - Líneas 75-83: Configuración de listeners
  - Líneas 85-104: Observadores del ViewModel
  - Líneas 110-118: Carga de notificaciones del usuario
  - Líneas 124-134: Mostrar lista de notificaciones
  - Líneas 140-147: Selección de notificación
  - Líneas 153-159: Abrir encuesta desde notificación
  - Líneas 165-187: Mostrar detalle de notificación según tipo

#### **Service**
- **`app/src/main/java/com/grupo4/appreservas/service/NotificacionesService.kt`** (Líneas 1-77)
  - Líneas 18-41: Creación del canal de notificaciones para Android 8.0+
  - Líneas 46-77: Método `mostrarNotificacion(notificacion, usuarioId)` que muestra la notificación push

- **`app/src/main/java/com/grupo4/appreservas/service/NotificacionesScheduler.kt`** (Líneas 1-50)
  - Líneas 16-41: Programación del trabajo periódico de notificaciones (cada 6 horas)
  - Líneas 46-48: Cancelación del trabajo periódico

#### **Worker**
- **`app/src/main/java/com/grupo4/appreservas/worker/NotificacionesWorker.kt`** (Líneas 1-111)
  - Líneas 16-47: Clase Worker que se ejecuta en segundo plano
  - Líneas 24-47: Método `doWork()` que genera notificaciones para todos los usuarios turistas
  - Líneas 49-106: Método `generarRecordatoriosHorario(usuarioId)` para recordatorios de tours próximos
  - Líneas 108-150: Método `generarAlertasClimaticas(usuarioId)` para alertas climáticas
  - Líneas 152-188: Método `generarOfertasUltimoMinuto(usuarioId)` para ofertas de último minuto

#### **ViewModel**
- **`app/src/main/java/com/grupo4/appreservas/viewmodel/NotificacionesViewModel.kt`**
  - Lógica de negocio para cargar y gestionar notificaciones

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Líneas 608-651: Métodos relacionados con notificaciones:
    - `crearNotificacionRecordatorio()`
    - `crearNotificacionAlertaClimatica()`
    - `crearNotificacionOferta()`
    - `obtenerRecordatorios(usuarioId)`
    - `obtenerNotificacionesNoLeidasPorUsuario(usuarioId)`

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Métodos para insertar y consultar notificaciones en la base de datos

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/Notificacion.kt`** (Líneas 1-56)
  - Data class con información de la notificación (id, titulo, descripcion, tipo, tourId, leida, etc.)
  - Enum `TipoNotificacion` (RECORDATORIO, ALERTA_CLIMATICA, OFERTA_ULTIMO_MINUTO, ENCUESTA_SATISFACCION)

#### **Adapters**
- **`app/src/main/java/com/grupo4/appreservas/adapter/NotificacionesAdapter.kt`**
  - Adaptador para mostrar la lista de notificaciones en el RecyclerView

#### **Layouts XML**
- **`app/src/main/res/layout/activity_notificaciones.xml`**
  - Layout completo de la pantalla de notificaciones con RecyclerView y opciones de marcar como leídas

- **`app/src/main/res/layout/item_notificacion.xml`**
  - Layout del item individual de notificación en el RecyclerView

- **`app/src/main/res/drawable/ic_notifications.xml`**
  - Icono de notificaciones

- **`app/src/main/res/drawable/circle_badge_red.xml`**
  - Badge rojo para contador de notificaciones no leídas

#### **AndroidManifest.xml**
- Líneas 100-104: Declaración de `NotificacionesActivity`

---

## HU-007: Sistema de Puntos y Logros

**Descripción:** Como turista, deseo acumular puntos y logros por cada reserva confirmada.

### Escenario 1: Acumulación de puntos
**Flujo:** El turista completa una reserva → La reserva pasa a estado "Completada" → Se suman puntos automáticamente en su perfil.

### Escenario 2: Visualización de logros
**Flujo:** El turista accede a su perfil → Consulta la sección "Mis logros" → Se muestran puntos acumulados y logros desbloqueados.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/RecompensasActivity.kt`** (Líneas 1-260)
  - Líneas 27-64: Clase principal y configuración inicial
  - Líneas 73-92: Inicialización de vistas
  - Líneas 94-124: Configuración del RecyclerView para logros e historial
  - Líneas 126-138: Configuración de listeners
  - Líneas 140-166: Observadores del ViewModel
  - Líneas 168-178: Carga de datos del perfil
  - Líneas 184-198: Abrir álbum de fotos del tour
  - Líneas 204-221: Mostrar puntos del usuario con cálculo de nivel
  - Líneas 227-233: Mostrar logros del usuario
  - Líneas 239-241: Mostrar reservas en el historial
  - Líneas 246-257: Actualizar barra de progreso según puntos

#### **ViewModel**
- **`app/src/main/java/com/grupo4/appreservas/viewmodel/RecompensasViewModel.kt`** (Líneas 1-156)
  - Líneas 20-40: Propiedades LiveData para puntos, logros, reservas, etc.
  - Líneas 45-66: Método `actualizarPuntos(usuarioId, reservaId)` que suma puntos por reserva completada
  - Líneas 72-83: Método `cargarLogros(usuarioId)` que carga los logros del usuario
  - Líneas 130-154: Método `verificarYDesbloquearLogros(usuarioId)` que verifica y desbloquea logros según criterios

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Líneas 774-808: Métodos relacionados con puntos y logros:
    - `sumarPuntosPorReserva(usuarioId, reservaId)`
    - `obtenerPuntosUsuario(usuarioId)`
    - `obtenerLogrosUsuario(usuarioId)`
    - `desbloquearLogro(usuarioId, codigoLogro)`
    - `verificarYDesbloquearLogros(usuarioId)`

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Líneas 2058-2119: Métodos para gestionar puntos y logros en la base de datos

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/PuntosUsuario.kt`** (Líneas 1-51)
  - Data class con información de puntos del usuario (usuarioId, puntosAcumulados, nivel, puntosParaSiguienteNivel)
  - Líneas 24-31: Método `calcularNivel(puntos)` que calcula el nivel según puntos
  - Líneas 36-43: Método `calcularPuntosParaSiguienteNivel(puntos)`
  - Línea 48: Constante `PUNTOS_POR_RESERVA = 200`

- **`app/src/main/java/com/grupo4/appreservas/modelos/Logro.kt`** (Líneas 1-61)
  - Data class con información del logro (id, nombre, descripcion, icono, puntosRequeridos, tipo, criterio, fechaDesbloqueo, desbloqueado)
  - Enum `TipoLogro` (PRIMER_VIAJE, VIAJERO_FRECUENTE, EXPLORADOR_SEMANA, etc.)
  - Enum `TipoCriterio` (TOURS_COMPLETADOS, PUNTOS_ACUMULADOS, etc.)

#### **Adapters**
- **`app/src/main/java/com/grupo4/appreservas/adapter/LogrosAdapter.kt`**
  - Adaptador para mostrar la lista de logros en el RecyclerView

- **`app/src/main/java/com/grupo4/appreservas/adapter/HistorialViajesAdapter.kt`**
  - Adaptador para mostrar el historial de viajes/reservas

#### **Layouts XML**
- **`app/src/main/res/layout/activity_recompensas.xml`**
  - Layout completo de la pantalla de recompensas con información de puntos, nivel, logros e historial

- **`app/src/main/res/layout/item_logro.xml`**
  - Layout del item individual de logro en el RecyclerView

- **`app/src/main/res/layout/item_historial_viaje.xml`**
  - Layout del item individual de viaje en el historial

#### **AndroidManifest.xml**
- Líneas 106-110: Declaración de `RecompensasActivity`

---

## HU-008: Álbum de Fotos Grupal

**Descripción:** Como turista, deseo subir fotos al álbum grupal del tour y ver las que compartan otros viajeros.

### Escenario 1: Subida de fotos
**Flujo:** El turista finaliza su tour → Selecciona opción "Subir fotos" → La app permite elegir imágenes y subirlas al álbum grupal.

### Escenario 2: Visualización de álbum
**Flujo:** El turista entra al álbum del tour → La app carga las fotos compartidas → Se muestran todas las imágenes aprobadas del grupo.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/AlbumTourActivity.kt`** (Líneas 1-151)
  - Líneas 21-57: Clase principal y configuración inicial
  - Líneas 59-70: Inicialización de vistas
  - Líneas 72-79: Configuración del RecyclerView con GridLayoutManager
  - Líneas 81-89: Configuración de listeners (volver, subir fotos)
  - Líneas 91-101: Observadores del ViewModel
  - Líneas 107-113: Carga del álbum del tour
  - Líneas 119-122: Mostrar fotos en el RecyclerView
  - Líneas 128-134: Navegación a SubirFotosActivity
  - Líneas 144-148: Recargar fotos cuando se vuelve a la actividad

- **`app/src/main/java/com/grupo4/appreservas/ui/SubirFotosActivity.kt`** (Líneas 1-233)
  - Líneas 24-64: Clase principal y configuración inicial
  - Líneas 66-78: Inicialización de vistas
  - Líneas 80-91: Configuración del RecyclerView para fotos seleccionadas
  - Líneas 93-109: Configuración de listeners (volver, elegir fotos, subir)
  - Líneas 111-120: Observadores del ViewModel
  - Líneas 126-131: Mostrar pantalla de confirmación después de subir
  - Líneas 137-151: Selección de fotos desde la galería
  - Líneas 153-172: Manejo del resultado de selección de imágenes
  - Líneas 174-184: Actualización de fotos seleccionadas
  - Líneas 200-212: Envío de fotos seleccionadas al ViewModel
  - Líneas 218-222: Mostrar progreso de subida

#### **ViewModel**
- **`app/src/main/java/com/grupo4/appreservas/viewmodel/AlbumTourViewModel.kt`** (Líneas 1-77)
  - Líneas 19-27: Propiedades LiveData para fotos del álbum y mensajes de estado
  - Líneas 33-46: Método `cargarFotosAlbum(idTour)` que carga las fotos del álbum
  - Líneas 56-77: Método `subirFotosSeleccionadas(idTour, rutasImagenes, nombreAutor)` que sube las fotos al álbum

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Métodos relacionados con fotos:
    - `obtenerFotosPorTour(tourId)`
    - `subirFoto(foto)`

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Métodos para insertar y consultar fotos en la base de datos:
    - `insertarFoto(foto)`
    - `obtenerFotosPorTour(tourId)`

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/Foto.kt`**
  - Data class con información de la foto (idFoto, idTour, urlImagen, nombreAutor, fechaSubida, aprobada)

#### **Adapters**
- **`app/src/main/java/com/grupo4/appreservas/adapter/FotosAdapter.kt`**
  - Adaptador para mostrar las fotos del álbum en un GridLayout

- **`app/src/main/java/com/grupo4/appreservas/adapter/FotosSeleccionadasAdapter.kt`**
  - Adaptador para mostrar las fotos seleccionadas antes de subirlas

#### **Layouts XML**
- **`app/src/main/res/layout/activity_album_tour.xml`**
  - Layout completo del álbum del tour con RecyclerView en grid y botón de subir fotos

- **`app/src/main/res/layout/activity_subir_fotos.xml`**
  - Layout completo de la pantalla de subida de fotos con RecyclerView de fotos seleccionadas y botones de elegir y subir

- **`app/src/main/res/layout/item_foto.xml`**
  - Layout del item individual de foto en el álbum

- **`app/src/main/res/layout/item_foto_seleccionada.xml`**
  - Layout del item individual de foto seleccionada antes de subir

#### **AndroidManifest.xml**
- Líneas 7-8: Permisos de lectura de almacenamiento (`READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`)
- Líneas 112-116: Declaración de `AlbumTourActivity`
- Líneas 118-123: Declaración de `SubirFotosActivity`

#### **Tests**
- **`app/src/test/java/com/grupo4/appreservas/integracion/IntegracionAlbumFotosTest.kt`** (Líneas 1-317)
  - Pruebas de integración para HU-008:
    - Líneas 93-138: Test de subida de fotos al álbum grupal
    - Líneas 140-198: Test de visualización de álbum con fotos aprobadas
    - Líneas 200-224: Test de álbum vacío
    - Líneas 226-261: Test de guardar múltiples fotos
    - Líneas 263-314: Test de fotos de diferentes tours no se mezclan

---

## HU-009: Encuestas de Satisfacción

**Descripción:** Como turista, deseo responder una encuesta después de mi tour.

### Escenario 1: Envío automático de encuesta
**Flujo:** El tour ha finalizado → El sistema envía encuesta al turista → El turista recibe notificación con enlace o formulario.

### Escenario 2: Registro de respuesta
**Flujo:** El turista completa la encuesta → Envía su calificación y comentario → La app registra la respuesta y genera una métrica de satisfacción.

### Archivos y Código Asociado:

#### **UI (Activities)**
- **`app/src/main/java/com/grupo4/appreservas/ui/EncuestaActivity.kt`** (Líneas 1-246)
  - Líneas 23-59: Clase principal y configuración inicial
  - Líneas 61-88: Inicialización de vistas (estrellas, comentario, contador de caracteres)
  - Líneas 90-113: Configuración de listeners (cerrar, responder más tarde, estrellas, enviar)
  - Líneas 115-140: Observadores del ViewModel
  - Líneas 142-144: Carga de la encuesta
  - Líneas 150-152: Solicitar formulario de encuesta
  - Líneas 158-172: Mostrar formulario con información del tour
  - Líneas 177-199: Selección de calificación (1-5 estrellas)
  - Líneas 205-212: Envío de respuesta de la encuesta
  - Líneas 218-221: Mostrar pantalla de encuesta completada
  - Líneas 227-235: Mostrar confirmación con puntos ganados

#### **ViewModel**
- **`app/src/main/java/com/grupo4/appreservas/viewmodel/EncuestaViewModel.kt`** (Líneas 1-38)
  - Propiedades LiveData para tour, mensaje de estado, respuesta de encuesta y encuesta enviada
  - Métodos:
    - `cargarEncuesta(idTour)`
    - `registrarRespuesta(idTour, usuarioId, calificacion, comentario)`

#### **Service**
- **`app/src/main/java/com/grupo4/appreservas/service/NotificacionesService.kt`**
  - Líneas 47-59: Manejo especial de notificaciones de encuesta que abren directamente EncuestaActivity

#### **Worker**
- **`app/src/main/java/com/grupo4/appreservas/worker/NotificacionesWorker.kt`**
  - Líneas 39-40: Llamada a `generarEncuestasAutomaticas(usuarioId)` en el método `doWork()`
  - Líneas 193-248: Método `generarEncuestasAutomaticas(usuarioId)` que envía encuestas automáticas para tours finalizados

#### **Repository**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Líneas 930-953: Método `enviarEncuestaAutomatica(idTour, usuarioId): Boolean` que envía la encuesta automática
  - Líneas 955-1000: Método `guardarRespuestaEncuesta(idTour, usuarioId, calificacion, comentario): EncuestaRespuesta?` que guarda la respuesta
  - Métodos relacionados:
    - `crearNotificacionEncuesta(usuarioId, idTour, nombreTour)`
    - `yaRespondioEncuesta(idTour, usuarioId)`

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Líneas 2344-2408: Métodos para gestionar encuestas en la base de datos:
    - `insertarEncuestaRespuesta(encuestaRespuesta)`
    - `existeEncuestaRespuesta(idTour, usuarioId)`
    - `obtenerEncuestasRespuestaPorTour(idTour)`

#### **Modelos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/EncuestaRespuesta.kt`**
  - Data class con información de la respuesta de encuesta (id, tourId, usuarioId, calificacion, comentario, fechaRespuesta, puntosOtorgados)

#### **Layouts XML**
- **`app/src/main/res/layout/activity_encuesta.xml`**
  - Layout completo de la pantalla de encuesta con estrellas de calificación, campo de comentario y botones de enviar/responder más tarde

#### **AndroidManifest.xml**
- Líneas 125-129: Declaración de `EncuestaActivity`

#### **Tests**
- **`app/src/test/java/com/grupo4/appreservas/integracion/IntegracionEncuestaTest.kt`**
  - Pruebas de integración para HU-009

---

## Código Compartido entre Múltiples HUs

### **Repository Compartido**
- **`app/src/main/java/com/grupo4/appreservas/repository/PeruvianServiceRepository.kt`**
  - Este archivo contiene métodos utilizados por múltiples HUs:
    - Métodos de autenticación (HU-004)
    - Métodos de reservas (HU-002)
    - Métodos de pagos (HU-003)
    - Métodos de notificaciones (HU-006)
    - Métodos de puntos y logros (HU-007)
    - Métodos de fotos (HU-008)
    - Métodos de encuestas (HU-009)

- **`app/src/main/java/com/grupo4/appreservas/repository/DatabaseHelper.kt`**
  - Este archivo contiene todos los métodos de acceso a la base de datos SQLite:
    - Métodos de usuarios (HU-004)
    - Métodos de destinos (HU-001)
    - Métodos de reservas (HU-002, HU-003)
    - Métodos de pagos (HU-003)
    - Métodos de check-ins (HU-005)
    - Métodos de notificaciones (HU-006)
    - Métodos de puntos y logros (HU-007)
    - Métodos de fotos (HU-008)
    - Métodos de encuestas (HU-009)

### **Modelos Compartidos**
- **`app/src/main/java/com/grupo4/appreservas/modelos/Usuario.kt`**
  - Utilizado en: HU-004, HU-007, HU-008

- **`app/src/main/java/com/grupo4/appreservas/modelos/Destino.kt`**
  - Utilizado en: HU-001, HU-002, HU-003

- **`app/src/main/java/com/grupo4/appreservas/modelos/Reserva.kt`**
  - Utilizado en: HU-002, HU-003, HU-005, HU-007, HU-009

- **`app/src/main/java/com/grupo4/appreservas/modelos/Tour.kt`**
  - Utilizado en: HU-005, HU-006, HU-008, HU-009

### **UI Compartida**
- **`app/src/main/java/com/grupo4/appreservas/ui/PanelPrincipalActivity.kt`**
  - Utilizado en: HU-004 (redirige según rol después de login/registro)

- **`app/src/main/java/com/grupo4/appreservas/ui/CatalogoActivity.kt`**
  - Utilizado en: HU-001 (catálogo principal), HU-006 (badge de notificaciones), HU-007 (navegación desde recompensas)

### **Layouts XML Compartidos**
- **`app/src/main/res/layout/activity_panel_principal.xml`**
  - Utilizado en: HU-004

- **`app/src/main/res/values/strings.xml`**
  - Strings compartidos utilizados en todas las HUs

- **`app/src/main/res/values/colors.xml`**
  - Colores compartidos utilizados en todas las HUs

- **`app/src/main/res/values/themes.xml`**
  - Temas compartidos utilizados en todas las HUs

### **AndroidManifest.xml**
- **`app/src/main/AndroidManifest.xml`** (Líneas 1-134)
  - Declaraciones de todas las Activities y permisos:
    - Líneas 5-10: Permisos compartidos (INTERNET, CAMERA, READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES)
    - Líneas 22-131: Declaraciones de todas las Activities del proyecto

---

## Resumen de Archivos por HU

### HU-001 (Catálogo de Destinos)
- **Kotlin:** 6 archivos principales
- **XML:** 3 layouts principales
- **Total:** ~9 archivos

### HU-002 (Reserva de Tours)
- **Kotlin:** 4 archivos principales
- **XML:** 3 layouts principales
- **Total:** ~7 archivos

### HU-003 (Pago de Reservas)
- **Kotlin:** 6 archivos principales
- **XML:** 4 layouts + 3 drawables
- **Total:** ~13 archivos

### HU-004 (Registro e Inicio de Sesión)
- **Kotlin:** 4 archivos principales
- **XML:** 3 layouts principales
- **Total:** ~7 archivos

### HU-005 (Escaneo QR)
- **Kotlin:** 3 archivos principales
- **XML:** 1 layout principal
- **Total:** ~4 archivos

### HU-006 (Notificaciones)
- **Kotlin:** 5 archivos principales
- **XML:** 2 layouts + 2 drawables
- **Total:** ~9 archivos

### HU-007 (Puntos y Logros)
- **Kotlin:** 4 archivos principales
- **XML:** 3 layouts principales
- **Total:** ~7 archivos

### HU-008 (Álbum de Fotos)
- **Kotlin:** 4 archivos principales + 1 test
- **XML:** 4 layouts principales
- **Total:** ~9 archivos

### HU-009 (Encuestas)
- **Kotlin:** 4 archivos principales + 1 test
- **XML:** 1 layout principal
- **Total:** ~6 archivos

---

**Nota:** Este documento refleja el estado actual del código del proyecto. Algunos archivos pueden ser compartidos entre múltiples HUs, especialmente los relacionados con modelos de datos, repositorios y componentes de UI comunes.

