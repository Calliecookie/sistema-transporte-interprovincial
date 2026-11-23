package pe.transportes.gestion.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Asiento;
import pe.transportes.gestion.modelo.Bus;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.modelo.EstadoAsiento;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Ruta;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.repositorio.AsientoRepositorio;
import pe.transportes.gestion.repositorio.BusRepositorio;
import pe.transportes.gestion.repositorio.ConductorRepositorio;
import pe.transportes.gestion.repositorio.EspecificacionViaje;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.RutaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.util.RangoFechas;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class ServicioViaje {

    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);

    private final ViajeRepositorio viajeRepositorio;
    private final RutaRepositorio rutaRepositorio;
    private final BusRepositorio busRepositorio;
    private final ConductorRepositorio conductorRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final ServicioReserva servicioReserva;
    private final ValidadorEntidades validador;
    private final Clock reloj;

    public ServicioViaje(ViajeRepositorio viajeRepositorio, RutaRepositorio rutaRepositorio,
            BusRepositorio busRepositorio, ConductorRepositorio conductorRepositorio,
            AsientoRepositorio asientoRepositorio, ReservaRepositorio reservaRepositorio,
            ServicioReserva servicioReserva, ValidadorEntidades validador, Clock reloj) {
        this.viajeRepositorio = viajeRepositorio;
        this.rutaRepositorio = rutaRepositorio;
        this.busRepositorio = busRepositorio;
        this.conductorRepositorio = conductorRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.servicioReserva = servicioReserva;
        this.validador = validador;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public Page<Viaje> buscar(LocalDate desde, LocalDate hasta, Long rutaId, EstadoViaje estado,
            Pageable paginacion) {
        RangoFechas.validar(desde, hasta);
        Page<Viaje> pagina = viajeRepositorio.findAll(EspecificacionViaje.filtrar(desde, hasta, rutaId, estado),
                paginacion);
        completarDisponibilidad(pagina.getContent());
        return pagina;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public Page<Viaje> buscarDisponibles(LocalDate fecha, Long rutaId, Pageable paginacion) {
        Page<Viaje> pagina = viajeRepositorio.findAll(
                EspecificacionViaje.disponibles(LocalDateTime.now(reloj), fecha, rutaId), paginacion);
        completarDisponibilidad(pagina.getContent());
        return pagina;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public List<Viaje> proximosProgramados() {
        List<Viaje> viajes = viajeRepositorio.findTop6ByEstadoAndFechaSalidaAfterOrderByFechaSalidaAsc(
                EstadoViaje.PROGRAMADO, LocalDateTime.now(reloj));
        completarDisponibilidad(viajes);
        return viajes;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public Viaje obtener(Long id) {
        return viajeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el viaje solicitado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public Viaje obtenerConDisponibilidad(Long id) {
        Viaje viaje = obtener(id);
        viaje.setAsientosLibres((int) asientoRepositorio.countByViajeIdAndEstado(id, EstadoAsiento.LIBRE));
        return viaje;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public List<Asiento> asientos(Long viajeId) {
        return asientoRepositorio.findByViajeIdOrderByNumeroAsc(viajeId);
    }

    @Transactional
    public Viaje programar(Viaje datos) {
        validador.validar(datos);
        Ruta ruta = buscarRuta(datos);
        Bus bus = buscarBus(datos);
        Conductor conductor = buscarConductor(datos);
        LocalDateTime salida = datos.getFechaSalida();
        if (!salida.isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("fechaSalida",
                    "La fecha de salida debe ser posterior a la fecha y hora actual");
        }
        LocalDateTime llegada = calcularLlegada(salida, ruta);
        verificarDisponibilidad(bus, conductor, salida, llegada, 0L);
        Viaje viaje = new Viaje();
        copiar(datos, viaje, ruta, bus, conductor, salida, llegada);
        viaje.setEstado(EstadoViaje.PROGRAMADO);
        Viaje guardado = viajeRepositorio.save(viaje);
        generarAsientos(guardado, bus.getCapacidad());
        return guardado;
    }

    @Transactional
    public Viaje actualizar(Long id, Viaje datos) {
        validador.validar(datos);
        Viaje viaje = obtener(id);
        if (viaje.getEstado() != EstadoViaje.PROGRAMADO) {
            throw new ReglaNegocioException("Solo se pueden modificar los viajes que están programados");
        }
        Ruta ruta = buscarRuta(datos);
        Bus bus = buscarBus(datos);
        Conductor conductor = buscarConductor(datos);
        LocalDateTime salida = datos.getFechaSalida();
        if (!salida.equals(viaje.getFechaSalida()) && !salida.isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("fechaSalida",
                    "La fecha de salida debe ser posterior a la fecha y hora actual");
        }
        LocalDateTime llegada = calcularLlegada(salida, ruta);
        verificarDisponibilidad(bus, conductor, salida, llegada, id);
        boolean cambioBus = !viaje.getBus().getId().equals(bus.getId());
        boolean cambioRuta = !viaje.getRuta().getId().equals(ruta.getId());
        boolean tieneReservas = reservaRepositorio.existsByViajeId(id);
        if (cambioBus && tieneReservas) {
            throw new ReglaNegocioException("idBus", "No se puede cambiar el bus porque el viaje ya tiene reservas");
        }
        if (cambioRuta && tieneReservas) {
            throw new ReglaNegocioException("idRuta", "No se puede cambiar la ruta porque el viaje ya tiene reservas");
        }
        copiar(datos, viaje, ruta, bus, conductor, salida, llegada);
        Viaje guardado = viajeRepositorio.save(viaje);
        if (cambioBus) {
            asientoRepositorio.eliminarPorViaje(id);
            generarAsientos(guardado, bus.getCapacidad());
        }
        return guardado;
    }

    @Transactional
    public Viaje iniciar(Long id) {
        Viaje viaje = obtener(id);
        if (viaje.getEstado() != EstadoViaje.PROGRAMADO) {
            throw new ReglaNegocioException("Solo se puede iniciar un viaje que está programado");
        }
        viaje.setEstado(EstadoViaje.EN_CURSO);
        return viajeRepositorio.save(viaje);
    }

    @Transactional
    public Viaje finalizar(Long id) {
        Viaje viaje = obtener(id);
        if (!viaje.getEstado().isEnRuta()) {
            throw new ReglaNegocioException("Solo se puede finalizar un viaje que está en ruta");
        }
        viaje.setEstado(EstadoViaje.FINALIZADO);
        return viajeRepositorio.save(viaje);
    }

    @Transactional
    public Viaje cancelar(Long id) {
        Viaje viaje = obtener(id);
        if (viaje.getEstado() != EstadoViaje.PROGRAMADO) {
            throw new ReglaNegocioException("Solo se puede cancelar un viaje que está programado");
        }
        servicioReserva.anularActivasDelViaje(id);
        viaje.setEstado(EstadoViaje.CANCELADO);
        return viajeRepositorio.save(viaje);
    }

    @Transactional
    public void eliminar(Long id) {
        Viaje viaje = obtener(id);
        if (reservaRepositorio.existsByViajeId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar un viaje con reservas registradas. Puede cancelarlo en su lugar");
        }
        asientoRepositorio.eliminarPorViaje(id);
        viajeRepositorio.delete(viaje);
    }

    private void completarDisponibilidad(List<Viaje> viajes) {
        if (viajes.isEmpty()) {
            return;
        }
        List<Long> ids = viajes.stream().map(Viaje::getId).toList();
        Map<Long, Long> libres = new HashMap<>();
        for (Object[] fila : asientoRepositorio.contarPorViajes(ids, EstadoAsiento.LIBRE)) {
            libres.put((Long) fila[0], (Long) fila[1]);
        }
        for (Viaje viaje : viajes) {
            viaje.setAsientosLibres(libres.getOrDefault(viaje.getId(), 0L).intValue());
        }
    }

    private Ruta buscarRuta(Viaje datos) {
        if (datos.getRuta() == null || datos.getRuta().getId() == null) {
            throw new ReglaNegocioException("idRuta", "Seleccione una ruta");
        }
        Ruta ruta = rutaRepositorio.findById(datos.getRuta().getId())
                .orElseThrow(() -> new ReglaNegocioException("idRuta", "La ruta seleccionada no existe"));
        if (!ruta.isActiva()) {
            throw new ReglaNegocioException("idRuta", "La ruta seleccionada está inactiva");
        }
        return ruta;
    }

    private Bus buscarBus(Viaje datos) {
        if (datos.getBus() == null || datos.getBus().getId() == null) {
            throw new ReglaNegocioException("idBus", "Seleccione un bus");
        }
        Bus bus = busRepositorio.findById(datos.getBus().getId())
                .orElseThrow(() -> new ReglaNegocioException("idBus", "El bus seleccionado no existe"));
        if (!bus.isActivo()) {
            throw new ReglaNegocioException("idBus", "El bus seleccionado está inactivo");
        }
        return bus;
    }

    private Conductor buscarConductor(Viaje datos) {
        if (datos.getConductor() == null || datos.getConductor().getId() == null) {
            throw new ReglaNegocioException("idConductor", "Seleccione un conductor");
        }
        Conductor conductor = conductorRepositorio.findById(datos.getConductor().getId())
                .orElseThrow(() -> new ReglaNegocioException("idConductor", "El conductor seleccionado no existe"));
        if (!conductor.isActivo()) {
            throw new ReglaNegocioException("idConductor", "El conductor seleccionado está inactivo");
        }
        return conductor;
    }

    private LocalDateTime calcularLlegada(LocalDateTime salida, Ruta ruta) {
        long minutos = ruta.getDuracionHoras().multiply(MINUTOS_POR_HORA).setScale(0, RoundingMode.HALF_UP)
                .longValue();
        return salida.plusMinutes(minutos);
    }

    private void verificarDisponibilidad(Bus bus, Conductor conductor, LocalDateTime salida, LocalDateTime llegada,
            Long excluirId) {
        if (viajeRepositorio.contarCrucesDeBus(bus.getId(), salida, llegada, EstadoViaje.CANCELADO, excluirId) > 0) {
            throw new ReglaNegocioException("idBus", "El bus ya tiene un viaje asignado en ese horario");
        }
        if (viajeRepositorio.contarCrucesDeConductor(conductor.getId(), salida, llegada, EstadoViaje.CANCELADO,
                excluirId) > 0) {
            throw new ReglaNegocioException("idConductor", "El conductor ya tiene un viaje asignado en ese horario");
        }
    }

    private void copiar(Viaje datos, Viaje viaje, Ruta ruta, Bus bus, Conductor conductor, LocalDateTime salida,
            LocalDateTime llegada) {
        viaje.setRuta(ruta);
        viaje.setBus(bus);
        viaje.setConductor(conductor);
        viaje.setFechaSalida(salida);
        viaje.setFechaLlegada(llegada);
        viaje.setPrecio(datos.getPrecio());
    }

    private void generarAsientos(Viaje viaje, int capacidad) {
        List<Asiento> asientos = new ArrayList<>(capacidad);
        for (int numero = 1; numero <= capacidad; numero++) {
            Asiento asiento = new Asiento();
            asiento.setViaje(viaje);
            asiento.setNumero(numero);
            asiento.setEstado(EstadoAsiento.LIBRE);
            asientos.add(asiento);
        }
        asientoRepositorio.saveAll(asientos);
    }
}
