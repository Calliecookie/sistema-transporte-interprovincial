package pe.transportes.gestion.modelo;

public enum EstadoReserva {
    RESERVADA("Reservada"),
    VENDIDA("Vendida"),
    ANULADA("Anulada");

    private final String etiqueta;

    EstadoReserva(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
