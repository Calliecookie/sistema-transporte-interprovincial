DROP DATABASE IF EXISTS transporte_interprovincial;
CREATE DATABASE transporte_interprovincial CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE transporte_interprovincial;
SET NAMES utf8mb4;

CREATE TABLE usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre_usuario VARCHAR(40) NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BIT(1) NOT NULL DEFAULT b'1',
    conductor_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_usuario_nombre_usuario UNIQUE (nombre_usuario),
    CONSTRAINT uq_usuario_conductor UNIQUE (conductor_id),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN', 'VENDEDOR', 'CONDUCTOR'))
) ENGINE=InnoDB;

CREATE TABLE pasajero (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dni VARCHAR(8) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    telefono VARCHAR(9) NULL,
    correo VARCHAR(100) NULL,
    fecha_nacimiento DATE NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_pasajero_dni UNIQUE (dni),
    CONSTRAINT ck_pasajero_dni CHECK (dni REGEXP '^[0-9]{8}$'),
    CONSTRAINT ck_pasajero_telefono CHECK (telefono IS NULL OR telefono REGEXP '^9[0-9]{8}$')
) ENGINE=InnoDB;

CREATE TABLE conductor (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dni VARCHAR(8) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    licencia VARCHAR(9) NOT NULL,
    categoria_licencia VARCHAR(10) NOT NULL,
    telefono VARCHAR(9) NULL,
    activo BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    CONSTRAINT uq_conductor_dni UNIQUE (dni),
    CONSTRAINT uq_conductor_licencia UNIQUE (licencia),
    CONSTRAINT ck_conductor_dni CHECK (dni REGEXP '^[0-9]{8}$'),
    CONSTRAINT ck_conductor_licencia CHECK (licencia REGEXP '^[A-Za-z][0-9]{8}$'),
    CONSTRAINT ck_conductor_telefono CHECK (telefono IS NULL OR telefono REGEXP '^9[0-9]{8}$')
) ENGINE=InnoDB;

CREATE TABLE bus (
    id BIGINT NOT NULL AUTO_INCREMENT,
    placa VARCHAR(7) NOT NULL,
    marca VARCHAR(40) NOT NULL,
    modelo VARCHAR(40) NOT NULL,
    capacidad INT NOT NULL,
    anio_fabricacion INT NOT NULL,
    activo BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    CONSTRAINT uq_bus_placa UNIQUE (placa),
    CONSTRAINT ck_bus_capacidad CHECK (capacidad BETWEEN 20 AND 60),
    CONSTRAINT ck_bus_anio CHECK (anio_fabricacion BETWEEN 2000 AND 2030)
) ENGINE=InnoDB;

CREATE TABLE destino (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(60) NOT NULL,
    departamento VARCHAR(60) NOT NULL,
    terminal VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_destino_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE ruta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    origen_id BIGINT NOT NULL,
    destino_id BIGINT NOT NULL,
    distancia_km INT NOT NULL,
    duracion_horas DECIMAL(4,1) NOT NULL,
    precio_base DECIMAL(8,2) NOT NULL,
    activa BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    CONSTRAINT uq_ruta_origen_destino UNIQUE (origen_id, destino_id),
    CONSTRAINT fk_ruta_origen FOREIGN KEY (origen_id) REFERENCES destino (id),
    CONSTRAINT fk_ruta_destino FOREIGN KEY (destino_id) REFERENCES destino (id),
    CONSTRAINT ck_ruta_extremos CHECK (origen_id <> destino_id),
    CONSTRAINT ck_ruta_distancia CHECK (distancia_km > 0),
    CONSTRAINT ck_ruta_duracion CHECK (duracion_horas > 0),
    CONSTRAINT ck_ruta_precio CHECK (precio_base > 0)
) ENGINE=InnoDB;

