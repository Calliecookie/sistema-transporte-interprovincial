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

### 4. Ejecutar

Presione F6 (Run Project). No hace falta configurar ningún servidor: Tomcat viene incluido.
Cuando la consola muestre `Started AplicacionTransporte`, abra http://localhost:8081 en el navegador.

Si el puerto 8080 está ocupado, cambie `server.port` (por defecto 8081) en `application.properties`.

## Usuarios de acceso

Los usuarios se crean al iniciar la aplicación, solo cuando la tabla `usuario` está vacía, y sus contraseñas se guardan con BCrypt.

| Usuario  | Contraseña    | Rol      |
|----------|---------------|----------|
| admin    | Admin123*     | ADMIN    |
| vendedor | Vendedor123*  | VENDEDOR |

## Permisos por rol

| Sección                                              | ADMIN | VENDEDOR |
|------------------------------------------------------|:-----:|:--------:|
| Inicio, reportes (viajes y pasajeros, con CSV)       |  Sí   |    Sí    |
| Pasajeros (registrar y editar)                       |  Sí   |    Sí    |
| Eliminar pasajeros                                   |  Sí   |    No    |
| Vender pasajes, reservas, anulaciones y pasajes      |  Sí   |    Sí    |
| Programar viajes                                     |  Sí   |    No    |
| Buses, conductores, destinos y rutas                 |  Sí   |    No    |
| Usuarios del sistema                                 |  Sí   |    No    |

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
src/main/java/pe/transportes/gestion
    modelo/                      entidades JPA, enumeraciones y formularios
    repositorio/                 interfaces Spring Data y especificaciones de filtros
    servicio/                    reglas de negocio, transacciones y @PreAuthorize
    controlador/                 controladores MVC y manejo de errores (@ControllerAdvice)
    seguridad/                   configuración de Spring Security, usuarios y roles
    util/                        excepciones, exportador CSV y utilidades
    configuracion/               configuración general
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

## Búsquedas, paginación y reportes

- Todos los listados tienen filtros y paginación de 10 registros por página (`Pageable`).
- Filtros: DNI, nombre, fecha, ruta y estado, según la pantalla.
- Reportes en el menú Reportes: viajes (ocupación e ingresos) y pasajeros (asiento, reserva y pasaje). Ambos se pueden exportar a CSV con los filtros aplicados. El archivo se abre en Excel con las tildes correctas.

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
