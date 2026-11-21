package pe.transportes.gestion.servicio;

import java.math.BigDecimal;

public record ResumenReporteViajes(long viajes, long pasajesVendidos, long asientosReservados, BigDecimal ingresos) {
}