CREATE TABLE viaje (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ruta_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL,
    conductor_id BIGINT NOT NULL,
    fecha_salida DATETIME NOT NULL,
    fecha_llegada DATETIME NOT NULL,
    precio DECIMAL(8,2) NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'PROGRAMADO',
    PRIMARY KEY (id),
    CONSTRAINT fk_viaje_ruta FOREIGN KEY (ruta_id) REFERENCES ruta (id),
    CONSTRAINT fk_viaje_bus FOREIGN KEY (bus_id) REFERENCES bus (id),
    CONSTRAINT fk_viaje_conductor FOREIGN KEY (conductor_id) REFERENCES conductor (id),
    CONSTRAINT ck_viaje_fechas CHECK (fecha_llegada > fecha_salida),
    CONSTRAINT ck_viaje_precio CHECK (precio > 0),
    CONSTRAINT ck_viaje_estado CHECK (estado IN ('PROGRAMADO', 'EN_CURSO', 'EN_PAUSA', 'RETRASADO', 'INCIDENTE', 'FINALIZADO', 'CANCELADO'))
) ENGINE=InnoDB;

CREATE TABLE asiento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    viaje_id BIGINT NOT NULL,
    numero INT NOT NULL,
    estado VARCHAR(12) NOT NULL DEFAULT 'LIBRE',
    PRIMARY KEY (id),
    CONSTRAINT uq_asiento_viaje_numero UNIQUE (viaje_id, numero),
    CONSTRAINT fk_asiento_viaje FOREIGN KEY (viaje_id) REFERENCES viaje (id),
    CONSTRAINT ck_asiento_numero CHECK (numero > 0),
    CONSTRAINT ck_asiento_estado CHECK (estado IN ('LIBRE', 'RESERVADO', 'VENDIDO'))
) ENGINE=InnoDB;

CREATE TABLE reserva (
    id BIGINT NOT NULL AUTO_INCREMENT,
    viaje_id BIGINT NOT NULL,
    pasajero_id BIGINT NOT NULL,
    asiento_id BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    fecha_reserva DATETIME NOT NULL,
    estado VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reserva_viaje FOREIGN KEY (viaje_id) REFERENCES viaje (id),
    CONSTRAINT fk_reserva_pasajero FOREIGN KEY (pasajero_id) REFERENCES pasajero (id),
    CONSTRAINT fk_reserva_asiento FOREIGN KEY (asiento_id) REFERENCES asiento (id),
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_reserva_estado CHECK (estado IN ('RESERVADA', 'VENDIDA', 'ANULADA'))
) ENGINE=InnoDB;

CREATE TABLE pasaje (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reserva_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    precio DECIMAL(8,2) NOT NULL,
    fecha_venta DATETIME NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    usuario_id BIGINT NULL,
    estado VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_pasaje_reserva UNIQUE (reserva_id),
    CONSTRAINT uq_pasaje_codigo UNIQUE (codigo),
    CONSTRAINT fk_pasaje_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id),
    CONSTRAINT fk_pasaje_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_pasaje_precio CHECK (precio > 0),
    CONSTRAINT ck_pasaje_metodo CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'BILLETERA_DIGITAL')),
    CONSTRAINT ck_pasaje_estado CHECK (estado IN ('EMITIDO', 'ANULADO'))
) ENGINE=InnoDB;

ALTER TABLE usuario ADD CONSTRAINT fk_usuario_conductor FOREIGN KEY (conductor_id) REFERENCES conductor (id);

CREATE TABLE incidencia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    viaje_id BIGINT NOT NULL,
    tipo VARCHAR(15) NOT NULL,
    descripcion VARCHAR(500) NULL,
    minutos_retraso INT NULL,
    fecha_registro DATETIME NOT NULL,
    usuario_id BIGINT NULL,
    atendida BIT(1) NOT NULL DEFAULT b'0',
    atendida_por_id BIGINT NULL,
    fecha_atencion DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_incidencia_viaje FOREIGN KEY (viaje_id) REFERENCES viaje (id),
    CONSTRAINT fk_incidencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_incidencia_atendida_por FOREIGN KEY (atendida_por_id) REFERENCES usuario (id),
    CONSTRAINT ck_incidencia_tipo CHECK (tipo IN ('INICIO', 'ACCIDENTE', 'RETRASO', 'PAUSA', 'REANUDACION', 'OBSERVACION', 'LLEGADA')),
    CONSTRAINT ck_incidencia_minutos CHECK (minutos_retraso IS NULL OR minutos_retraso BETWEEN 1 AND 1440)
) ENGINE=InnoDB;

