# Pruebas de Integración - HU-004, HU-005 y HU-006

## Resumen

Se han actualizado y creado pruebas de integración para las historias de usuario HU-004 (Registro e Inicio de Sesión), HU-005 (Escaneo de QR) y HU-006 (Sistema de Notificaciones).

## Archivos Actualizados/Creados

### 1. `IntegracionAutenticacionTest.kt` (HU-004)
**Ubicación:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionAutenticacionTest.kt`

**Pruebas implementadas:**

1. **`test HU-004 Escenario 1 - Registro de usuario sin rol asigna rol turista por defecto`**
   - Verifica que cuando un usuario se registra sin especificar rol, se le asigna el rol por defecto (turista = 2)
   - Valida que se crea el usuario correctamente
   - Verifica que el usuario tiene el rol correcto

2. **`test HU-004 Escenario 2 - Inicio de sesión con credenciales correctas redirige según rol`**
   - Verifica que un usuario puede iniciar sesión con credenciales válidas
   - Valida que se identifica el rol del usuario
   - Verifica que se puede obtener el rol correctamente

3. **`test inicio de sesión con credenciales incorrectas muestra mensaje de error`**
   - Verifica que se muestra un mensaje de error cuando las credenciales son incorrectas
   - Valida que no se autentica al usuario
   - Verifica que `mensajeEstado` contiene el mensaje de error

4. **`test registro con rol específico asigna el rol correcto`**
   - Verifica que se puede registrar un usuario con un rol específico
   - Valida que el rol se asigna correctamente

5. **`test contraseña se hashea correctamente con SHA-256`**
   - Verifica que las contraseñas se hashean correctamente antes de almacenarse
   - Valida que el hash es diferente de la contraseña original

6. **`test flujo completo de registro e inicio de sesión`**
   - Verifica el flujo completo desde el registro hasta el inicio de sesión
   - Valida que un usuario registrado puede iniciar sesión con las mismas credenciales

7. **`test obtenerUsuarioActual retorna usuario autenticado`**
   - Verifica que `obtenerUsuarioActual()` retorna el usuario autenticado correctamente

**Cambios realizados:**
- ✅ Actualizado para usar `usuarioAutenticado` en lugar de `usuario`
- ✅ Actualizado para usar `registrarUsuario()` en lugar de `registrar()`
- ✅ Actualizado para usar `iniciarSesion()` con `nombreUsuario` en lugar de `correo`
- ✅ Agregado soporte para `mensajeEstado` LiveData
- ✅ Agregado uso de `runTest` para pruebas asíncronas

### 2. `IntegracionEscaneoQRTest.kt` (HU-005)
**Ubicación:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionEscaneoQRTest.kt`

**Pruebas implementadas:**

1. **`test HU-005 Escenario 1 - Escaneo de QR válido marca asistencia como confirmada`**
   - Verifica que cuando se escanea un QR válido, se registra el check-in
   - Valida que se retorna un objeto `CheckIn` con estado "Confirmado"
   - Verifica que se actualiza `resultadoCheckin` LiveData

2. **`test HU-005 Escenario 2 - Escaneo de QR inválido muestra mensaje de error`**
   - Verifica que cuando se escanea un QR inválido, se muestra un mensaje de error
   - Valida que NO se registra un check-in
   - Verifica que `mensajeEstado` contiene el mensaje "QR no válido o ya registrado"

3. **`test escaneo de QR ya usado muestra mensaje de error`**
   - Verifica que cuando se intenta escanear un QR que ya fue usado, se muestra un mensaje de error
   - Valida que NO se registra otro check-in
   - Verifica que se detecta correctamente que el QR ya fue usado

4. **`test escaneo de QR que no pertenece al tour muestra mensaje de error`**
   - Verifica que cuando se escanea un QR que pertenece a otro tour, se muestra un mensaje de error
   - Valida que NO se registra el check-in
   - Verifica que `validarCodigoQR` valida correctamente que el QR pertenece al tour

5. **`test cargar tours del día muestra lista de tours asignados al guía`**
   - Verifica que se pueden cargar los tours del día para un guía
   - Valida que se retorna la lista correcta de tours

6. **`test flujo completo desde escaneo QR hasta confirmación de asistencia`**
   - Verifica el flujo completo desde el escaneo del QR hasta la confirmación de asistencia
   - Valida que se ejecutan todos los pasos correctamente
   - Verifica que se retorna el `CheckIn` registrado

**Cambios realizados:**
- ✅ Actualizado para usar `resultadoCheckin: LiveData<CheckIn?>` en lugar de `resultadoEscaneo: LiveData<String>`
- ✅ Actualizado para usar `mensajeEstado: LiveData<String?>` en lugar de `error: LiveData<String>`
- ✅ Actualizado para usar `validarCodigoQR(codigoQR, idTour): Reserva?` en lugar de métodos separados
- ✅ Actualizado para usar `registrarCheckIn()` que retorna `CheckIn?` en lugar de `Boolean`
- ✅ Agregado mock para `obtenerCheckInPorReserva()`

