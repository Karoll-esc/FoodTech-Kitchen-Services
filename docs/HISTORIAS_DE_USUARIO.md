# 📋 Historias de Usuario - FoodTech Kitchen Service

## HU-001: Procesar pedido de cocina

### Descripción

**Como** responsable de cocina  
**Quiero** que el sistema reciba un pedido y lo descomponga automáticamente en tareas por estación  
**Para** que cada área de preparación pueda trabajar de forma independiente y eficiente

### Criterios de Aceptación

#### Escenario 1: Pedido con un solo tipo de producto

```gherkin
Scenario: Pedido únicamente con bebidas
  Given que existe un pedido para la mesa "A1"
  And el pedido contiene 2 bebidas diferentes
  When el pedido es registrado en el sistema
  Then el sistema genera 1 tarea de preparación
  And la tarea es asignada a la estación de barra
  And la tarea contiene los 2 productos solicitados
```

#### Escenario 2: Pedido mixto con múltiples tipos de productos

```gherkin
Scenario: Pedido con bebidas, plato caliente y postre
  Given que existe un pedido para la mesa "B5"
  And el pedido contiene 1 bebida
  And el pedido contiene 1 plato principal
  And el pedido contiene 1 postre
  When el pedido es registrado en el sistema
  Then el sistema genera 3 tareas de preparación
  And existe 1 tarea asignada a la estación de barra
  And existe 1 tarea asignada a la estación de cocina caliente
  And existe 1 tarea asignada a la estación de cocina fría
  And cada tarea contiene únicamente los productos de su estación correspondiente
```

#### Escenario 3: Agrupación de productos similares

```gherkin
Scenario: Múltiples productos del mismo tipo se agrupan en una sola tarea
  Given que existe un pedido para la mesa "C2"
  And el pedido contiene 3 bebidas diferentes
  And el pedido contiene 2 platos principales diferentes
  When el pedido es registrado en el sistema
  Then el sistema genera 2 tareas de preparación
  And la tarea de barra contiene las 3 bebidas agrupadas
  And la tarea de cocina caliente contiene los 2 platos agrupados
```

#### Escenario 4: Pedido sin productos no puede ser procesado

```gherkin
Scenario: Sistema rechaza pedidos vacíos
  Given que existe un pedido para la mesa "D3"
  And el pedido no contiene ningún producto
  When se intenta registrar el pedido en el sistema
  Then el sistema rechaza el pedido
  And se notifica que el pedido debe contener al menos un producto
  And no se genera ninguna tarea de preparación
```

#### Escenario 5: Validación de información mínima requerida

```gherkin
Scenario: Pedido sin identificación de mesa no puede ser procesado
  Given que existe un pedido sin número de mesa asignado
  And el pedido contiene 2 productos válidos
  When se intenta registrar el pedido en el sistema
  Then el sistema rechaza el pedido
  And se notifica que el pedido debe tener un número de mesa válido
  And no se genera ninguna tarea de preparación
```

---

## HU-002: Consultar tareas por estación

### Descripción

**Como** encargado de una estación de cocina  
**Quiero** visualizar únicamente las tareas pendientes de mi estación  
**Para** prepararlas sin confusión con tareas de otras áreas

### Criterios de Aceptación

#### Escenario 1: Consulta de tareas de una estación específica

```gherkin
Scenario: Estación de barra consulta sus tareas pendientes
  Given que existen 3 tareas pendientes en el sistema
  And 2 tareas están asignadas a la estación de barra
  And 1 tarea está asignada a la estación de cocina caliente
  When el encargado de barra consulta las tareas de su estación
  Then el sistema muestra únicamente las 2 tareas de barra
  And no se muestran tareas de otras estaciones
```

#### Escenario 2: Estación sin tareas pendientes

```gherkin
Scenario: Estación consulta tareas cuando no tiene pendientes
  Given que existen 2 tareas pendientes en el sistema
  And ambas tareas están asignadas a la estación de cocina caliente
  And no hay tareas asignadas a la estación de barra
  When el encargado de barra consulta las tareas de su estación
  Then el sistema muestra que no hay tareas pendientes
  And se confirma que la consulta fue exitosa
```

#### Escenario 3: Información completa de cada tarea

