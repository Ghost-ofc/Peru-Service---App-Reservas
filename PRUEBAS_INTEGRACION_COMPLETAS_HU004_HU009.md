# Pruebas de Integración Completas - HU-004 a HU-009

## Resumen

Este documento resume todas las pruebas de integración implementadas para las historias de usuario HU-004, HU-005, HU-006, HU-007, HU-008 y HU-009.

---

## HU-004: Registro e Inicio de Sesión

**Archivo:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionAutenticacionTest.kt`

### Escenarios de Prueba:

1. **`test HU-004 Escenario 1 - Registro de usuario sin rol asigna rol turista por defecto`**
   - ✅ Verifica que cuando un usuario se registra sin especificar rol, se le asigna el rol por defecto (turista = 2)
   - ✅ Valida que se crea el usuario correctamente
   - ✅ Verifica que el usuario tiene el rol correcto

2. **`test HU-004 Escenario 2 - Inicio de sesión con credenciales correctas redirige según rol`**
   - ✅ Verifica que un usuario puede iniciar sesión con credenciales válidas
   - ✅ Valida que se identifica el rol del usuario
   - ✅ Verifica que se puede obtener el rol correctamente

3. **`test inicio de sesión con credenciales incorrectas muestra mensaje de error`**
   - ✅ Verifica que se muestra un mensaje de error cuando las credenciales son incorrectas
   - ✅ Valida que no se autentica al usuario
   - ✅ Verifica que `mensajeEstado` contiene el mensaje de error

4. **`test registro con rol específico asigna el rol correcto`**
   - ✅ Verifica que se puede registrar un usuario con un rol específico
   - ✅ Valida que el rol se asigna correctamente

5. **`test contraseña se hashea correctamente con SHA-256`**
   - ✅ Verifica que las contraseñas se hashean correctamente antes de almacenarse
   - ✅ Valida que el hash es diferente de la contraseña original

6. **`test flujo completo de registro e inicio de sesión`**
   - ✅ Verifica el flujo completo desde el registro hasta el inicio de sesión
   - ✅ Valida que un usuario registrado puede iniciar sesión con las mismas credenciales

7. **`test obtenerUsuarioActual retorna usuario autenticado`**
   - ✅ Verifica que `obtenerUsuarioActual()` retorna el usuario autenticado correctamente

**Total de pruebas:** 7

---

## HU-005: Escaneo de QR para Check-in

**Archivo:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionEscaneoQRTest.kt`

### Escenarios de Prueba:

1. **`test HU-005 Escenario 1 - Escaneo de QR válido marca asistencia como confirmada`**
   - ✅ Verifica que cuando se escanea un QR válido, se registra el check-in
   - ✅ Valida que se retorna un objeto `CheckIn` con estado "Confirmado"
   - ✅ Verifica que se actualiza `resultadoCheckin` LiveData

2. **`test HU-005 Escenario 2 - Escaneo de QR inválido muestra mensaje de error`**
   - ✅ Verifica que cuando se escanea un QR inválido, se muestra un mensaje de error
   - ✅ Valida que NO se registra un check-in
   - ✅ Verifica que `mensajeEstado` contiene el mensaje "QR no válido o ya registrado"

3. **`test escaneo de QR ya usado muestra mensaje de error`**
   - ✅ Verifica que cuando se intenta escanear un QR que ya fue usado, se muestra un mensaje de error
   - ✅ Valida que NO se registra otro check-in
   - ✅ Verifica que se detecta correctamente que el QR ya fue usado

4. **`test escaneo de QR que no pertenece al tour muestra mensaje de error`**
   - ✅ Verifica que cuando se escanea un QR que pertenece a otro tour, se muestra un mensaje de error
   - ✅ Valida que NO se registra el check-in
   - ✅ Verifica que `validarCodigoQR` valida correctamente que el QR pertenece al tour

5. **`test cargar tours del día muestra lista de tours asignados al guía`**
   - ✅ Verifica que se pueden cargar los tours del día para un guía
   - ✅ Valida que se retorna la lista correcta de tours

6. **`test flujo completo desde escaneo QR hasta confirmación de asistencia`**
   - ✅ Verifica el flujo completo desde el escaneo del QR hasta la confirmación de asistencia
   - ✅ Valida que se ejecutan todos los pasos correctamente
   - ✅ Verifica que se retorna el `CheckIn` registrado

**Total de pruebas:** 6

---

## HU-006: Sistema de Notificaciones

