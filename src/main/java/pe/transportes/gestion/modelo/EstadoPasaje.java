package pe.transportes.gestion.modelo;

public enum EstadoPasaje {
    EMITIDO("Emitido"),
    ANULADO("Anulado");

    private final String etiqueta;

    EstadoPasaje(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
