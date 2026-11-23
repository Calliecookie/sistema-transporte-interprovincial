package pe.transportes.gestion.integracion;

public record PersonaDni(String dni, String nombres, String apellidoPaterno, String apellidoMaterno) {

    public String apellidos() {
        return (apellidoPaterno + " " + apellidoMaterno).strip();
    }
}