```gherkin
Scenario: Cada tarea muestra la información necesaria para su preparación
  Given que existe 1 tarea pendiente para la estación de barra
  And la tarea corresponde al pedido de la mesa "A1"
  And la tarea contiene 2 bebidas específicas
  When el encargado de barra consulta las tareas de su estación
  Then el sistema muestra el número de mesa asociado
  And el sistema muestra la lista detallada de productos a preparar
  And el sistema muestra el momento en que se creó la tarea
```

#### Escenario 4: Validación de estación existente

```gherkin
Scenario: Consulta de estación inexistente
  Given que el sistema solo reconoce las estaciones: barra, cocina caliente y cocina fría
  When se consultan tareas para una estación no reconocida
  Then el sistema informa que la estación no existe
  And no se muestran tareas
```

---

## HU-003: Ejecutar tarea de preparación

### Descripción

**Como** cocinero de una estación  
**Quiero** iniciar la preparación de una tarea asignada  
**Para** que el sistema registre automáticamente el progreso y notifique cuando esté completada

### Criterios de Aceptación

#### Escenario 1: Iniciar preparación de una tarea
```gherkin
Scenario: Cocinero inicia preparación de una tarea pendiente
  Given que existe una tarea pendiente para la estación de barra
  And la tarea está en estado "PENDIENTE"
  When el cocinero indica que inicia la preparación de la tarea
  Then el sistema cambia el estado de la tarea a "EN_PREPARACION"
  And el sistema registra la hora de inicio de preparación
```

#### Escenario 2: Sistema completa tarea automáticamente
```gherkin
Scenario: Tarea se completa automáticamente al finalizar preparación
  Given que existe una tarea en estado "EN_PREPARACION"
  And el cocinero está ejecutando la preparación física de los productos
  When el tiempo estimado de preparación transcurre
  Then el sistema cambia el estado de la tarea a "COMPLETADA" automáticamente
  And el sistema registra la hora de finalización
  And el sistema calcula el tiempo total de preparación
```

#### Escenario 3: Visualización de tareas completadas por estación
```gherkin
Scenario: Consulta de tareas completadas de una estación
  Given que la estación de barra tiene 2 tareas completadas
  And la estación de barra tiene 1 tarea en preparación
  And la estación de barra tiene 1 tarea pendiente
  When el responsable consulta el historial de tareas completadas de barra
  Then el sistema muestra únicamente las 2 tareas completadas
  And cada tarea muestra su tiempo total de preparación
```

#### Escenario 4: Estado del pedido basado en estado de sus tareas
```gherkin
Scenario: Pedido refleja el estado agregado de todas sus tareas
  Given que un pedido generó 3 tareas para diferentes estaciones
  And 2 tareas ya están completadas
  And 1 tarea está en preparación
  When el área de servicio consulta el estado del pedido
  Then el sistema indica que el pedido está "EN_PREPARACION"
  
  When la última tarea se completa automáticamente
  And el área de servicio consulta nuevamente el estado del pedido
  Then el sistema indica que el pedido está "COMPLETADO"
```

#### Escenario 5: No se puede iniciar una tarea ya iniciada
```gherkin
Scenario: Validación de estado antes de iniciar preparación
  Given que existe una tarea en estado "EN_PREPARACION"
  When el cocinero intenta iniciar nuevamente la preparación de la misma tarea
  Then el sistema rechaza la operación
  And el sistema informa que la tarea ya está en preparación
  And la tarea permanece en estado "EN_PREPARACION"
```

---

## HU-004: Autenticación y control de acceso por roles

### Descripción

**Como** administrador del sistema  
**Quiero** que los usuarios se autentiquen mediante Auth0 y tengan accesos diferenciados según su rol  
**Para** garantizar la seguridad y que cada persona solo pueda realizar las operaciones correspondientes a su función

### Criterios de Aceptación

#### Escenario 1: Usuario sin autenticación no puede acceder al sistema

```gherkin
Scenario: Acceso sin autenticación
  Given que un usuario no ha iniciado sesión
  When el usuario intenta acceder a cualquier funcionalidad del sistema
  Then el sistema rechaza el acceso
  And el sistema solicita autenticación
```

#### Escenario 2: Mesero puede crear pedidos pero no puede gestionar productos

```gherkin
Scenario: Mesero con permisos limitados
  Given que existe un usuario autenticado con rol "WAITER"
  When el usuario intenta crear un pedido para una mesa
  Then el sistema permite la operación y confirma la creación exitosa
  
  When el mismo usuario intenta crear un nuevo producto en el catálogo
  Then el sistema rechaza la operación por permisos insuficientes
  And el sistema informa que no tiene permisos de administrador
```

