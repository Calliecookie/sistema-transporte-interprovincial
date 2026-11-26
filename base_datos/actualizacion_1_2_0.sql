USE transporte_interprovincial;
SET NAMES utf8mb4;

ALTER TABLE usuario
    ADD COLUMN correo VARCHAR(100) NULL,
    ADD CONSTRAINT uq_usuario_correo UNIQUE (correo);

CREATE TABLE permiso_rol (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rol VARCHAR(20) NOT NULL,
    modulo VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_permiso_rol_modulo UNIQUE (rol, modulo),
    CONSTRAINT ck_permiso_rol_rol CHECK (rol IN ('ADMIN', 'VENDEDOR', 'CONDUCTOR')),
    CONSTRAINT ck_permiso_modulo CHECK (modulo IN ('PASAJEROS', 'VENTAS', 'PASAJES', 'REPORTES', 'ALERTAS', 'MIS_VIAJES'))
) ENGINE=InnoDB;

INSERT INTO permiso_rol (rol, modulo) VALUES
('VENDEDOR', 'PASAJEROS'),
('VENDEDOR', 'VENTAS'),
('VENDEDOR', 'PASAJES'),
('VENDEDOR', 'REPORTES'),
('VENDEDOR', 'ALERTAS'),
('CONDUCTOR', 'MIS_VIAJES');

CREATE TABLE token_recuperacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    huella VARCHAR(64) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    usado BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    CONSTRAINT uq_token_huella UNIQUE (huella),
    CONSTRAINT fk_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE INDEX idx_token_usuario ON token_recuperacion (usuario_id, fecha_creacion);
