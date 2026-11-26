package pe.transportes.gestion.modelo;

public enum Sexo {
    MASCULINO("Masculino"),
    FEMENINO("Femenino");

    private final String etiqueta;

    Sexo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Sexo desde(String texto) {
        if (texto == null) {
            return null;
        }
        switch (texto.strip().toUpperCase()) {
            case "M", "H", "1", "MASCULINO", "HOMBRE":
                return MASCULINO;
            case "F", "2", "FEMENINO", "MUJER":
                return FEMENINO;
            default:
                return null;
        }
    }
}