CREATE INDEX idx_incidencia_pendiente ON incidencia (atendida, tipo, fecha_registro);
CREATE INDEX idx_viaje_llegada_estado ON viaje (estado, fecha_llegada);
CREATE INDEX idx_pasajero_apellidos ON pasajero (apellidos, nombres);
CREATE INDEX idx_conductor_apellidos ON conductor (apellidos, nombres);
CREATE INDEX idx_viaje_fecha_salida ON viaje (fecha_salida);
CREATE INDEX idx_viaje_estado ON viaje (estado);
CREATE INDEX idx_viaje_bus_fechas ON viaje (bus_id, fecha_salida, fecha_llegada);
CREATE INDEX idx_viaje_conductor_fechas ON viaje (conductor_id, fecha_salida, fecha_llegada);
CREATE INDEX idx_asiento_viaje_estado ON asiento (viaje_id, estado);
CREATE INDEX idx_reserva_estado ON reserva (estado);
CREATE INDEX idx_reserva_fecha ON reserva (fecha_reserva);
CREATE INDEX idx_pasaje_fecha_venta ON pasaje (fecha_venta);

INSERT INTO destino (id, nombre, departamento, terminal) VALUES
(1, 'Lima', 'Lima', 'Terminal Lima Centro'),
(2, 'Arequipa', 'Arequipa', 'Terminal Terrestre Arequipa'),
(3, 'Cusco', 'Cusco', 'Terminal Terrestre Cusco'),
(4, 'Trujillo', 'La Libertad', 'Terminal Trujillo Norte'),
(5, 'Piura', 'Piura', 'Terminal Terrestre Piura'),
(6, 'Chiclayo', 'Lambayeque', 'Terminal Terrestre Chiclayo');

INSERT INTO ruta (id, origen_id, destino_id, distancia_km, duracion_horas, precio_base) VALUES
(1, 1, 2, 1010, 16.0, 110.00),
(2, 2, 1, 1010, 16.0, 110.00),
(3, 1, 3, 1120, 21.0, 150.00),
(4, 3, 1, 1120, 21.0, 150.00),
(5, 1, 4, 560, 8.5, 70.00),
(6, 4, 1, 560, 8.5, 70.00),
(7, 1, 5, 980, 15.0, 120.00),
(8, 5, 1, 980, 15.0, 120.00),
(9, 1, 6, 770, 12.0, 85.00),
(10, 6, 1, 770, 12.0, 85.00),
(11, 2, 3, 520, 10.0, 65.00),
(12, 3, 2, 520, 10.0, 65.00),
(13, 4, 6, 210, 3.5, 30.00),
(14, 6, 4, 210, 3.5, 30.00),
(15, 6, 5, 220, 3.5, 30.00),
(16, 5, 6, 220, 3.5, 30.00);

INSERT INTO bus (id, placa, marca, modelo, capacidad, anio_fabricacion) VALUES
(1, 'AUD-581', 'Mercedes-Benz', 'O 500 RS', 40, 2021),
(2, 'BKX-204', 'Volvo', 'B8R', 44, 2022),
(3, 'CDF-772', 'Scania', 'K400', 48, 2020),
(4, 'D2P-318', 'Mercedes-Benz', 'O 500 RSD', 44, 2019),
(5, 'F5L-640', 'Volvo', 'B11R', 40, 2023),
(6, 'G7H-905', 'Scania', 'K410', 48, 2022),
(7, 'H3R-127', 'Volkswagen', '18.320 OT', 36, 2018),
(8, 'J8M-446', 'Mercedes-Benz', 'O 500 RS', 40, 2020),
(9, 'K1N-863', 'Volvo', 'B8R', 44, 2024),
(10, 'L9T-052', 'Scania', 'K360', 52, 2021);

