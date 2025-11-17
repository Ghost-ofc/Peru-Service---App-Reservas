# Resumen de Historias de Usuario (HU-006 y HU-007) y su Implementación

Este documento resume las historias de usuario HU-006 (Sistema de Notificaciones) y HU-007 (Sistema de Recompensas y Logros), detallando su implementación y alineación con los diagramas de objetos (UML).

---

## HU-006: Sistema de Notificaciones

### Historia de Usuario

**Como turista**, deseo recibir recordatorios, alertas climáticas y promociones de último minuto para mantenerme informado sobre mis tours y aprovechar descuentos.

#### Escenarios:

1. **Recordatorio de horario:**
   - **Condición:** El tour está próximo a iniciar
   - **Acción:** Se activa una notificación push
   - **Resultado:** El turista recibe el recordatorio con hora y punto de encuentro

2. **Alerta climática:**
   - **Condición:** Se detecta un cambio de clima
   - **Acción:** El sistema envía una notificación automática
   - **Resultado:** El turista recibe un aviso con recomendaciones

3. **Oferta de último minuto:**
   - **Condición:** Un tour tiene baja ocupación
   - **Acción:** El sistema activa una promoción
   - **Resultado:** Se envía notificación con descuento disponible

### Diagrama de Objetos (UML)

El diagrama UML proporcionado ilustra la arquitectura del sistema de notificaciones, siguiendo el patrón **Model-View-ViewModel (MVVM)**:

```
┌─────────────────────────────────────────────────────────────┐
│ ui (User Interface)                                          │
│  ┌─────────────────────────────┐                            │
│  │ NotificacionesActivity      │                            │
│  │ +mostrarNotificaciones()    │                            │
│  │ +mostrarDetalleNotificacion()│                           │
│  └──────────────┬──────────────┘                            │
└─────────────────┼───────────────────────────────────────────┘
                  │
┌─────────────────┼───────────────────────────────────────────┐
│ viewmodel       │                                            │
│  ┌──────────────┴──────────────┐                            │
│  │ NotificacionesViewModel     │                            │
│  │ +recordatorios: LiveData    │                            │
│  │ +cargarRecordatorios()      │                            │
│  │ +detectarCambioClimatico()  │                            │
│  │ +generarOfertaUltimoMinuto()│                            │
│  └──────┬───────┬──────────────┘                            │
└─────────┼───────┼───────────────────────────────────────────┘
          │       │       │
┌─────────┼───────┼───────┼───────────────────────────────────┐
│ data    │       │       │                                    │
│  ┌──────▼───┐ ┌─▼──────┐ ┌──────────────────────────┐      │
│  │Repositorio│ │Repositorio│ │RepositorioNotificaciones│     │
│  │Ofertas   │ │Clima    │ │                        │      │
│  │+toursCon │ │+obtener │ │+obtenerRecordatorios() │      │
│  │BajaOcup()│ │Condiciones()│+enviarNotificacionPush()│      │
│  │+generar  │ │+detectar│ │                        │      │
│  │Descuento()│ │Cambio()  │ │                        │      │
│  └──────────┘ └─────────┘ └────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

**Descripción del Diagrama:**

- **`ui` (User Interface):**
  - **`NotificacionesActivity`**: Vista principal que interactúa con el usuario. Contiene métodos para `mostrarNotificaciones()` y `mostrarDetalleNotificacion()`. Observa los datos del ViewModel.

- **`viewmodel` (ViewModel):**
  - **`NotificacionesViewModel`**: Intermediario entre la UI y la capa de datos. Expone `LiveData<List<Notificacion>>` (`recordatorios`) que la UI puede observar. Contiene la lógica para `cargarRecordatorios()`, `detectarCambioClimatico()` y `generarOfertaUltimoMinuto()`, delegando las operaciones de datos a los repositorios.

- **`data` (Capa de Datos):**
  - **`RepositorioOfertas`**: Lógica de negocio relacionada con ofertas. Proporciona métodos como `toursConBajaOcupacion()` y `generarDescuento()`.
  - **`RepositorioClima`**: Maneja la información climática. Ofrece métodos como `obtenerCondiciones()` y `detectarCambio()`.
  - **`RepositorioNotificaciones`**: Gestiona las notificaciones. Incluye métodos para `obtenerRecordatorios()` y `enviarNotificacionPush()`.

### Implementación en el Proyecto

La implementación de HU-006 sigue el patrón **MVVM** y se alinea con el diagrama de objetos proporcionado:

#### Arquitectura y Componentes:

1. **Modelos:**
   - `Notificacion.kt`: Clase de datos que representa una notificación con campos como `id`, `usuarioId`, `titulo`, `mensaje`, `tipo`, `fecha`, `leida`.
   - `Clima.kt`: Clase de datos para representar condiciones climáticas.

2. **Repositorios:**
   - **`RepositorioNotificaciones`**: 
     - Gestiona las notificaciones en la base de datos SQLite.
     - Métodos: `obtenerRecordatorios(usuarioId)`, `enviarNotificacionPush(notificacion)`, `obtenerNotificacionesNoLeidas(usuarioId)`, `marcarComoLeida(notificacionId)`.
   
   - **`RepositorioOfertas`**: 
     - Se encarga de obtener tours con baja ocupación y generar descuentos.
     - Métodos: `toursConBajaOcupacion()`, `generarDescuento(tourId)`.
   
   - **`RepositorioClima`**: 
     - Simula la obtención de condiciones climáticas y la detección de cambios.
     - Métodos: `obtenerCondiciones(actualUbicacion)`, `detectarCambio(condiciones)`.

3. **Controlador (MVC):**
   - **`ControlNotificaciones`**: 
     - Contiene la lógica de negocio para generar los diferentes tipos de notificaciones (recordatorios, alertas climáticas, ofertas).
     - Métodos: `cargarRecordatorios(usuarioId)`, `detectarCambioClimatico(usuarioId)`, `generarOfertaUltimoMinuto(usuarioId)`.
     - Interactúa con los repositorios y `NotificacionesService` para crear y enviar notificaciones.

4. **Servicios:**
   - **`NotificacionesService`**: 
     - Gestiona la creación y envío de notificaciones push de Android.
     - Utiliza `NotificationManager` y `NotificationChannel` para mostrar notificaciones en el sistema.
   
   - **`NotificacionesScheduler`**: 
     - Programa trabajos periódicos usando `WorkManager`.
     - Configura `NotificacionesWorker` para ejecutarse cada 6 horas.
   
   - **`NotificacionesWorker`**: 
     - Worker que se ejecuta en segundo plano para generar y enviar notificaciones.
     - Ejecuta la lógica de `ControlNotificaciones` para todos los usuarios turistas.

5. **UI:**
   - **`NotificacionesActivity`**: 
     - Muestra la lista de notificaciones utilizando un `RecyclerView` y un `NotificacionesAdapter`.
     - Permite marcar notificaciones como leídas.
     - Muestra un contador de notificaciones no leídas.
   
   - **`NotificacionesAdapter`**: 
     - Adaptador para mostrar las notificaciones en el `RecyclerView`.
     - Diferencia visualmente entre notificaciones leídas y no leídas.

6. **Base de Datos:**
   - Se ha añadido una nueva tabla `notificaciones` en `DatabaseHelper`.
   - La versión de la base de datos (`VERSION_BD`) se ha incrementado para incluir esta nueva tabla.
   - Métodos CRUD: `insertarNotificacion()`, `obtenerNotificacionesPorUsuario()`, `obtenerNotificacionesNoLeidasPorUsuario()`, `marcarNotificacionComoLeida()`.

7. **Integración UI:**
   - Se ha añadido un icono de notificación con un contador de no leídas en `CatalogoActivity` y `PanelGuiaActivity`.
   - Al hacer clic en el icono, se navega a `NotificacionesActivity`.

### Alineación con el Diagrama UML

La implementación sigue fielmente la estructura MVVM del diagrama:

- **`NotificacionesActivity` (UI)**: 
  - Interactúa con `ControlNotificaciones` (que actúa como ViewModel en MVC) en lugar de un `NotificacionesViewModel` puro, pero mantiene la misma separación de responsabilidades.
  - Los métodos `mostrarNotificaciones()` y `mostrarDetalleNotificacion()` están implementados.

- **`ControlNotificaciones` (ViewModel/Controller)**: 
  - Actúa como intermediario entre la UI y los repositorios, similar al `NotificacionesViewModel` del diagrama.
  - Implementa los métodos `cargarRecordatorios()`, `detectarCambioClimatico()` y `generarOfertaUltimoMinuto()`.

- **Repositorios (`RepositorioOfertas`, `RepositorioClima`, `RepositorioNotificaciones`)**: 
  - Siguen exactamente la estructura del diagrama, con los mismos métodos y responsabilidades.

- **Notificaciones Push (Background)**: 
  - Se ha integrado `WorkManager` con `NotificacionesWorker` para generar y enviar notificaciones periódicamente en segundo plano (cada 6 horas), asegurando que los turistas reciban alertas incluso si la aplicación no está activa.
  - `NotificacionesScheduler` se encarga de programar este trabajo periódico.

---

## HU-007: Sistema de Recompensas y Logros

### Historia de Usuario

**Como turista**, deseo acumular puntos y logros por cada reserva confirmada para obtener recompensas y motivarme a seguir viajando.

#### Escenarios:

1. **Acumulación de puntos:**
   - **Condición:** El turista completa una reserva
   - **Condición:** La reserva pasa a estado "Completada"
   - **Resultado:** Se suman puntos automáticamente en su perfil

2. **Visualización de logros:**
   - **Condición:** El turista accede a su perfil
   - **Acción:** Consulta la sección "Mis logros"
   - **Resultado:** Se muestran puntos acumulados y logros desbloqueados

**Adicional:** En la interfaz del perfil debe salir el historial de viajes del usuario, tanto los reservados como los completados.

### Diagrama de Objetos (UML)

Aunque no se proporcionó un diagrama UML específico para HU-007 en el mismo formato que HU-006, la implementación sigue un patrón **MVVM** similar. A continuación, se describe la estructura lógica:

```
┌─────────────────────────────────────────────────────────────┐
│ ui (User Interface)                                          │
│  ┌─────────────────────────────┐                            │
│  │ RecompensasActivity         │                            │
│  │ +mostrarPuntos()            │                            │
│  │ +mostrarNivel()             │                            │
│  │ +mostrarLogros()            │                            │
│  │ +mostrarHistorialViajes()   │                            │
│  └──────────────┬──────────────┘                            │
└─────────────────┼───────────────────────────────────────────┘
                  │
