package pe.transportes.gestion.controlador;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import pe.transportes.gestion.modelo.Asiento;
import pe.transportes.gestion.modelo.EstadoAsiento;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.servicio.ServicioReserva;
import pe.transportes.gestion.servicio.ServicioViaje;

@Component
public class ConstructorMapa {

    private final ServicioViaje servicioViaje;
    private final ServicioReserva servicioReserva;

    public ConstructorMapa(ServicioViaje servicioViaje, ServicioReserva servicioReserva) {
        this.servicioViaje = servicioViaje;
        this.servicioReserva = servicioReserva;
    }

    public void cargar(Model modelo, Long viajeId) {
        Viaje viaje = servicioViaje.obtenerConDisponibilidad(viajeId);
        List<Asiento> asientos = servicioViaje.asientos(viajeId);
        modelo.addAttribute("viaje", viaje);
        modelo.addAttribute("asientos", asientos);
        modelo.addAttribute("reservasPorAsiento", servicioReserva.activasPorAsiento(viajeId));
        modelo.addAttribute("ventaAbierta", servicioReserva.ventaAbierta(viaje));
        modelo.addAttribute("totalReservados", contar(asientos, EstadoAsiento.RESERVADO));
        modelo.addAttribute("totalVendidos", contar(asientos, EstadoAsiento.VENDIDO));
    }

    private long contar(List<Asiento> asientos, EstadoAsiento estado) {
        return asientos.stream().filter(asiento -> asiento.getEstado() == estado).count();
    }
}
