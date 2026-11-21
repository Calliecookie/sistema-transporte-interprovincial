package pe.transportes.gestion.servicio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.AccionReserva;
import pe.transportes.gestion.modelo.Asiento;
import pe.transportes.gestion.modelo.EstadoAsiento;
import pe.transportes.gestion.modelo.EstadoPasaje;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.FormularioReserva;
import pe.transportes.gestion.modelo.MetodoPago;
import pe.transportes.gestion.modelo.Pasaje;
import pe.transportes.gestion.modelo.Pasajero;
import pe.transportes.gestion.modelo.Reserva;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.repositorio.AsientoRepositorio;
import pe.transportes.gestion.repositorio.EspecificacionReserva;
import pe.transportes.gestion.repositorio.PasajeRepositorio;
import pe.transportes.gestion.repositorio.PasajeroRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.seguridad.UsuarioActual;
import pe.transportes.gestion.util.GeneradorCodigo;
import pe.transportes.gestion.util.RangoFechas;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ServicioReserva {

    private final ReservaRepositorio reservaRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final PasajeroRepositorio pasajeroRepositorio;
    private final ViajeRepositorio viajeRepositorio;
    private final PasajeRepositorio pasajeRepositorio;
    private final ValidadorEntidades validador;
    private final UsuarioActual usuarioActual;
    private final Clock reloj;

    public ServicioReserva(ReservaRepositorio reservaRepositorio, AsientoRepositorio asientoRepositorio,
            PasajeroRepositorio pasajeroRepositorio, ViajeRepositorio viajeRepositorio,
            PasajeRepositorio pasajeRepositorio, ValidadorEntidades validador, UsuarioActual usuarioActual,
            Clock reloj) {
        this.reservaRepositorio = reservaRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.viajeRepositorio = viajeRepositorio;
        this.pasajeRepositorio = pasajeRepositorio;
        this.validador = validador;
        this.usuarioActual = usuarioActual;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public Page<Reserva> buscar(String dni, EstadoReserva estado, Long rutaId, LocalDate desde, LocalDate hasta,
            Pageable paginacion) {
        RangoFechas.validar(desde, hasta);
        return reservaRepositorio.findAll(
                EspecificacionReserva.filtrar(dni, estado, rutaId, desde, hasta, null, null), paginacion);
    }

    @Transactional(readOnly = true)
    public Reserva obtener(Long id) {
        return reservaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la reserva solicitada"));
    }

    @Transactional(readOnly = true)
    public Map<Long, Reserva> activasPorAsiento(Long viajeId) {
        return reservaRepositorio.findByViajeIdAndEstadoNot(viajeId, EstadoReserva.ANULADA).stream()
                .collect(Collectors.toMap(r -> r.getAsiento().getId(), Function.identity(), (a, b) -> a));
    }

    @Transactional(readOnly = true)
    public Pasaje pasajeDeReserva(Long reservaId) {
        return pasajeRepositorio.findByReservaId(reservaId).orElse(null);
    }

    public boolean ventaAbierta(Viaje viaje) {
        return viaje.getEstado() == EstadoViaje.PROGRAMADO
                && viaje.getFechaSalida().isAfter(LocalDateTime.now(reloj));
    }

    @Transactional(readOnly = true)
    public Asiento buscarAsiento(Long viajeId, Long asientoId) {
        return asientoRepositorio.findById(asientoId)
                .filter(asiento -> asiento.getViaje().getId().equals(viajeId))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Asiento obtenerAsientoLibre(Long viajeId, Long asientoId) {
        Viaje viaje = viajeRepositorio.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje indicado no existe"));
        Asiento asiento = asientoRepositorio.findById(asientoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El asiento indicado no existe"));
        if (!asiento.getViaje().getId().equals(viaje.getId())) {
            throw new ReglaNegocioException("El asiento no pertenece al viaje seleccionado");
        }
        if (!ventaAbierta(viaje)) {
            throw new ReglaNegocioException("El viaje ya no admite reservas ni ventas");
        }
        if (asiento.getEstado() != EstadoAsiento.LIBRE) {
            throw new ReglaNegocioException("El asiento " + asiento.getNumero() + " ya está ocupado");
        }
        return asiento;
    }

    @Transactional
    public Reserva registrar(FormularioReserva formulario) {
        validador.validar(formulario);
        boolean venta = formulario.getAccion() == AccionReserva.VENDER;
        if (venta && formulario.getMetodoPago() == null) {
            throw new ReglaNegocioException("metodoPago", "Seleccione el método de pago");
        }
        Viaje viaje = viajeRepositorio.findById(formulario.getViajeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje indicado no existe"));
        if (!ventaAbierta(viaje)) {
            throw new ReglaNegocioException("El viaje ya no admite reservas ni ventas");
        }
        Pasajero pasajero = pasajeroRepositorio.findByDni(formulario.getDni())
                .orElseThrow(() -> new ReglaNegocioException("dni", "No existe un pasajero registrado con ese DNI"));
        if (reservaRepositorio.existsByViajeIdAndPasajeroIdAndEstadoNot(viaje.getId(), pasajero.getId(),
                EstadoReserva.ANULADA)) {
            throw new ReglaNegocioException("dni", "El pasajero ya tiene un asiento en este viaje");
        }
        Asiento asiento = asientoRepositorio.buscarParaActualizar(formulario.getAsientoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El asiento indicado no existe"));
        if (!asiento.getViaje().getId().equals(viaje.getId())) {
            throw new ReglaNegocioException("El asiento no pertenece al viaje seleccionado");
        }
        if (asiento.getEstado() != EstadoAsiento.LIBRE) {
            throw new ReglaNegocioException("El asiento " + asiento.getNumero() + " ya está ocupado");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setPasajero(pasajero);
        reserva.setAsiento(asiento);
        reserva.setUsuario(usuarioActual.obtener().orElse(null));
        reserva.setFechaReserva(ahora);
        reserva.setEstado(venta ? EstadoReserva.VENDIDA : EstadoReserva.RESERVADA);
        asiento.setEstado(venta ? EstadoAsiento.VENDIDO : EstadoAsiento.RESERVADO);
        asientoRepositorio.save(asiento);
        Reserva guardada = reservaRepositorio.save(reserva);
        if (venta) {
            emitirPasaje(guardada, formulario.getMetodoPago(), ahora);
        }
        return guardada;
    }

    @Transactional
    public Reserva confirmarVenta(Long reservaId, MetodoPago metodoPago) {
        Reserva reserva = obtener(reservaId);
        if (reserva.getEstado() != EstadoReserva.RESERVADA) {
            throw new ReglaNegocioException("Solo se puede vender una reserva que esté en estado reservada");
        }
        if (!ventaAbierta(reserva.getViaje())) {
            throw new ReglaNegocioException("El viaje ya no admite reservas ni ventas");
        }
        if (metodoPago == null) {
            throw new ReglaNegocioException("Seleccione el método de pago");
        }
        Asiento asiento = asientoRepositorio.buscarParaActualizar(reserva.getAsiento().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El asiento indicado no existe"));
        asiento.setEstado(EstadoAsiento.VENDIDO);
        asientoRepositorio.save(asiento);
        reserva.setEstado(EstadoReserva.VENDIDA);
        Reserva guardada = reservaRepositorio.save(reserva);
        emitirPasaje(guardada, metodoPago, LocalDateTime.now(reloj));
        return guardada;
    }

    @Transactional
    public Reserva anular(Long reservaId) {
        Reserva reserva = obtener(reservaId);
        if (reserva.getEstado() == EstadoReserva.ANULADA) {
            throw new ReglaNegocioException("La reserva ya fue anulada");
        }
        if (reserva.getViaje().getEstado() != EstadoViaje.PROGRAMADO) {
            throw new ReglaNegocioException("Solo se pueden anular reservas de viajes programados");
        }
        return liberar(reserva);
    }

    @Transactional
    public void anularActivasDelViaje(Long viajeId) {
        List<Reserva> activas = reservaRepositorio.findByViajeIdAndEstadoNot(viajeId, EstadoReserva.ANULADA);
        for (Reserva reserva : activas) {
            liberar(reserva);
        }
    }

    private Reserva liberar(Reserva reserva) {
        Asiento asiento = asientoRepositorio.buscarParaActualizar(reserva.getAsiento().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El asiento indicado no existe"));
        asiento.setEstado(EstadoAsiento.LIBRE);
        asientoRepositorio.save(asiento);
        reserva.setEstado(EstadoReserva.ANULADA);
        Reserva guardada = reservaRepositorio.save(reserva);
        pasajeRepositorio.findByReservaId(reserva.getId()).ifPresent(pasaje -> {
            pasaje.setEstado(EstadoPasaje.ANULADO);
            pasajeRepositorio.save(pasaje);
        });
        return guardada;
    }

    private Pasaje emitirPasaje(Reserva reserva, MetodoPago metodoPago, LocalDateTime fechaVenta) {
        Pasaje pasaje = new Pasaje();
        pasaje.setReserva(reserva);
        pasaje.setCodigo(GeneradorCodigo.temporal());
        pasaje.setPrecio(reserva.getViaje().getPrecio());
        pasaje.setFechaVenta(fechaVenta);
        pasaje.setMetodoPago(metodoPago);
        pasaje.setUsuario(usuarioActual.obtener().orElse(null));
        pasaje.setEstado(EstadoPasaje.EMITIDO);
        Pasaje guardado = pasajeRepositorio.save(pasaje);
        guardado.setCodigo(GeneradorCodigo.codigoPasaje(guardado.getId()));
        return pasajeRepositorio.save(guardado);
    }
}