#### Escenario 3: Personal de cocina solo puede modificar tareas de su propia estación

```gherkin
Scenario: Personal de barra intenta modificar tarea de cocina caliente
  Given que existe un usuario autenticado con rol "KITCHEN_BAR"
  And existe una tarea pendiente asignada a la estación "HOT_KITCHEN"
  When el usuario intenta iniciar la preparación de esa tarea
  Then el sistema rechaza la operación por permisos insuficientes
  And el sistema informa que solo puede modificar tareas de la estación "BAR"
  
  Given que existe una tarea pendiente asignada a la estación "BAR"
  When el usuario intenta iniciar la preparación de esa tarea
  Then el sistema permite la operación y cambia el estado a "EN_PREPARACION"
```

#### Escenario 4: Personal de cocina puede visualizar todas las tareas

```gherkin
Scenario: Encargado de barra visualiza tareas de todas las estaciones
  Given que existe un usuario autenticado con rol "KITCHEN_BAR"
  And existen 5 tareas pendientes: 2 en BAR, 2 en HOT_KITCHEN, 1 en COLD_KITCHEN
  When el usuario consulta todas las tareas del sistema
  Then el sistema muestra las 5 tareas con toda su información
  And cada tarea indica si el usuario puede modificarla (campo "canModify")
  And las tareas de BAR muestran "canModify": true
  And las tareas de otras estaciones muestran "canModify": false
```

#### Escenario 5: Administrador tiene acceso completo

```gherkin
Scenario: Administrador puede realizar cualquier operación
  Given que existe un usuario autenticado con rol "ADMIN"
  When el usuario intenta crear un producto
  Then el sistema permite la operación
  
  When el usuario intenta crear una mesa
  Then el sistema permite la operación
  
  When el usuario intenta modificar tareas de cualquier estación
  Then el sistema permite todas las operaciones
```

#### Escenario 6: Usuario puede consultar su información de perfil

```gherkin
Scenario: Consulta de información del usuario autenticado
  Given que existe un usuario autenticado con rol "KITCHEN_BAR"
  When el usuario consulta su información de perfil
  Then el sistema muestra los datos del usuario
  And la información incluye: userId, email, nombre, lista de roles
  And la información incluye el campo "station" con valor "BAR"
  
  Given que existe un usuario autenticado con rol "WAITER"
  When el usuario consulta su información de perfil
  Then el sistema muestra el perfil del usuario
  And el campo "station" es null (los meseros no tienen estación asignada)
```

---

# HU-005: Gestión administrativa de mesas

### Descripción

**Como** administrador del restaurante
**Quiero** registrar y consultar las mesas físicas del establecimiento
**Para** configurar la infraestructura del salón antes de iniciar la operación diaria

> **Nota:** Esta historia cubre exclusivamente la **configuración inicial** y el inventario de mesas. El flujo operativo de servicio (cambios de estado por pedidos, limpieza y ocupación) se detalla en la **HU-007**.

### Reglas de Negocio

* **Identificador Único:** Cada mesa debe tener un número o código de identificación que no se repita en el sistema.
* **Capacidad Mínima:** La capacidad de comensales por mesa debe ser un número entero mayor a **0**.
* **Estado por Defecto:** Toda mesa nueva debe iniciar automáticamente con el estado `AVAILABLE`.
* **Integridad:** Al crearse, la mesa no debe tener ningún `currentOrderId` asociado.

---

### Criterios de Aceptación

#### Escenario 1: Registro exitoso de una nueva mesa

```gherkin
Scenario: Administrador registra una nueva mesa válida
  Given que existe un usuario autenticado con rol "ADMIN"
  And no existe una mesa con número "A1" en el sistema
  When el administrador crea una mesa con número "A1" y capacidad para 4 personas
  Then el sistema registra la mesa exitosamente
  And la mesa queda con estado inicial "AVAILABLE"
  And la mesa no tiene ningún pedido asociado
  And el sistema confirma la creación con un mensaje de éxito

```

#### Escenario 2: Restricción de números de mesa duplicados

```gherkin
Scenario: Intento de registro de mesa con número ya existente
  Given que existe un usuario autenticado con rol "ADMIN"
  And ya existe una mesa registrada con número "B3"
  When el administrador intenta crear otra mesa con el mismo número "B3"
  Then el sistema rechaza la operación
  And el sistema informa que ya existe una mesa con ese número

```

