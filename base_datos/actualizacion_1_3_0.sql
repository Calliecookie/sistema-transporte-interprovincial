USE transporte_interprovincial;
SET NAMES utf8mb4;

ALTER TABLE pasajero
    ADD COLUMN apellido_paterno VARCHAR(39) NULL AFTER nombres,
    ADD COLUMN apellido_materno VARCHAR(39) NULL AFTER apellido_paterno,
    ADD COLUMN sexo VARCHAR(10) NULL,
    ADD COLUMN departamento VARCHAR(60) NULL,
    ADD COLUMN provincia VARCHAR(60) NULL,
    ADD COLUMN distrito VARCHAR(60) NULL,
    ADD COLUMN direccion VARCHAR(150) NULL,
    ADD COLUMN direccion_completa VARCHAR(250) NULL,
    ADD CONSTRAINT ck_pasajero_sexo CHECK (sexo IS NULL OR sexo IN ('MASCULINO', 'FEMENINO'));

UPDATE pasajero
SET apellido_paterno = SUBSTRING_INDEX(apellidos, ' ', 1),
    apellido_materno = CASE WHEN LOCATE(' ', apellidos) > 0 THEN TRIM(SUBSTRING(apellidos, LOCATE(' ', apellidos) + 1)) ELSE NULL END
WHERE apellido_paterno IS NULL;
