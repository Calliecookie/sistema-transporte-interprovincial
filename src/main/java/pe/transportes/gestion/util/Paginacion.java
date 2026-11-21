package pe.transportes.gestion.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Paginacion {

    public static final int TAMANO = 10;

    private Paginacion() {
    }

    public static Pageable de(int pagina, Sort orden) {
        return PageRequest.of(Math.max(pagina, 0), TAMANO, orden);
    }
}