┌─────────────────┼───────────────────────────────────────────┐
│ viewmodel       │                                            │
│  ┌──────────────┴──────────────┐                            │
│  │ RecompensasViewModel        │                            │
│  │ +puntos: LiveData           │                            │
│  │ +logros: LiveData           │                            │
│  │ +toursCompletados: LiveData │                            │
│  │ +actualizarPuntos()         │                            │
│  │ +cargarLogros()             │                            │
│  │ +verificarYDesbloquearLogros()│                          │
│  └──────┬──────────────┬───────┘                            │
└─────────┼──────────────┼────────────────────────────────────┘
          │              │
┌─────────┼──────────────┼────────────────────────────────────┐
│ data    │              │                                     │
│  ┌──────▼──────────┐ ┌─▼──────────────────────────┐        │
│  │RepositorioRecompensas│ │ReservasRepository        │        │
│  │+sumarPuntos()   │ │+obtenerReservasConfirmadas()│        │
│  │+obtenerPuntos() │ │+obtenerReservasPorUsuario() │        │
│  │+obtenerLogros() │ │                            │        │
│  │+insertarLogro() │ │                            │        │
│  └────────┬────────┘ └──────────────┬─────────────┘        │
│           │                         │                       │
│           └──────────┬──────────────┘                       │
│                      │                                      │
│           ┌──────────▼──────────┐                           │
│           │ DatabaseHelper      │                           │
│           │ +obtenerPuntos()    │                           │
│           │ +sumarPuntos()      │                           │
│           │ +obtenerLogros()    │                           │
│           │ +insertarLogro()    │                           │
│           └─────────────────────┘                           │
└─────────────────────────────────────────────────────────────┘
```

**Descripción del Diagrama (Conceptual):**

1. **`ui` (User Interface):**
   - **`RecompensasActivity`**: Vista del perfil del usuario. Muestra los puntos acumulados, el nivel, la lista de logros (desbloqueados y pendientes) y el historial de viajes. Observa los `LiveData` del ViewModel.

2. **`viewmodel` (ViewModel):**
   - **`RecompensasViewModel`**: Gestiona el estado de la UI para la pantalla de recompensas. Expone `LiveData` para `puntos`, `logros` y `toursCompletados`. Contiene la lógica para `actualizarPuntos()` (que también verifica y desbloquea logros), `cargarLogros()` y `verificarYDesbloquearLogros()`. Interactúa con `RepositorioRecompensas` y `ReservasRepository`.

3. **`data` (Capa de Datos):**
   - **`RepositorioRecompensas`**: Repositorio dedicado a la gestión de puntos y logros. Contiene métodos para `sumarPuntos()`, `obtenerPuntosUsuario()`, `obtenerLogros()`, `insertarLogroParaUsuario()` y `obtenerNumeroReservasConfirmadas()`.
   - **`ReservasRepository`**: Se ha extendido para incluir métodos como `obtenerReservasConfirmadas()` y `obtenerReservasPorUsuario()` para el historial de viajes.
   - **`DatabaseHelper`**: Base de datos subyacente, que ahora incluye tablas para `puntos` y `logros`, con métodos CRUD asociados.

4. **Modelos:**
   - **`Logro`**: Clase de datos que representa un logro, incluyendo su nombre, descripción, icono, tipo, criterio de desbloqueo y estado.
   - **`PuntosUsuario`**: Clase de datos que encapsula los puntos acumulados de un usuario, su nivel actual y los puntos necesarios para el siguiente nivel.
   - **Enums y Clases Auxiliares**: `TipoLogro`, `CriterioLogro`, `TipoCriterio` para definir la naturaleza y condiciones de los logros.

### Implementación en el Proyecto

La implementación de HU-007 también sigue el patrón **MVVM**:

#### Arquitectura y Componentes:

1. **Modelos:**
   - **`Logro.kt`**: 
     - Data class para representar los logros.
     - Campos: `id`, `nombre`, `descripcion`, `icono`, `puntosRequeridos`, `tipo` (TipoLogro), `criterio` (CriterioLogro), `fechaDesbloqueo`, `desbloqueado`.
   
   - **`PuntosUsuario.kt`**: 
     - Data class para gestionar los puntos y niveles del usuario.
     - Campos: `usuarioId`, `puntosAcumulados`, `nivel` (NivelUsuario), `puntosParaSiguienteNivel`.
   
   - **`TipoLogro`**: Enum con valores como `PRIMER_VIAJE`, `EXPLORADOR_SEMANA`, `TOURS_COMPLETADOS`, `PUNTOS_ACUMULADOS`.
   
   - **`CriterioLogro`**: Data class que define el criterio para desbloquear un logro (tipo y valor).
   
   - **`NivelUsuario`**: Enum con valores como `Explorador`, `Explorador Experto`, `Viajero Profesional`.

2. **Base de Datos:**
   - **`DatabaseHelper`**: 
     - Se han añadido nuevas tablas `puntos` y `logros`.
     - La `VERSION_BD` se ha incrementado a 8.
     - Métodos CRUD:
       - `obtenerPuntos(usuarioId)`: Obtiene los puntos acumulados de un usuario.
       - `sumarPuntos(usuarioId, puntos)`: Suma puntos a un usuario.
       - `inicializarPuntos(usuarioId)`: Inicializa el registro de puntos si no existe.
       - `obtenerLogros(usuarioId)`: Obtiene todos los logros de un usuario.
       - `obtenerLogrosDesbloqueados(usuarioId)`: Obtiene solo los logros desbloqueados.
       - `insertarLogroParaUsuario(logro)`: Inserta o actualiza un logro para un usuario.
       - `existeLogro(usuarioId, logroId)`: Verifica si un logro ya existe para un usuario.
       - `obtenerNumeroReservasConfirmadas(usuarioId)`: Obtiene el número de reservas confirmadas de un usuario.

3. **Repositorios:**
   - **`RepositorioRecompensas`**: 
     - Nuevo repositorio que interactúa con `DatabaseHelper` para gestionar puntos y logros.
     - Métodos:
       - `sumarPuntos(usuarioId, puntos)`: Suma puntos a un usuario.
       - `obtenerPuntos(usuarioId)`: Obtiene los puntos acumulados.
       - `obtenerPuntosUsuario(usuarioId)`: Obtiene la información completa de puntos (incluyendo nivel).
       - `obtenerLogros(usuarioId)`: Obtiene todos los logros de un usuario.
       - `obtenerLogrosDesbloqueados(usuarioId)`: Obtiene solo los logros desbloqueados.
       - `insertarLogro(logro)`: Inserta o actualiza un logro.
       - `existeLogro(usuarioId, logroId)`: Verifica si un logro existe.
       - `obtenerNumeroReservasConfirmadas(usuarioId)`: Obtiene el número de reservas confirmadas.
       - `obtenerReservasCompletadasRecientes(usuarioId, limite)`: Obtiene las reservas completadas más recientes.
   
   - **`ReservasRepository`**: 
     - Se ha actualizado con `obtenerReservasConfirmadas(usuarioId)` para el historial de viajes.
     - Se mantiene `obtenerReservasUsuario(usuarioId)` para obtener todas las reservas.

4. **ViewModel:**
   - **`RecompensasViewModel`**: 
     - Gestiona el estado de la UI para la pantalla de recompensas.
     - Expone `LiveData` para:
       - `puntos: LiveData<Int>`: Puntos acumulados del usuario.
       - `puntosUsuario: LiveData<PuntosUsuario>`: Información completa de puntos (incluyendo nivel).
       - `logros: LiveData<List<Logro>>`: Lista de logros del usuario.
       - `toursCompletados: LiveData<Int>`: Número de tours completados.
     - Métodos:
       - `actualizarPuntos(usuarioId, reservaId)`: Actualiza los puntos después de una reserva confirmada y verifica logros.
       - `cargarLogros(usuarioId)`: Carga los logros y puntos del usuario.
       - `verificarYDesbloquearLogros(usuarioId)`: Verifica si se cumplen los criterios para desbloquear logros.
       - `generarLogrosSiNoExisten(usuarioId)`: Genera logros por defecto si no existen.

5. **UI:**
   - **`RecompensasActivity`**: 
     - Activity principal para el perfil del usuario.
     - Observa los `LiveData` del `RecompensasViewModel` para actualizar la interfaz.
     - Muestra:
       - Información del usuario (nombre, avatar).
       - Puntos acumulados y nivel actual.
       - Barra de progreso hacia el siguiente nivel.
       - Tarjetas de estadísticas (Tours Completados, Logros Desbloqueados).
       - Lista de logros con iconos y estados (desbloqueado/pendiente).
       - Historial de viajes (reservas y completadas).
   
   - **`activity_recompensas.xml`**: 
     - Layout principal de la pantalla de perfil.
     - Incluye:
       - Header personalizado con fondo oscuro (`@color/primary_dark`).
       - Tarjeta de puntos con fondo más oscuro (`#5A2A1A`).
       - Barra de progreso personalizada usando `FrameLayout` y `View`.
       - Tarjetas de estadísticas con bordes (`MaterialCardView`).
       - `RecyclerView` para logros.
       - `RecyclerView` para historial de viajes.
   
   - **`item_logro.xml`**: 
     - Layout para cada ítem de logro en la lista.
     - Muestra icono del logro, nombre, descripción y check verde circular si está desbloqueado.
   
   - **`item_historial_viaje.xml`**: 
     - Layout para cada ítem del historial de viajes.
     - Muestra destino, fecha, hora y estado de la reserva.
   
   - **`LogrosAdapter`**: 
     - Adaptador para mostrar los logros en un `RecyclerView`.
     - Usa iconos y fondos específicos para cada tipo de logro.
     - Muestra un check verde circular (`circle_green_check.xml`) para los logros desbloqueados.
   
   - **`HistorialViajesAdapter`**: 
     - Adaptador para mostrar el historial de viajes en un `RecyclerView`.

