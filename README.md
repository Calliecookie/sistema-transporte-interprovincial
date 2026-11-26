# Sistema de gestión de una empresa de transporte interprovincial

Caso propuesto Nro. 7. Aplicación web para administrar buses, conductores, rutas y viajes, y para reservar y vender pasajes con control de asientos. Hecha con Java 17, Spring Boot 3, Spring Data JPA (Hibernate), Spring Security, Thymeleaf y MySQL 8.

## Requisitos

- JDK 17 o superior
- MySQL 8 y MySQL Workbench
- NetBeans con soporte para Maven (la versión 17 o superior ya lo trae)
- Conexión a internet solo la primera vez, para que Maven descargue las dependencias. Después funciona sin internet: no se usa ningún CDN.

## Pasos para ejecutarlo

### 1. Crear la base de datos

1. Abra MySQL Workbench y conéctese a su servidor local.
2. Abra el archivo `base_datos/transporte_interprovincial.sql` (menú File > Open SQL Script).
3. Ejecute todo el script de una sola vez con el icono del rayo.
4. Si la base `transporte_interprovincial` ya existía, el script la elimina y la vuelve a crear con los datos de prueba.
5. Si prefiere conservar los datos de una instalación anterior, ejecute solo los scripts de actualización que le falten, en orden: `actualizacion_1_1_0.sql`, `actualizacion_1_2_0.sql` y `actualizacion_1_3_0.sql`.

### 2. Abrir el proyecto en NetBeans

1. Descomprima el archivo .zip.
2. En NetBeans use File > Open Project y seleccione la carpeta `gestion-transporte` (la que contiene el archivo `pom.xml`).
3. Espere a que NetBeans termine de descargar las dependencias.

### 3. Ajustar la contraseña de MySQL

Abra `src/main/resources/application.properties` y cambie estas líneas con los datos de su servidor:

```
spring.datasource.username=root
spring.datasource.password=tu_contrasena
```

### 4. Consulta automática de DNI (Factiliza)