INSERT INTO conductor (id, dni, nombres, apellidos, licencia, categoria_licencia, telefono) VALUES
(1, '41230987', 'Carlos Alberto', 'Quispe Mamani', 'Q41230987', 'A-IIIc', '958123456'),
(2, '09876543', 'Jorge Luis', 'Ramírez Salazar', 'Q09876543', 'A-IIIc', '965432109'),
(3, '29384756', 'Luis Fernando', 'Huamán Torres', 'Q29384756', 'A-IIIb', '947856321'),
(4, '31245678', 'Miguel Ángel', 'Vargas Cáceres', 'Q31245678', 'A-IIIc', '981236547'),
(5, '40567812', 'José Antonio', 'Flores Condori', 'Q40567812', 'A-IIIc', '934567812'),
(6, '25678431', 'Raúl Eduardo', 'Paredes Villanueva', 'Q25678431', 'A-IIIb', '976543218'),
(7, '17893456', 'Víctor Hugo', 'Chávez Rojas', 'Q17893456', 'A-IIIc', '912345678'),
(8, '33456721', 'Pedro Pablo', 'Mendoza Alarcón', 'Q33456721', 'A-IIIb', '956781234');

INSERT INTO pasajero (id, dni, nombres, apellidos, telefono, correo, fecha_nacimiento) VALUES
(1, '45218790', 'Lucía Fernanda', 'Rojas Salazar', '987654321', 'lucia.rojas@correo.pe', '1992-03-14'),
(2, '70345128', 'Diego Armando', 'Chávez Torres', '954321876', 'diego.chavez@correo.pe', '1988-11-02'),
(3, '43876512', 'María Elena', 'Quispe Huamán', '976123450', 'maria.quispe@correo.pe', '1979-07-25'),
(4, '72091834', 'Jorge Luis', 'Paredes Cáceres', '965874123', NULL, '1995-01-30'),
(5, '46127395', 'Rosa Isabel', 'Mendoza Flores', '943218765', 'rosa.mendoza@correo.pe', '1984-09-09'),
(6, '71563280', 'Andrea Valeria', 'Campos Ríos', '912876543', 'andrea.campos@correo.pe', '2000-05-18'),
(7, '40982716', 'Julio César', 'Medina Vargas', '998123765', NULL, '1976-12-03'),
(8, '73418265', 'Karen Milagros', 'Alarcón Díaz', '931456782', 'karen.alarcon@correo.pe', '1998-08-21'),
(9, '44019532', 'Pedro Pablo', 'Condori Ccori', '956321478', NULL, '1981-04-11'),
(10, '75120936', 'Sofía Alejandra', 'Vega Pizarro', '982145637', 'sofia.vega@correo.pe', '2002-10-07'),
(11, '41756328', 'Héctor Manuel', 'Salas Benavides', '947123658', 'hector.salas@correo.pe', '1973-02-16'),
(12, '72845016', 'Gabriela Nicole', 'Torres Luna', '921987456', 'gabriela.torres@correo.pe', '1996-06-27'),
(13, '45903187', 'Raúl Ernesto', 'Villanueva Ortiz', '963258741', NULL, '1987-12-19'),
(14, '76231945', 'Camila Estefany', 'Ramos Gutiérrez', '974185236', 'camila.ramos@correo.pe', '2001-03-05'),
(15, '42367815', 'Walter Anselmo', 'Huanca Apaza', '958741236', NULL, '1969-09-28'),
(16, '70918432', 'Valeria Lucero', 'Cárdenas Soto', '936852147', 'valeria.cardenas@correo.pe', '1999-01-13'),
(17, '47182563', 'Fernando José', 'Aguilar Montoya', '945632187', 'fernando.aguilar@correo.pe', '1991-07-08'),
(18, '73654021', 'Milagros del Pilar', 'Soto Zevallos', '987412365', NULL, '1994-11-24'),
(19, '44825671', 'Óscar Daniel', 'Ponce Cabrera', '952147893', 'oscar.ponce@correo.pe', '1985-05-02'),
(20, '74309158', 'Natalia Beatriz', 'Espinoza Linares', '961478523', 'natalia.espinoza@correo.pe', '2003-08-15');

