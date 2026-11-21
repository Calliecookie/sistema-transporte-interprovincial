package pe.transportes.gestion.modelo;

public enum AccionReserva {
    RESERVAR("Solo reservar"),
    VENDER("Vender el pasaje ahora");

    private final String etiqueta;

    AccionReserva(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
