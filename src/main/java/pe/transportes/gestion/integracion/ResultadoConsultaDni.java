package pe.transportes.gestion.integracion;

import pe.transportes.gestion.modelo.Pasajero;

public record ResultadoConsultaDni(boolean encontrado, boolean registrado, String nombres, String apellidoPaterno,
        String apellidoMaterno, String apellidos, String departamento, String provincia, String distrito,
        String direccion, String direccionCompleta, String fechaNacimiento, String sexo, String telefono,
        String correo, String mensaje) {

    public static ResultadoConsultaDni encontrado(PersonaDni persona) {
        return new ResultadoConsultaDni(true, false, persona.nombres(), persona.apellidoPaterno(),
                persona.apellidoMaterno(), persona.apellidos(), persona.departamento(), persona.provincia(),
                persona.distrito(), persona.direccion(), persona.direccionCompleta(),
                persona.fechaNacimiento() == null ? null : persona.fechaNacimiento().toString(),
                persona.sexo() == null ? null : persona.sexo().name(), null, null, "Datos obtenidos de RENIEC");
    }

    public static ResultadoConsultaDni yaRegistrado(Pasajero pasajero, String mensaje) {
        return new ResultadoConsultaDni(true, true, pasajero.getNombres(), pasajero.getApellidoPaterno(),
                pasajero.getApellidoMaterno(), pasajero.getApellidos(), pasajero.getDepartamento(),
                pasajero.getProvincia(), pasajero.getDistrito(), pasajero.getDireccion(),
                pasajero.getDireccionCompleta(),
                pasajero.getFechaNacimiento() == null ? null : pasajero.getFechaNacimiento().toString(),
                pasajero.getSexo() == null ? null : pasajero.getSexo().name(), pasajero.getTelefono(),
                pasajero.getCorreo(), mensaje);
    }

    public static ResultadoConsultaDni yaRegistrado(String nombres, String apellidos, String mensaje) {
        return new ResultadoConsultaDni(true, true, nombres, null, null, apellidos, null, null, null, null, null,
                null, null, null, null, mensaje);
    }

    public static ResultadoConsultaDni sinResultado(String mensaje) {
        return new ResultadoConsultaDni(false, false, null, null, null, null, null, null, null, null, null, null,
                null, null, null, mensaje);
    }
}