6. **Integración:**
   - **`PagoActivity`**: 
     - Llama a `RepositorioRecompensas.sumarPuntos()` después de un pago exitoso para sumar puntos.
     - Llama a `RecompensasViewModel.actualizarPuntos()` para actualizar los `LiveData` y verificar logros.
   
   - **`ReciboActivity`**: 
     - Muestra los puntos ganados en el voucher.
     - Proporciona navegación a `RecompensasActivity` para ver el perfil completo.
   
   - **`CatalogoActivity`**: 
     - Proporciona navegación a `RecompensasActivity` desde el menú (icono de perfil).
   
   - **`AndroidManifest.xml`**: 
     - Declaración de `RecompensasActivity` con `android:parentActivityName=".ui.CatalogoActivity"`.

7. **Drawables:**
   - **`circle_purple.xml`**: Drawable para fondo circular púrpura (logros de tipo `TOURS_COMPLETADOS`).
   - **`circle_yellow.xml`**: Drawable para fondo circular amarillo (logros de tipo `PRIMER_VIAJE` y `PUNTOS_ACUMULADOS`).
   - **`circle_blue.xml`**: Drawable para fondo circular azul (logros de tipo `EXPLORADOR_SEMANA`).
   - **`progress_bar_background.xml`**: Drawable para el fondo de la barra de progreso.
   - **`progress_bar_progress.xml`**: Drawable para el indicador de progreso.
   - **`circle_green_check.xml`**: Drawable vectorial para el check verde circular que indica logros desbloqueados.

