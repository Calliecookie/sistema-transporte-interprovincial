package pe.transportes.gestion.util;

import java.util.UUID;

public final class GeneradorCodigo {

    private GeneradorCodigo() {
    }

    public static String temporal() {
        return "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 19);
    }

    public static String codigoPasaje(Long id) {
        return String.format("PJ-%08d", id);
    }
}
