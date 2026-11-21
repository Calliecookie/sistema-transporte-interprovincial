package pe.transportes.gestion.servicio;

import java.math.BigDecimal;

public record ResumenPanel(long pasajeros, long busesActivos, long conductoresActivos, long viajesProgramados,
        long reservasPendientes, long pasajesEmitidos, BigDecimal ingresos) {
}
