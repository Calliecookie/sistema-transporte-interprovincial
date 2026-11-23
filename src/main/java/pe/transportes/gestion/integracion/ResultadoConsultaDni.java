package pe.transportes.gestion.integracion;

public record ResultadoConsultaDni(boolean encontrado, boolean registrado, String nombres, String apellidos,
        String mensaje) {

    public static ResultadoConsultaDni encontrado(PersonaDni persona) {
        return new ResultadoConsultaDni(true, false, persona.nombres(), persona.apellidos(),
                "Datos obtenidos de RENIEC");
    }

    public static ResultadoConsultaDni yaRegistrado(String nombres, String apellidos, String mensaje) {
        return new ResultadoConsultaDni(true, true, nombres, apellidos, mensaje);
    }

    public static ResultadoConsultaDni sinResultado(String mensaje) {
        return new ResultadoConsultaDni(false, false, null, null, mensaje);
    }
}
