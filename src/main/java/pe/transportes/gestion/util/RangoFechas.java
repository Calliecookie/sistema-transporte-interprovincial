package pe.transportes.gestion.util;

import java.time.LocalDate;

public final class RangoFechas {

    private RangoFechas() {
    }

    public static void validar(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }
}
