package pe.transportes.gestion.util;

import java.util.Locale;

public final class Patrones {

    private Patrones() {
    }

    public static boolean tieneTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    public static String contiene(String texto) {
        String limpio = texto.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + limpio + "%";
    }
}