**Archivo:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionNotificacionesTest.kt`

### Escenarios de Prueba:

1. **`test HU-006 Escenario 1 - Recordatorio de horario se genera correctamente`**
   - ✅ Verifica que se genera un recordatorio cuando un tour está próximo a iniciar
   - ✅ Valida que se crea una notificación de tipo `RECORDATORIO`
   - ✅ Verifica que se envía la notificación push

2. **`test HU-006 Escenario 2 - Alerta climática se genera cuando hay cambio de clima`**
   - ✅ Verifica que se detecta un cambio climático
   - ✅ Valida que se genera una notificación de alerta climática
   - ✅ Verifica el flujo completo de detección de cambios climáticos

3. **`test HU-006 Escenario 3 - Oferta de último minuto se genera para tours con baja ocupación`**
   - ✅ Verifica que se detectan tours con baja ocupación
   - ✅ Valida que se genera una notificación de oferta de último minuto
   - ✅ Verifica que se calcula correctamente el descuento

4. **`test cargar recordatorios devuelve lista de notificaciones`**
   - ✅ Verifica que se pueden cargar las notificaciones de un usuario
   - ✅ Valida que se retornan los tipos correctos de notificaciones

5. **`test flujo completo de notificaciones - desde reserva hasta notificación`**
   - ✅ Verifica el flujo completo desde la creación de una reserva hasta la generación de notificaciones
   - ✅ Valida que se persisten las notificaciones correctamente

6. **`test no se generan notificaciones duplicadas para el mismo tour`**
   - ✅ Verifica que no se crean notificaciones duplicadas
   - ✅ Valida la lógica de prevención de duplicados

**Total de pruebas:** 6

---

## HU-007: Sistema de Recompensas y Logros

**Archivo:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionRecompensasTest.kt`

### Escenarios de Prueba:

1. **`test HU-007 Escenario 1 - Puntos se suman automáticamente al completar reserva`**
   - ✅ Verifica que cuando un turista completa una reserva, se suman puntos automáticamente
   - ✅ Valida que los puntos se acumulan correctamente en su perfil
   - ✅ Verifica que se inicializan los puntos si el usuario no tiene

2. **`test HU-007 Escenario 2 - Logro Primer Viaje se desbloquea al completar primera reserva`**
   - ✅ Verifica que cuando un turista completa su primera reserva, se desbloquea el logro "Primer Viaje"
   - ✅ Valida que el logro se marca como desbloqueado
   - ✅ Verifica que se puede visualizar en el perfil

3. **`test puntos acumulados se muestran correctamente en el perfil`**
   - ✅ Verifica que los puntos acumulados se muestran correctamente
   - ✅ Valida que se calcula el nivel del usuario basado en los puntos
   - ✅ Verifica que se actualiza el LiveData

4. **`test logro de 5 tours se desbloquea al completar 5 reservas`**
   - ✅ Verifica que cuando un turista completa 5 reservas, se desbloquea el logro correspondiente
   - ✅ Valida que se verifica correctamente el criterio de desbloqueo

5. **`test nivel del usuario se calcula correctamente basado en puntos`**
   - ✅ Verifica que el nivel se calcula correctamente según los puntos acumulados
   - ✅ Valida todos los niveles: Explorador, Explorador Experto, Viajero Profesional, Maestro Viajero

6. **`test puntos para siguiente nivel se calculan correctamente`**
   - ✅ Verifica que se calculan correctamente los puntos necesarios para el siguiente nivel
   - ✅ Valida el cálculo para cada nivel

7. **`test flujo completo desde reserva hasta desbloqueo de logro`**
   - ✅ Verifica el flujo completo desde la confirmación de reserva hasta el desbloqueo de logros
   - ✅ Valida que se suman puntos y se desbloquean logros automáticamente

8. **`test logros se generan automáticamente al cargar perfil por primera vez`**
   - ✅ Verifica que los logros base se generan automáticamente cuando un usuario accede a su perfil por primera vez
   - ✅ Valida que se crean todos los logros base

9. **`test obtener logros devuelve lista de logros del usuario`**
   - ✅ Verifica que se pueden obtener los logros del usuario
   - ✅ Valida que se retornan tanto logros desbloqueados como no desbloqueados

**Total de pruebas:** 9

---

## HU-008: Subida y Visualización de Fotos del Álbum Grupal

