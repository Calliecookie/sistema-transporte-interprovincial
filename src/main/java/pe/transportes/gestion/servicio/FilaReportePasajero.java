package pe.transportes.gestion.servicio;

import pe.transportes.gestion.modelo.Pasaje;
import pe.transportes.gestion.modelo.Reserva;

public record FilaReportePasajero(Reserva reserva, Pasaje pasaje) {
}