INSERT INTO viaje (id, ruta_id, bus_id, conductor_id, fecha_salida, fecha_llegada, precio, estado)
SELECT p.id, p.ruta_id, p.bus_id, p.conductor_id, p.salida,
       DATE_ADD(p.salida, INTERVAL CAST(ROUND(r.duracion_horas * 60) AS SIGNED) MINUTE),
       r.precio_base + p.recargo, p.estado
FROM (
    SELECT 1 AS id, 1 AS ruta_id, 1 AS bus_id, 1 AS conductor_id, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '20:00:00') AS salida, 0 AS recargo, 'FINALIZADO' AS estado
    UNION ALL SELECT 2, 5, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '22:00:00'), 0, 'FINALIZADO'
    UNION ALL SELECT 3, 9, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '19:30:00'), 5, 'FINALIZADO'
    UNION ALL SELECT 4, 3, 4, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '15:00:00'), 10, 'FINALIZADO'
    UNION ALL SELECT 5, 7, 5, 5, DATE_SUB(NOW(), INTERVAL 4 HOUR), 0, 'EN_CURSO'
    UNION ALL SELECT 6, 2, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 7, 4, 4, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 10, 'PROGRAMADO'
    UNION ALL SELECT 8, 6, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '21:00:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 9, 10, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '20:00:00'), 5, 'PROGRAMADO'
    UNION ALL SELECT 10, 1, 6, 6, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 11, 5, 7, 7, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '22:30:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 12, 11, 8, 8, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '07:00:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 13, 7, 9, 5, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '18:00:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 14, 9, 10, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '21:00:00'), 5, 'PROGRAMADO'
    UNION ALL SELECT 15, 13, 5, 6, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '09:00:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 16, 12, 8, 8, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '06:30:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 17, 3, 1, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00'), 10, 'PROGRAMADO'
    UNION ALL SELECT 18, 15, 9, 7, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '12:00:00'), 0, 'PROGRAMADO'
    UNION ALL SELECT 19, 14, 10, 4, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00'), 0, 'CANCELADO'
) p
JOIN ruta r ON r.id = p.ruta_id;

CREATE TEMPORARY TABLE plan_reserva (
    id BIGINT NOT NULL,
    viaje_id BIGINT NOT NULL,
    numero INT NOT NULL,
    pasajero_id BIGINT NOT NULL,
    fecha_reserva DATETIME NOT NULL,
    estado VARCHAR(10) NOT NULL
);