**Archivo:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionAlbumFotosTest.kt`

### Escenarios de Prueba:

1. **`test HU-008 Escenario 1 - Subida de fotos al álbum grupal`**
   - ✅ Verifica que un turista puede subir fotos después de finalizar su tour
   - ✅ Valida que se permite elegir múltiples imágenes
   - ✅ Verifica que las fotos se suben correctamente al álbum grupal
   - ✅ Valida que las fotos se marcan como aprobadas automáticamente

2. **`test HU-008 Escenario 2 - Visualización de álbum con fotos aprobadas`**
   - ✅ Verifica que un turista puede acceder al álbum del tour
   - ✅ Valida que se cargan las fotos compartidas
   - ✅ Verifica que solo se muestran las fotos aprobadas del grupo
   - ✅ Valida que las fotos no aprobadas no se muestran

3. **`test cargar fotos de álbum vacío muestra lista vacía`**
   - ✅ Verifica que cuando un álbum no tiene fotos, se muestra una lista vacía
   - ✅ Valida que no se producen errores

4. **`test guardar múltiples fotos para un tour`**
   - ✅ Verifica que se pueden subir múltiples fotos en una sola operación
   - ✅ Valida que todas las fotos se guardan correctamente
   - ✅ Verifica que cada foto tiene un ID único

5. **`test fotos de diferentes tours no se mezclan`**
   - ✅ Verifica que las fotos de diferentes tours se mantienen separadas
   - ✅ Valida que al cargar fotos de un tour, solo se muestran las fotos de ese tour

**Total de pruebas:** 5

---

## HU-009: Encuestas de Satisfacción

**Archivo:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionEncuestaTest.kt`

### Escenarios de Prueba:

1. **`test HU-009 Escenario 1 - Envío automático de encuesta después de finalizar tour`**
   - ✅ Verifica que cuando un tour ha finalizado, el sistema envía automáticamente una encuesta al turista
   - ✅ Valida que se crea una notificación de tipo `ENCUESTA_SATISFACCION`
   - ✅ Verifica que el turista recibe la notificación con enlace o formulario

2. **`test HU-009 Escenario 2 - Registro de respuesta de encuesta`**
   - ✅ Verifica que un turista puede completar la encuesta
   - ✅ Valida que se registra la calificación y comentario
   - ✅ Verifica que se genera una métrica de satisfacción
   - ✅ Valida que se suman puntos por completar la encuesta (50 puntos)

3. **`test no se envía encuesta si el usuario ya respondió`**
   - ✅ Verifica que no se envía una encuesta duplicada si el usuario ya respondió
   - ✅ Valida la lógica de prevención de duplicados

4. **`test no se registra respuesta duplicada`**
   - ✅ Verifica que no se puede registrar una respuesta duplicada
   - ✅ Valida que se muestra un mensaje de error apropiado

5. **`test validación de calificación debe estar entre 1 y 5`**
   - ✅ Verifica que la calificación debe estar en el rango válido (1-5)
   - ✅ Valida que se rechazan calificaciones fuera del rango

6. **`test puntos se suman al completar encuesta`**
   - ✅ Verifica que se suman 50 puntos al completar una encuesta
   - ✅ Valida que los puntos se acumulan correctamente

7. **`test calificación promedio se calcula correctamente`**
   - ✅ Verifica que se calcula correctamente la calificación promedio de un tour
   - ✅ Valida el cálculo con múltiples respuestas

8. **`test flujo completo desde notificación hasta respuesta`**
   - ✅ Verifica el flujo completo desde el envío de la notificación hasta el registro de la respuesta
   - ✅ Valida que se suman puntos y se guarda la respuesta correctamente

**Total de pruebas:** 8

---

## Resumen General

| HU | Descripción | Archivo de Pruebas | Total de Pruebas | Estado |
|---|---|---|---|---|
| **HU-004** | Registro e Inicio de Sesión | `IntegracionAutenticacionTest.kt` | 7 | ✅ Completo |
| **HU-005** | Escaneo de QR para Check-in | `IntegracionEscaneoQRTest.kt` | 6 | ✅ Completo |
| **HU-006** | Sistema de Notificaciones | `IntegracionNotificacionesTest.kt` | 6 | ✅ Completo |
| **HU-007** | Sistema de Recompensas y Logros | `IntegracionRecompensasTest.kt` | 9 | ✅ Completo |
| **HU-008** | Subida y Visualización de Fotos | `IntegracionAlbumFotosTest.kt` | 5 | ✅ Completo |
| **HU-009** | Encuestas de Satisfacción | `IntegracionEncuestaTest.kt` | 8 | ✅ Completo |
| **TOTAL** | | | **41 pruebas** | ✅ **Todas completas** |

---

## Ejecutar las Pruebas

### Ejecutar todas las pruebas de integración:
```bash
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.*"
```

### Ejecutar pruebas por HU específica:
```bash
# HU-004
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionAutenticacionTest"

# HU-005
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionEscaneoQRTest"

# HU-006
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionNotificacionesTest"

# HU-007
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionRecompensasTest"

# HU-008
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionAlbumFotosTest"

# HU-009
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionEncuestaTest"
```

