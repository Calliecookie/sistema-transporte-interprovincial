package pe.transportes.gestion.modelo;

public enum Rol {
    ADMIN("Administrador"),
    VENDEDOR("Vendedor"),
    CONDUCTOR("Conductor");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