#### Escenario 3: Validación de capacidad permitida

```gherkin
Scenario: Registro de mesa con capacidad inválida
  Given que el administrador intenta registrar una nueva mesa
  When ingresa una capacidad de 0 o un valor negativo
  Then el sistema rechaza la solicitud de creación
  And el sistema notifica que la capacidad debe ser mayor a 0

```

#### Escenario 4: Consulta del catálogo de mesas configuradas

```gherkin
Scenario: Visualización del listado de mesas para fines administrativos
  Given que el sistema tiene 5 mesas registradas
  When el administrador consulta la lista global de mesas
  Then el sistema muestra la información completa de las 5 mesas
  And cada registro incluye: número de mesa, capacidad, estado actual y fecha de creación

```
---

## HU-006: Gestión del catálogo de productos

### Descripción

**Como** administrador del restaurante  
**Quiero** gestionar el catálogo de productos (platillos y bebidas) del menú  
**Para** mantener actualizada la oferta disponible, sus precios y disponibilidad

### Criterios de Aceptación

#### Escenario 1: Administrador crea un nuevo producto en el catálogo

```gherkin
Scenario: Agregar nueva bebida al menú
  Given que existe un usuario autenticado con rol "ADMIN"
  And no existe un producto con nombre "Mojito"
  When el administrador crea un producto con:
    | nombre               | Mojito                          |
    | descripción          | Cóctel de ron blanco con menta |
    | tipo                 | DRINK                          |
    | precio               | 8.50                           |
    | tiempo preparación   | 4 segundos                     |
  Then el sistema crea el producto con estado "disponible" por defecto
  And el sistema confirma la creación exitosa
  And el sistema asigna un ID único al producto
```

#### Escenario 2: No se pueden crear productos duplicados

```gherkin
Scenario: Intento de crear producto con nombre ya existente
  Given que existe un producto llamado "Pizza Margherita"
  When el administrador intenta crear otro producto con el mismo nombre
  Then el sistema rechaza la operación con un mensaje de error
  And el sistema informa que ya existe un producto con ese nombre
```

#### Escenario 3: Administrador actualiza información de un producto

```gherkin
Scenario: Modificar precio y descripción de un platillo
  Given que existe un producto "Ensalada César" con precio 8.99
  When el administrador actualiza el producto con:
    | precio               | 9.50                                    |
    | descripción          | Ensalada con aderezo césar casero      |
    | tiempo preparación   | 6 segundos                             |
  Then el sistema actualiza la información del producto
  And el sistema actualiza el campo "updatedAt" con la fecha actual
  And el sistema confirma la actualización exitosa
```

#### Escenario 4: Administrador marca producto como no disponible

```gherkin
Scenario: Marcar producto temporalmente no disponible
  Given que existe un producto "Salmón a la parrilla" disponible
  And el restaurante se quedó sin salmón fresco
  When el administrador cambia la disponibilidad del producto a false
  Then el sistema actualiza el campo "available" a false
  And el producto ya no aparece en el menú para los meseros
  And el producto sigue existiendo en el sistema para futuras consultas
```

#### Escenario 5: Administrador reactiva producto previamente deshabilitado

```gherkin
Scenario: Volver a hacer disponible un producto
  Given que existe un producto "Tiramisu" con disponibilidad en false
  When el administrador cambia la disponibilidad del producto a true
  Then el sistema actualiza el campo "available" a true
  And el producto vuelve a aparecer en el menú para los meseros
```

#### Escenario 6: Administrador elimina un producto del catálogo

```gherkin
Scenario: Eliminar producto que ya no se ofrece
  Given que existe un producto "Sangría" que el restaurante ya no preparará
  When el administrador elimina el producto del catálogo
  Then el sistema elimina permanentemente el producto
  And el sistema confirma la eliminación
  And el producto no aparece en ninguna consulta posterior
```

#### Escenario 7: Mesero consulta productos disponibles para crear pedidos

```gherkin
Scenario: Visualización del menú disponible por meseros
  Given que existen 10 productos en el catálogo
  And 7 productos tienen disponibilidad en true
  And 3 productos tienen disponibilidad en false
  When un mesero consulta los productos disponibles
  Then el sistema muestra los 7 productos disponibles
  And cada producto incluye: nombre, descripción, tipo, precio
  And no se incluyen productos con disponibilidad en false
```

