package pe.transportes.gestion.servicio;

import java.time.LocalDateTime;
import pe.transportes.gestion.modelo.Incidencia;
import pe.transportes.gestion.modelo.Viaje;

public record AlertaViaje(Viaje viaje, Incidencia incidencia, String titulo, String detalle, LocalDateTime fecha,
        String nivel) {

    public boolean atendible() {
        return incidencia != null;
    }
}
