package pe.transportes.gestion.modelo;

public enum TipoIncidencia {
    INICIO("Inicio del viaje", false),
    ACCIDENTE("Accidente en ruta", true),
    RETRASO("Retraso", true),
    PAUSA("Pausa del viaje", true),
    REANUDACION("Reanudación del viaje", false),
    OBSERVACION("Observación", false),
    LLEGADA("Llegada a destino", false);

    private final String etiqueta;
    private final boolean alerta;

    TipoIncidencia(String etiqueta, boolean alerta) {
        this.etiqueta = etiqueta;
        this.alerta = alerta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean isAlerta() {
        return alerta;
    }
}