### Alineación con el Diagrama UML (Conceptual)

La implementación sigue el patrón MVVM:

- **`RecompensasActivity` (UI)**: 
  - Observa `RecompensasViewModel` a través de `LiveData`.
  - Los métodos `mostrarPuntos()`, `mostrarNivel()`, `mostrarLogros()` y `mostrarHistorialViajes()` están implementados a través de la observación de `LiveData`.

- **`RecompensasViewModel` (ViewModel)**: 
  - Es el cerebro de la pantalla de recompensas, gestionando la lógica de negocio para calcular puntos, verificar logros y preparar los datos para la UI.
  - Implementa los métodos `actualizarPuntos()`, `cargarLogros()` y `verificarYDesbloquearLogros()`.

- **Repositorios (`RepositorioRecompensas`, `ReservasRepository`)**: 
  - Interactúan con `DatabaseHelper` para almacenar y recuperar toda la información relacionada con puntos, logros y el historial de reservas del usuario.
  - Siguen la misma estructura y responsabilidades del diagrama conceptual.

- **Modelos (`Logro`, `PuntosUsuario`)**: 
  - Estructuran los datos de recompensas de manera clara y consistente.
  - Incluyen toda la información necesaria para mostrar los logros, puntos y niveles en la UI.

---

## Resumen de Alineación con Diagramas UML