### 3. `IntegracionNotificacionesTest.kt` (HU-006)
**Ubicación:** `app/src/test/java/com/grupo4/appreservas/integracion/IntegracionNotificacionesTest.kt`

**Pruebas implementadas:**

1. **`test HU-006 Escenario 1 - Recordatorio de horario se genera correctamente`**
   - Verifica que se genera un recordatorio cuando un tour está próximo a iniciar
   - Valida que se crea una notificación de tipo `RECORDATORIO`

2. **`test HU-006 Escenario 2 - Alerta climática se genera cuando hay cambio de clima`**
   - Verifica que se detecta un cambio climático
   - Valida que se genera una notificación de alerta climática

3. **`test HU-006 Escenario 3 - Oferta de último minuto se genera para tours con baja ocupación`**
   - Verifica que se detectan tours con baja ocupación
   - Valida que se genera una notificación de oferta de último minuto

4. **`test cargar recordatorios devuelve lista de notificaciones`**
   - Verifica que se pueden cargar las notificaciones de un usuario
   - Valida que se retornan los tipos correctos de notificaciones

5. **`test flujo completo de notificaciones - desde reserva hasta notificación`**
   - Verifica el flujo completo desde la creación de una reserva hasta la generación de notificaciones
   - Valida que se persisten las notificaciones correctamente

**Estado:** ✅ Las pruebas están completas y actualizadas según los cambios realizados en HU-006.

## Cobertura de Pruebas

### HU-004 (Registro e Inicio de Sesión)
- ✅ Registro de usuario sin rol (asigna turista por defecto)
- ✅ Registro de usuario con rol específico
- ✅ Inicio de sesión con credenciales correctas
- ✅ Inicio de sesión con credenciales incorrectas
- ✅ Hash de contraseñas con SHA-256
- ✅ Flujo completo de registro e inicio de sesión
- ✅ Obtener usuario actual autenticado

### HU-005 (Escaneo de QR)
- ✅ Escaneo de QR válido marca asistencia como confirmada
- ✅ Escaneo de QR inválido muestra mensaje de error
- ✅ Escaneo de QR ya usado muestra mensaje de error
- ✅ Escaneo de QR que no pertenece al tour muestra mensaje de error
- ✅ Cargar tours del día para el guía
- ✅ Flujo completo desde escaneo QR hasta confirmación

### HU-006 (Notificaciones)
- ✅ Recordatorio de horario
- ✅ Alerta climática
- ✅ Oferta de último minuto
- ✅ Carga de notificaciones
- ✅ Flujo completo de notificaciones

## Ejecutar las Pruebas

Para ejecutar las pruebas de integración:

```bash
# Ejecutar todas las pruebas de integración
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.*"

# Ejecutar solo las pruebas de HU-004
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionAutenticacionTest"

# Ejecutar solo las pruebas de HU-005
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionEscaneoQRTest"

# Ejecutar solo las pruebas de HU-006
./gradlew :app:testDebugUnitTest --tests "com.grupo4.appreservas.integracion.IntegracionNotificacionesTest"
```

## Tecnologías Utilizadas

- **JUnit 4**: Framework de pruebas
- **MockK**: Framework de mocking para Kotlin
- **Kotlin Coroutines Test**: Para pruebas asíncronas
- **LiveData Testing**: Para probar LiveData con `InstantTaskExecutorRule`
- **StandardTestDispatcher**: Para controlar la ejecución de coroutines en pruebas

## Notas Importantes

1. **Mocks de DatabaseHelper**: Todas las pruebas usan mocks de `DatabaseHelper` para evitar acceso real a la base de datos.

2. **Singleton del Repositorio**: Se resetea la instancia singleton del repositorio en `tearDown()` para asegurar que cada test comience con un estado limpio.

3. **Coroutines**: Todas las pruebas asíncronas usan `runTest` y `testDispatcher.scheduler.advanceUntilIdle()` para controlar la ejecución.

4. **LiveData Observers**: Se usan observadores mockeados para verificar que los LiveData se actualizan correctamente.

## Estado Actual

- ✅ Estructura de pruebas actualizada
- ✅ Pruebas implementadas para todos los escenarios de HU-004, HU-005 y HU-006
- ✅ Pruebas alineadas con los cambios recientes en el código
- ✅ Uso correcto de coroutines y LiveData
- ✅ Documentación creada

## Próximos Pasos

1. Ejecutar las pruebas y verificar que todas pasen
2. Agregar más pruebas de casos límite si es necesario
3. Mejorar la cobertura de código
4. Considerar agregar pruebas de rendimiento si es necesario

