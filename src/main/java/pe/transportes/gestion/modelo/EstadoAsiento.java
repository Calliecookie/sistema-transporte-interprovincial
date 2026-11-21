package pe.transportes.gestion.modelo;

public enum EstadoAsiento {
    LIBRE("Libre"),
    RESERVADO("Reservado"),
    VENDIDO("Vendido");

    private final String etiqueta;

    EstadoAsiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
