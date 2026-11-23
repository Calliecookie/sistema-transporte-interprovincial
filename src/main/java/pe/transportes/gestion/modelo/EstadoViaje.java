package pe.transportes.gestion.modelo;

public enum EstadoViaje {
    PROGRAMADO("Programado"),
    EN_CURSO("En curso"),
    EN_PAUSA("En pausa"),
    RETRASADO("Retrasado"),
    INCIDENTE("Detenido por incidente"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoViaje(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean isEnRuta() {
        return this == EN_CURSO || this == EN_PAUSA || this == RETRASADO || this == INCIDENTE;
    }
}
