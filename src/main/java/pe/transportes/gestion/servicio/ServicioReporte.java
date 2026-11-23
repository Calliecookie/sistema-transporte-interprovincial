package pe.transportes.gestion.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.EstadoAsiento;
import pe.transportes.gestion.modelo.EstadoPasaje;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Pasaje;
import pe.transportes.gestion.modelo.Reserva;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.repositorio.AsientoRepositorio;
import pe.transportes.gestion.repositorio.EspecificacionReserva;
import pe.transportes.gestion.repositorio.EspecificacionViaje;
import pe.transportes.gestion.repositorio.PasajeRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.util.ExportadorCsv;
import pe.transportes.gestion.util.RangoFechas;
import pe.transportes.gestion.util.RecursoNoEncontradoException;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ServicioReporte {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ViajeRepositorio viajeRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final PasajeRepositorio pasajeRepositorio;

    public ServicioReporte(ViajeRepositorio viajeRepositorio, ReservaRepositorio reservaRepositorio,
            AsientoRepositorio asientoRepositorio, PasajeRepositorio pasajeRepositorio) {
        this.viajeRepositorio = viajeRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.pasajeRepositorio = pasajeRepositorio;
    }

    @Transactional(readOnly = true)
    public Page<FilaReporteViaje> viajes(LocalDate desde, LocalDate hasta, Long rutaId, EstadoViaje estado,
            Pageable paginacion) {
        RangoFechas.validar(desde, hasta);
        Page<Viaje> pagina = viajeRepositorio.findAll(EspecificacionViaje.filtrar(desde, hasta, rutaId, estado),
                paginacion);
        return new PageImpl<>(construirFilasViaje(pagina.getContent()), paginacion, pagina.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ResumenReporteViajes resumenViajes(LocalDate desde, LocalDate hasta, Long rutaId, EstadoViaje estado) {
        List<FilaReporteViaje> filas = todosLosViajes(desde, hasta, rutaId, estado);
        long vendidos = filas.stream().mapToLong(FilaReporteViaje::vendidos).sum();
        long reservados = filas.stream().mapToLong(FilaReporteViaje::reservados).sum();
        BigDecimal ingresos = filas.stream().map(FilaReporteViaje::ingresos).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenReporteViajes(filas.size(), vendidos, reservados, ingresos);
    }

    @Transactional(readOnly = true)
    public byte[] csvViajes(LocalDate desde, LocalDate hasta, Long rutaId, EstadoViaje estado) {
        List<String> cabeceras = List.of("N.° de viaje", "Origen", "Destino", "Salida", "Llegada", "Bus", "Conductor",
                "Estado", "Precio (S/)", "Asientos totales", "Vendidos", "Reservados", "Libres", "Ingresos (S/)");
        List<List<String>> datos = todosLosViajes(desde, hasta, rutaId, estado).stream().map(fila -> {
            Viaje viaje = fila.viaje();
            return List.of(
                    String.valueOf(viaje.getId()),
                    viaje.getRuta().getOrigen().getNombre(),
                    viaje.getRuta().getDestino().getNombre(),
                    viaje.getFechaSalida().format(FORMATO_FECHA_HORA),
                    viaje.getFechaLlegada().format(FORMATO_FECHA_HORA),
                    viaje.getBus().getPlaca(),
                    viaje.getConductor().getNombreCompleto(),
                    viaje.getEstado().getEtiqueta(),
                    viaje.getPrecio().toPlainString(),
                    String.valueOf(viaje.getAsientosTotales()),
                    String.valueOf(fila.vendidos()),
                    String.valueOf(fila.reservados()),
                    String.valueOf(fila.libres()),
                    fila.ingresos().toPlainString());
        }).toList();
        return ExportadorCsv.generar(cabeceras, datos);
    }

    @Transactional(readOnly = true)
    public Page<FilaReportePasajero> pasajeros(String dni, EstadoReserva estado, Long rutaId, LocalDate desde,
            LocalDate hasta, Pageable paginacion) {
        RangoFechas.validar(desde, hasta);
        Page<Reserva> pagina = reservaRepositorio.findAll(
                EspecificacionReserva.filtrar(dni, estado, rutaId, null, null, desde, hasta), paginacion);
        return new PageImpl<>(construirFilasPasajero(pagina.getContent()), paginacion, pagina.getTotalElements());
    }

    @Transactional(readOnly = true)
    public byte[] csvPasajeros(String dni, EstadoReserva estado, Long rutaId, LocalDate desde, LocalDate hasta) {
        RangoFechas.validar(desde, hasta);
        List<Reserva> reservas = reservaRepositorio.findAll(
                EspecificacionReserva.filtrar(dni, estado, rutaId, null, null, desde, hasta), ordenPasajeros());
        List<String> cabeceras = List.of("DNI", "Pasajero", "Teléfono", "Origen", "Destino", "Salida", "Asiento",
                "Estado de la reserva", "Código del pasaje", "Monto (S/)", "Método de pago", "Estado del pasaje");
        List<List<String>> datos = construirFilasPasajero(reservas).stream().map(fila -> {
            Reserva reserva = fila.reserva();
            Pasaje pasaje = fila.pasaje();
            return List.of(
                    reserva.getPasajero().getDni(),
                    reserva.getPasajero().getNombreCompleto(),
                    textoODefecto(reserva.getPasajero().getTelefono()),
                    reserva.getViaje().getRuta().getOrigen().getNombre(),
                    reserva.getViaje().getRuta().getDestino().getNombre(),
                    reserva.getViaje().getFechaSalida().format(FORMATO_FECHA_HORA),
                    String.valueOf(reserva.getAsiento().getNumero()),
                    reserva.getEstado().getEtiqueta(),
                    pasaje == null ? "" : pasaje.getCodigo(),
                    pasaje == null ? "" : pasaje.getPrecio().toPlainString(),
                    pasaje == null ? "" : pasaje.getMetodoPago().getEtiqueta(),
                    pasaje == null ? "" : pasaje.getEstado().getEtiqueta());
        }).toList();
        return ExportadorCsv.generar(cabeceras, datos);
    }

    @Transactional(readOnly = true)
    public Viaje viaje(Long viajeId) {
        return viajeRepositorio.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el viaje solicitado"));
    }

    @Transactional(readOnly = true)
    public FilaReporteViaje ocupacionDelViaje(Long viajeId) {
        return construirFilasViaje(List.of(viaje(viajeId))).get(0);
    }

    @Transactional(readOnly = true)
    public List<FilaReportePasajero> pasajerosDelViaje(Long viajeId) {
        viaje(viajeId);
        List<Reserva> reservas = reservaRepositorio.findByViajeIdAndEstadoNot(viajeId, EstadoReserva.ANULADA)
                .stream()
                .sorted(Comparator.comparing(reserva -> reserva.getAsiento().getNumero()))
                .toList();
        return construirFilasPasajero(reservas);
    }

    @Transactional(readOnly = true)
    public byte[] csvPasajerosDelViaje(Long viajeId) {
        List<String> cabeceras = List.of("Asiento", "DNI", "Pasajero", "Teléfono", "Estado de la reserva",
                "Código del pasaje", "Monto (S/)", "Método de pago");
        List<List<String>> datos = pasajerosDelViaje(viajeId).stream().map(fila -> {
            Reserva reserva = fila.reserva();
            Pasaje pasaje = fila.pasaje();
            return List.of(
                    String.valueOf(reserva.getAsiento().getNumero()),
                    reserva.getPasajero().getDni(),
                    reserva.getPasajero().getNombreCompleto(),
                    textoODefecto(reserva.getPasajero().getTelefono()),
                    reserva.getEstado().getEtiqueta(),
                    pasaje == null ? "" : pasaje.getCodigo(),
                    pasaje == null ? "" : pasaje.getPrecio().toPlainString(),
                    pasaje == null ? "" : pasaje.getMetodoPago().getEtiqueta());
        }).toList();
        return ExportadorCsv.generar(cabeceras, datos);
    }

    public static Sort ordenPasajeros() {
        return Sort.by(Sort.Order.desc("viaje.fechaSalida"), Sort.Order.asc("asiento.numero"));
    }

    private String textoODefecto(String texto) {
        return texto == null ? "" : texto;
    }

    private List<FilaReporteViaje> todosLosViajes(LocalDate desde, LocalDate hasta, Long rutaId,
            EstadoViaje estado) {
        RangoFechas.validar(desde, hasta);
        List<Viaje> viajes = viajeRepositorio.findAll(EspecificacionViaje.filtrar(desde, hasta, rutaId, estado),
                Sort.by(Sort.Direction.DESC, "fechaSalida"));
        return construirFilasViaje(viajes);
    }

    private List<FilaReporteViaje> construirFilasViaje(List<Viaje> viajes) {
        if (viajes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = viajes.stream().map(Viaje::getId).toList();
        Map<Long, Long> vendidos = contar(ids, EstadoAsiento.VENDIDO);
        Map<Long, Long> reservados = contar(ids, EstadoAsiento.RESERVADO);
        Map<Long, BigDecimal> ingresos = new HashMap<>();
        for (Object[] fila : pasajeRepositorio.sumarPreciosPorViaje(ids, EstadoPasaje.EMITIDO)) {
            ingresos.put((Long) fila[0], (BigDecimal) fila[1]);
        }
        return viajes.stream().map(viaje -> {
            int totalVendidos = vendidos.getOrDefault(viaje.getId(), 0L).intValue();
            int totalReservados = reservados.getOrDefault(viaje.getId(), 0L).intValue();
            int libres = viaje.getAsientosTotales() - totalVendidos - totalReservados;
            return new FilaReporteViaje(viaje, totalVendidos, totalReservados, libres,
                    ingresos.getOrDefault(viaje.getId(), BigDecimal.ZERO));
        }).toList();
    }

    private Map<Long, Long> contar(List<Long> ids, EstadoAsiento estado) {
        Map<Long, Long> conteo = new HashMap<>();
        for (Object[] fila : asientoRepositorio.contarPorViajes(ids, estado)) {
            conteo.put((Long) fila[0], (Long) fila[1]);
        }
        return conteo;
    }

    private List<FilaReportePasajero> construirFilasPasajero(List<Reserva> reservas) {
        if (reservas.isEmpty()) {
            return List.of();
        }
        List<Long> ids = reservas.stream().map(Reserva::getId).toList();
        Map<Long, Pasaje> pasajes = new HashMap<>();
        for (Pasaje pasaje : pasajeRepositorio.findByReservaIdIn(ids)) {
            pasajes.put(pasaje.getReserva().getId(), pasaje);
        }
        return reservas.stream().map(reserva -> new FilaReportePasajero(reserva, pasajes.get(reserva.getId())))
                .toList();
    }
}
