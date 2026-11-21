package pe.transportes.gestion.servicio;

import java.math.BigDecimal;
import pe.transportes.gestion.modelo.Viaje;

public record FilaReporteViaje(Viaje viaje, int vendidos, int reservados, int libres, BigDecimal ingresos) {
}
