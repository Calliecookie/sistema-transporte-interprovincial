package pe.transportes.gestion.integracion;

import java.time.LocalDate;

public record PersonaDni(String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
        LocalDate fechaNacimiento) {

    public String apellidos() {
        return (apellidoPaterno + " " + apellidoMaterno).strip();
    }
}
