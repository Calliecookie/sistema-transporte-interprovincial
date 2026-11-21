package pe.transportes.gestion.servicio;

import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.EstadoPasaje;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.repositorio.BusRepositorio;
import pe.transportes.gestion.repositorio.ConductorRepositorio;
import pe.transportes.gestion.repositorio.PasajeRepositorio;
import pe.transportes.gestion.repositorio.PasajeroRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ServicioPanel {

    private final PasajeroRepositorio pasajeroRepositorio;
    private final BusRepositorio busRepositorio;
    private final ConductorRepositorio conductorRepositorio;
    private final ViajeRepositorio viajeRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final PasajeRepositorio pasajeRepositorio;

    public ServicioPanel(PasajeroRepositorio pasajeroRepositorio, BusRepositorio busRepositorio,
            ConductorRepositorio conductorRepositorio, ViajeRepositorio viajeRepositorio,
            ReservaRepositorio reservaRepositorio, PasajeRepositorio pasajeRepositorio) {
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.busRepositorio = busRepositorio;
        this.conductorRepositorio = conductorRepositorio;
        this.viajeRepositorio = viajeRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.pasajeRepositorio = pasajeRepositorio;
    }

    @Transactional(readOnly = true)
    public ResumenPanel resumen() {
        BigDecimal ingresos = pasajeRepositorio.sumarPrecios(EstadoPasaje.EMITIDO);
        return new ResumenPanel(
                pasajeroRepositorio.count(),
                busRepositorio.countByActivoTrue(),
                conductorRepositorio.countByActivoTrue(),
                viajeRepositorio.countByEstado(EstadoViaje.PROGRAMADO),
                reservaRepositorio.countByEstado(EstadoReserva.RESERVADA),
                pasajeRepositorio.countByEstado(EstadoPasaje.EMITIDO),
                ingresos == null ? BigDecimal.ZERO : ingresos);
    }
}
