package pe.transportes.gestion.integracion;

import java.time.LocalDate;
import pe.transportes.gestion.modelo.Sexo;

public record PersonaDni(String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
        LocalDate fechaNacimiento, String departamento, String provincia, String distrito, String direccion,
        String direccionCompleta, Sexo sexo) {

    public String apellidos() {
        return (apellidoPaterno + " " + apellidoMaterno).strip();
    }
}
