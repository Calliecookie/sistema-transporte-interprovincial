package pe.transportes.gestion.modelo;

public enum EstadoViaje {
    PROGRAMADO("Programado"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoViaje(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