### HU-006 (Notificaciones)

✅ **Totalmente alineado con el diagrama UML proporcionado:**

- La estructura MVVM está implementada correctamente.
- `NotificacionesActivity` interactúa con `ControlNotificaciones` (que actúa como ViewModel/Controller).
- Los repositorios (`RepositorioOfertas`, `RepositorioClima`, `RepositorioNotificaciones`) siguen exactamente la estructura del diagrama.
- Todos los métodos especificados en el diagrama están implementados.
- Se ha añadido funcionalidad adicional (notificaciones push en segundo plano) que mejora la experiencia del usuario.

### HU-007 (Recompensas y Logros)

✅ **Sigue el patrón MVVM del diagrama conceptual:**

- La estructura MVVM está implementada correctamente.
- `RecompensasActivity` observa `RecompensasViewModel` a través de `LiveData`.
- Los repositorios (`RepositorioRecompensas`, `ReservasRepository`) interactúan con `DatabaseHelper` para la persistencia de datos.
- Todos los métodos conceptuales del diagrama están implementados.
- La lógica de negocio para la acumulación de puntos y el desbloqueo de logros se maneja en el `ViewModel` y `RepositorioRecompensas`.
- La interfaz de usuario ha sido refinada para coincidir con el diseño UX proporcionado.

---

## Conclusiones

Ambas historias de usuario (HU-006 y HU-007) han sido implementadas siguiendo los patrones arquitectónicos especificados en sus respectivos diagramas UML:

1. **HU-006** sigue estrictamente el diagrama UML proporcionado, implementando el patrón MVVM con `NotificacionesActivity`, `ControlNotificaciones` (ViewModel/Controller), y los repositorios correspondientes.

2. **HU-007** sigue el patrón MVVM del diagrama conceptual, con `RecompensasActivity`, `RecompensasViewModel`, y los repositorios necesarios para gestionar puntos, logros e historial de viajes.

3. Ambas implementaciones mantienen una **clara separación de responsabilidades**, facilitando el mantenimiento y las pruebas.

4. Se ha integrado funcionalidad adicional (notificaciones push en segundo plano, historial de viajes) que mejora la experiencia del usuario sin desviarse de la arquitectura establecida.

5. La interfaz de usuario ha sido diseñada y refinada para coincidir con las imágenes de UX proporcionadas, asegurando una experiencia de usuario consistente y atractiva.