#### Escenario 8: Mesero no puede modificar el catálogo de productos

```gherkin
Scenario: Mesero intenta crear un producto
  Given que existe un usuario autenticado con rol "WAITER"
  When el usuario intenta crear un nuevo producto
  Then el sistema rechaza la operación por permisos insuficientes
  And el sistema informa que solo administradores pueden gestionar productos
```

#### Escenario 9: Consulta de productos filtrados por tipo

```gherkin
Scenario: Listar solo bebidas del catálogo
  Given que existen productos de diferentes tipos en el catálogo
  When el administrador consulta productos filtrando por tipo "DRINK"
  Then el sistema muestra solo productos de tipo DRINK
  And cada producto muestra su información completa con precio
```

#### Escenario 10: Productos incluyen precio en todas las respuestas

```gherkin
Scenario: Precio siempre presente en respuestas de productos
  Given que existen productos en el catálogo con precios asignados
  When cualquier usuario autorizado consulta productos
  Then cada producto en la respuesta incluye el campo "price"
  And el precio está formateado con 2 decimales
  And el precio es mayor o igual a 0.00
```

---

# HU-002: Gestión manual del ciclo de vida de las mesas

### Descripción

**Como** mesero
**Quiero** actualizar manualmente el estado de las mesas a medida que avanza el servicio
**Para** tener un control preciso sobre la disponibilidad real del salón y el progreso de cada mesa

> **Nota:** Esta historia modifica el comportamiento sugerido en la **HU-005**, eliminando las transiciones automáticas en favor de una gestión supervisada por el personal.

---

### Reglas de Negocio

* **Intervención Humana:** Ningún evento del sistema (creación de pedido o finalización de tareas) debe alterar el estado de la mesa sin una acción explícita del usuario.
* **Restricción de Flujo:** Aunque el cambio sea manual, el sistema solo debe permitir transiciones lógicas para evitar errores operativos (ej. no se puede pasar de `AVAILABLE` a `SERVED` directamente).
* **Visibilidad de Pedido:** Para pasar una mesa a `OCCUPIED`, el sistema debe verificar que la mesa tiene al menos un pedido activo registrado.

---

### Criterios de Aceptación

#### Escenario 1: Mesero ocupa una mesa manualmente

```gherkin
Scenario: El mesero marca una mesa como ocupada al recibir clientes
  Given que existe una mesa en estado "AVAILABLE"
  And el mesero ha registrado un pedido para dicha mesa
  When el mesero selecciona la opción "Marcar como Ocupada"
  Then el sistema cambia el estado de la mesa a "OCCUPIED"
  And registra el momento exacto del cambio de estado

```

#### Escenario 2: Mesero confirma que la mesa ha sido servida

```gherkin
Scenario: Cambio manual a estado servido
  Given que una mesa está en estado "OCCUPIED"
  And todas las tareas de cocina aparecen como "COMPLETED" en el sistema
  When el mesero confirma manualmente que todos los productos están en la mesa
  Then el sistema cambia el estado de la mesa a "SERVED"

```

#### Escenario 3: Inicio de limpieza tras el cierre de cuenta

```gherkin
Scenario: El mesero marca la mesa para limpieza
  Given que la mesa está en estado "SERVED"
  And los clientes han abandonado el establecimiento
  When el mesero selecciona la opción "Enviar a Limpieza"
  Then el sistema cambia el estado de la mesa a "CLEANING"

```

#### Escenario 4: Confirmación de disponibilidad

```gherkin
Scenario: Mesa lista para nuevos clientes
  Given que la mesa está en estado "CLEANING"
  When el personal de sala marca la limpieza como "Finalizada"
  Then el sistema cambia el estado de la mesa a "AVAILABLE"
  And desvincula el ID del pedido anterior (set null)

```

#### Escenario 5: Validación de permisos por rol

```gherkin
Scenario: Personal de cocina intenta cambiar estado de mesa
  Given que existe un usuario autenticado con rol "KITCHEN_BAR"
  When el usuario intenta cambiar el estado de la mesa "A1" de "OCCUPIED" a "SERVED"
  Then el sistema rechaza la operación por permisos insuficientes
  And informa que solo el rol "WAITER" o "ADMIN" puede gestionar estados de mesa

```

---
