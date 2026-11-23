USE transporte_interprovincial;
SET NAMES utf8mb4;

ALTER TABLE usuario DROP CHECK ck_usuario_rol;
ALTER TABLE usuario
    ADD COLUMN conductor_id BIGINT NULL,
    ADD CONSTRAINT uq_usuario_conductor UNIQUE (conductor_id),
    ADD CONSTRAINT fk_usuario_conductor FOREIGN KEY (conductor_id) REFERENCES conductor (id),
    ADD CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN', 'VENDEDOR', 'CONDUCTOR'));

ALTER TABLE viaje DROP CHECK ck_viaje_estado;
ALTER TABLE viaje
    ADD CONSTRAINT ck_viaje_estado CHECK (estado IN ('PROGRAMADO', 'EN_CURSO', 'EN_PAUSA', 'RETRASADO', 'INCIDENTE', 'FINALIZADO', 'CANCELADO'));

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