Al registrar un pasajero o un conductor, basta con escribir los 8 dígitos del DNI para que los nombres y apellidos se completen solos con los datos de RENIEC, usando la API de [Factiliza](https://factiliza.com). Si el DNI ya está registrado en el sistema, se muestra un aviso.

Para activarla, defina la variable de entorno `FACTILIZA_TOKEN` con su token (en NetBeans: clic derecho al proyecto > Properties > Run > Environment, o en `nbactions.xml`). También puede escribirlo directamente en `application.properties`:

```
factiliza.token=su_token
```

Sin token la aplicación funciona igual y los datos se ingresan a mano.

### 5. Correo para recuperar la contraseña

En la pantalla de inicio de sesión, "¿Olvidó su contraseña?" envía al correo del usuario un enlace de un solo uso, vigente 30 minutos, para crear una nueva contraseña. El sistema guarda solo la huella del enlace, nunca el enlace, y permite 3 solicitudes por hora por usuario.

Para que el correo salga de verdad, defina estas variables de entorno (por ejemplo, una cuenta de Gmail con contraseña de aplicación):

```
CORREO_SERVIDOR=smtp.gmail.com
CORREO_PUERTO=587
CORREO_USUARIO=su_cuenta@gmail.com
CORREO_CONTRASENA=contraseña_de_aplicacion
CORREO_REMITENTE=su_cuenta@gmail.com
URL_PUBLICA=http://localhost:8081
```

Sin `CORREO_SERVIDOR`, el enlace se escribe en la consola de la aplicación, lo que sirve para probar y para una demostración.

### 6. Ejecutar

Presione F6 (Run Project). No hace falta configurar ningún servidor: Tomcat viene incluido.
Cuando la consola muestre `Started AplicacionTransporte`, abra http://localhost:8081 en el navegador.

Si el puerto 8080 está ocupado, cambie `server.port` (por defecto 8081) en `application.properties`.

## Usuarios de acceso

Los usuarios se crean al iniciar la aplicación, solo cuando la tabla `usuario` está vacía, y sus contraseñas se guardan con BCrypt.

| Usuario  | Contraseña    | Rol      |
|----------|---------------|----------|
| admin    | Admin123*     | ADMIN    |
| vendedor | Vendedor123*  | VENDEDOR |
| conductor | Conductor123* | CONDUCTOR (vinculado al primer conductor activo) |

Se puede ingresar con el usuario o con el correo (por ejemplo `admin@transportes-andinos.example`). Los correos de estos usuarios de prueba son ficticios: cámbielos desde Usuarios para probar la recuperación con un correo real.

Para crear más cuentas de conductor, el administrador entra a Usuarios, elige el rol Conductor y selecciona el conductor que usará la cuenta.

## Permisos por rol

| Sección                                              | ADMIN | VENDEDOR | CONDUCTOR |
|------------------------------------------------------|:-----:|:--------:|:---------:|
| Inicio, reportes (viajes, pasajeros y pasajeros por viaje, con CSV) |  Sí   |    Sí    |    No     |
| Alertas de viaje (accidentes, retrasos, pausas y buses sin llegada) |  Sí   |    Sí    |    No     |
| Pasajeros (registrar y editar)                       |  Sí   |    Sí    |    No     |
| Eliminar pasajeros                                   |  Sí   |    No    |    No     |
| Vender pasajes, reservas, anulaciones y pasajes      |  Sí   |    Sí    |    No     |
| Programar viajes                                     |  Sí   |    No    |    No     |
| Buses, conductores, destinos y rutas                 |  Sí   |    No    |    No     |
| Usuarios del sistema                                 |  Sí   |    No    |    No     |
| Mis viajes (rutas, horarios, ocupación, pasajeros y reportes de ruta) |  No   |    No    |    Sí     |

Los permisos de Vendedor y Conductor se pueden cambiar en **Roles y permisos** (solo el administrador): marque qué módulos puede usar cada rol (pasajeros, ventas y reservas, pasajes vendidos, reportes, alertas, mis viajes). Los valores iniciales son los de la tabla. La gestión de buses, conductores, destinos, rutas, viajes y usuarios es exclusiva del administrador.

La restricción se aplica en tres niveles: en las rutas (`SecurityFilterChain`), en los métodos de los servicios (`@PreAuthorize`) y en las vistas (los menús y botones se ocultan según el rol). Si un usuario entra a una dirección que no le corresponde, ve la página 403.

## Imágenes

Las fotografías se colocan en `src/main/resources/static/images/` con estos nombres exactos. Si falta alguna, la página muestra un fondo degradado y sigue funcionando.

| Archivo        | Tamaño sugerido | Dónde se usa                           | Qué buscar en Unsplash o Pexels                          |
|----------------|-----------------|----------------------------------------|----------------------------------------------------------|
| `login-bg.jpg` | 1920 x 1080 px  | Panel lateral de la pantalla de login  | "bus highway sunset", "coach bus road mountains"         |
| `bus.jpg`      | 1600 x 600 px   | Banner del panel de inicio             | "intercity bus", "bus on highway", "bus travel"          |
| `andes.jpg`    | 1920 x 1080 px  | Fondo de las páginas de error          | "andes mountains peru", "cusco landscape", "peru road"   |

Recomendaciones: formato JPG, menos de 400 KB cada una (calidad 75 a 80), y preferir fotos con zonas oscuras porque se les superpone una capa azul para que el texto se lea bien.

## Estructura del proyecto

```
base_datos/                      script SQL con las tablas y los datos de prueba
base_datos/actualizacion_1_1_0.sql  actualiza una base de la versión 1.0 a la 1.1
base_datos/actualizacion_1_2_0.sql  actualiza una base de la versión 1.1 a la 1.2
base_datos/actualizacion_1_3_0.sql  actualiza una base de la versión 1.2 a la 1.3
src/main/java/pe/transportes/gestion
    modelo/                      entidades JPA, enumeraciones y formularios
    repositorio/                 interfaces Spring Data y especificaciones de filtros
    servicio/                    reglas de negocio, transacciones y @PreAuthorize
    controlador/                 controladores MVC y manejo de errores (@ControllerAdvice)
    seguridad/                   configuración de Spring Security, usuarios y roles
    util/                        excepciones, exportador CSV y utilidades
    configuracion/               configuración general
    integracion/                 consulta de DNI con la API de Factiliza
    recuperacion/                recuperación de contraseña y envío de correo
src/main/resources
    templates/                   vistas Thymeleaf
    static/                      CSS, JavaScript e imágenes
src/test/java                    pruebas unitarias de las reglas de negocio
```

## Reglas de negocio

- No se puede reservar ni vender un asiento que ya está ocupado (se bloquea el asiento antes de modificarlo).
- No se puede programar un viaje si el bus o el conductor ya tienen otro viaje que se cruza en ese horario.
- Al programar un viaje se generan tantos asientos como la capacidad del bus.
- Al anular una reserva el asiento queda libre, y si la reserva estaba vendida el pasaje pasa a anulado.
- Al cancelar un viaje se anulan todas sus reservas.
- Un pasajero no puede tener dos asientos activos en el mismo viaje.
- No se puede cambiar el bus ni la ruta de un viaje que ya tiene reservas.
- No se pueden eliminar registros que tienen datos relacionados (por ejemplo, un bus con viajes).

## Seguimiento de viajes y alertas

El conductor entra con su cuenta y ve en **Mis viajes**:

- Las rutas planificadas con su horario de salida, llegada estimada, bus y asientos ocupados.
- La lista de pasajeros de cada viaje con su número de asiento.
- Botones para registrar la salida, pausar, reanudar, reportar un retraso (con minutos), escribir observaciones, confirmar la llegada y **reportar un accidente**.
- La bitácora con todo lo registrado en el viaje.

El accidente, el retraso y la pausa generan una alerta que el administrador y los vendedores ven al instante: un aviso rojo en todas las pantallas, un contador en el menú (se actualiza solo cada 30 segundos) y el detalle en **Alertas de viaje**, donde se marcan como atendidas.

Además, si un viaje no se confirma como finalizado una hora después de su llegada estimada, el sistema lo muestra como alerta **Bus sin confirmar llegada**, aunque el conductor no haya reportado nada (por ejemplo, si no tiene señal).

Estados de viaje: Programado, En curso, En pausa, Retrasado, Detenido por incidente, Finalizado y Cancelado.

## Búsquedas, paginación y reportes

- Todos los listados tienen filtros y paginación de 10 registros por página (`Pageable`).
- Filtros: DNI, nombre, fecha, ruta y estado, según la pantalla.
- Reportes en el menú Reportes: viajes (ocupación e ingresos) y pasajeros (asiento, reserva y pasaje). Ambos se pueden exportar a CSV con los filtros aplicados. El archivo se abre en Excel con las tildes correctas.
- Reporte de pasajeros por viaje: desde el reporte de viajes (botón Pasajeros) o desde el detalle del viaje (Lista de pasajeros). Muestra la ocupación del bus y la lista ordenada por asiento; se puede imprimir o exportar a CSV.

## Pruebas

Las pruebas unitarias no necesitan base de datos:

```
mvn test
```

## Problemas frecuentes

- `Access denied for user 'root'`: la contraseña de `application.properties` no coincide con la de su MySQL.
- `Communications link failure` o `Connection refused`: el servicio de MySQL está apagado.
- `Unknown database 'transporte_interprovincial'` o `Schema-validation`: falta ejecutar el script SQL del paso 1.
- `Port 8080 was already in use`: otra aplicación usa ese puerto; cambie `server.port` (por defecto 8081).