INSERT INTO plan_reserva (id, viaje_id, numero, pasajero_id, fecha_reserva, estado) VALUES
(1, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 8 DAY), 'VENDIDA'),
(2, 1, 2, 2, DATE_SUB(NOW(), INTERVAL 8 DAY), 'VENDIDA'),
(3, 1, 7, 3, DATE_SUB(NOW(), INTERVAL 7 DAY), 'VENDIDA'),
(4, 1, 8, 4, DATE_SUB(NOW(), INTERVAL 7 DAY), 'VENDIDA'),
(5, 2, 3, 5, DATE_SUB(NOW(), INTERVAL 7 DAY), 'VENDIDA'),
(6, 2, 4, 6, DATE_SUB(NOW(), INTERVAL 6 DAY), 'VENDIDA'),
(7, 2, 10, 7, DATE_SUB(NOW(), INTERVAL 6 DAY), 'VENDIDA'),
(8, 3, 5, 8, DATE_SUB(NOW(), INTERVAL 6 DAY), 'VENDIDA'),
(9, 3, 6, 9, DATE_SUB(NOW(), INTERVAL 5 DAY), 'VENDIDA'),
(10, 4, 12, 10, DATE_SUB(NOW(), INTERVAL 6 DAY), 'VENDIDA'),
(11, 4, 13, 11, DATE_SUB(NOW(), INTERVAL 5 DAY), 'VENDIDA'),
(12, 5, 1, 12, DATE_SUB(NOW(), INTERVAL 3 DAY), 'VENDIDA'),
(13, 5, 2, 13, DATE_SUB(NOW(), INTERVAL 3 DAY), 'VENDIDA'),
(14, 5, 9, 14, DATE_SUB(NOW(), INTERVAL 2 DAY), 'VENDIDA'),
(15, 6, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'VENDIDA'),
(16, 6, 2, 15, DATE_SUB(NOW(), INTERVAL 1 DAY), 'RESERVADA'),
(17, 6, 5, 16, DATE_SUB(NOW(), INTERVAL 2 DAY), 'ANULADA'),
(18, 7, 3, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), 'VENDIDA'),
(19, 7, 4, 17, DATE_SUB(NOW(), INTERVAL 1 DAY), 'RESERVADA'),
(20, 8, 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), 'VENDIDA'),
(21, 8, 2, 18, DATE_SUB(NOW(), INTERVAL 1 DAY), 'VENDIDA'),
(22, 9, 7, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), 'RESERVADA'),
(23, 10, 11, 19, DATE_SUB(NOW(), INTERVAL 1 DAY), 'VENDIDA'),
(24, 10, 12, 20, DATE_SUB(NOW(), INTERVAL 1 DAY), 'RESERVADA'),
(25, 11, 1, 5, DATE_SUB(NOW(), INTERVAL 1 DAY), 'VENDIDA'),
(26, 12, 6, 6, DATE_SUB(NOW(), INTERVAL 1 DAY), 'VENDIDA'),
(27, 13, 8, 7, DATE_SUB(NOW(), INTERVAL 1 DAY), 'RESERVADA'),
(28, 14, 3, 8, DATE_SUB(NOW(), INTERVAL 1 DAY), 'ANULADA');

INSERT INTO asiento (viaje_id, numero, estado)
SELECT v.id, n.numero,
       CASE pr.estado WHEN 'VENDIDA' THEN 'VENDIDO' WHEN 'RESERVADA' THEN 'RESERVADO' ELSE 'LIBRE' END
FROM viaje v
JOIN bus b ON b.id = v.bus_id
JOIN (
    SELECT d1.d + d2.d * 10 + 1 AS numero
    FROM (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d1
    CROSS JOIN (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
                UNION ALL SELECT 4 UNION ALL SELECT 5) d2
) n ON n.numero <= b.capacidad
LEFT JOIN plan_reserva pr ON pr.viaje_id = v.id AND pr.numero = n.numero
ORDER BY v.id, n.numero;

INSERT INTO reserva (id, viaje_id, pasajero_id, asiento_id, usuario_id, fecha_reserva, estado)
SELECT pr.id, pr.viaje_id, pr.pasajero_id, a.id, NULL, pr.fecha_reserva, pr.estado
FROM plan_reserva pr
JOIN asiento a ON a.viaje_id = pr.viaje_id AND a.numero = pr.numero
ORDER BY pr.id;

INSERT INTO pasaje (id, reserva_id, codigo, precio, fecha_venta, metodo_pago, usuario_id, estado)
SELECT r.id, r.id, CONCAT('PJ-', LPAD(r.id, 8, '0')), v.precio, r.fecha_reserva,
       ELT(1 + MOD(r.id, 4), 'EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'BILLETERA_DIGITAL'),
       NULL, IF(r.estado = 'ANULADA', 'ANULADO', 'EMITIDO')
FROM reserva r
JOIN viaje v ON v.id = r.viaje_id
WHERE r.estado = 'VENDIDA' OR r.id = 28
ORDER BY r.id;

DROP TEMPORARY TABLE plan_reserva;