### Ejecutar una prueba específica:
```bash
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionAutenticacionTest.test HU-004 Escenario 1"
```

---

## Tecnologías Utilizadas

- **JUnit 4**: Framework de pruebas
- **MockK**: Framework de mocking para Kotlin
- **Kotlin Coroutines Test**: Para pruebas asíncronas con `runTest` y `StandardTestDispatcher`
- **LiveData Testing**: Para probar LiveData con `InstantTaskExecutorRule`
- **Android Architecture Components**: ViewModel, LiveData, Repository pattern

---

## Patrones de Prueba

### Estructura común de las pruebas:

1. **Setup (`@Before`)**:
   - Configurar mocks de `DatabaseHelper`
   - Instanciar `PeruvianServiceRepository`
   - Configurar `StandardTestDispatcher` para coroutines

2. **Teardown (`@After`)**:
   - Limpiar mocks
   - Resetear singleton del repositorio
   - Resetear dispatcher

3. **Tests**:
   - Arrange: Configurar datos y mocks específicos
   - Act: Ejecutar la acción a probar
   - Assert: Verificar resultados y llamadas a métodos

### Características comunes:

- ✅ Uso de `runTest` para pruebas asíncronas
- ✅ Uso de `testDispatcher.scheduler.advanceUntilIdle()` para controlar ejecución
- ✅ Observadores mockeados para LiveData
- ✅ Verificación de llamadas a métodos con `verify()`
- ✅ Captura de argumentos con `slot()` y `capture()`

---

## Cobertura de Pruebas

### Funcionalidades cubiertas:

- ✅ **Autenticación**: Registro, inicio de sesión, validación de credenciales, hash de contraseñas
- ✅ **Check-in**: Validación de QR, registro de asistencia, manejo de errores
- ✅ **Notificaciones**: Recordatorios, alertas climáticas, ofertas de último minuto
- ✅ **Recompensas**: Acumulación de puntos, desbloqueo de logros, cálculo de niveles
- ✅ **Fotos**: Subida de fotos, visualización de álbum, filtrado por aprobación
- ✅ **Encuestas**: Envío automático, registro de respuestas, cálculo de promedios

### Casos límite cubiertos:

- ✅ Credenciales incorrectas
- ✅ QR inválido o ya usado
- ✅ Notificaciones duplicadas
- ✅ Respuestas de encuesta duplicadas
- ✅ Validación de rangos (calificaciones 1-5)
- ✅ Álbumes vacíos
- ✅ Tours sin fotos

---

## Notas Importantes

1. **Mocks de DatabaseHelper**: Todas las pruebas usan mocks de `DatabaseHelper` para evitar acceso real a la base de datos, lo que hace las pruebas rápidas y aisladas.

2. **Singleton del Repositorio**: Se resetea la instancia singleton del repositorio en `tearDown()` para asegurar que cada test comience con un estado limpio.

3. **Coroutines**: Todas las pruebas asíncronas usan `runTest` y `testDispatcher.scheduler.advanceUntilIdle()` para controlar la ejecución de coroutines.

4. **LiveData Observers**: Se usan observadores mockeados para verificar que los LiveData se actualizan correctamente.

5. **Verificación de Flujos Completos**: Cada HU incluye al menos una prueba que verifica el flujo completo desde el inicio hasta el final.

---

## Estado Actual

- ✅ **41 pruebas de integración** implementadas y documentadas
- ✅ **6 HUs** completamente cubiertas (HU-004 a HU-009)
- ✅ **Todos los escenarios principales** de cada HU están probados
- ✅ **Casos límite** y validaciones cubiertos
- ✅ **Flujos completos** verificados
- ✅ **Documentación completa** creada

---

## Próximos Pasos Recomendados

1. ✅ Ejecutar todas las pruebas y verificar que pasen
2. ✅ Agregar más pruebas de casos límite si es necesario
3. ✅ Considerar agregar pruebas de rendimiento para operaciones críticas
4. ✅ Mantener las pruebas actualizadas con cambios en el código
5. ✅ Considerar agregar pruebas de UI con Espresso si es necesario

---

## Conclusión

Todas las historias de usuario desde HU-004 hasta HU-009 tienen pruebas de integración completas que cubren:

- ✅ Todos los escenarios principales de cada HU
- ✅ Casos límite y validaciones
- ✅ Flujos completos de extremo a extremo
- ✅ Manejo de errores
- ✅ Verificación de datos y estados

Las pruebas están bien estructuradas, documentadas y listas para ejecutarse, proporcionando una base sólida para mantener la calidad del código.

