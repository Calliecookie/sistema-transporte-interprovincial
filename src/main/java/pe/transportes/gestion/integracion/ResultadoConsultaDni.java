package pe.transportes.gestion.integracion;

public record ResultadoConsultaDni(boolean encontrado, boolean registrado, String nombres, String apellidos,
        String fechaNacimiento, String mensaje) {

    public static ResultadoConsultaDni encontrado(PersonaDni persona) {
        return new ResultadoConsultaDni(true, false, persona.nombres(), persona.apellidos(),
                persona.fechaNacimiento() == null ? null : persona.fechaNacimiento().toString(),
                "Datos obtenidos de RENIEC");
    }

    public static ResultadoConsultaDni yaRegistrado(String nombres, String apellidos, String mensaje) {
        return new ResultadoConsultaDni(true, true, nombres, apellidos, null, mensaje);
    }

    public static ResultadoConsultaDni sinResultado(String mensaje) {
        return new ResultadoConsultaDni(false, false, null, null, null, mensaje);
    }
}
