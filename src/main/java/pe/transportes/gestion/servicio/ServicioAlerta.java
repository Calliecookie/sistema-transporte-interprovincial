package pe.transportes.gestion.servicio;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Incidencia;
import pe.transportes.gestion.modelo.TipoIncidencia;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.repositorio.IncidenciaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.seguridad.UsuarioActual;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ServicioAlerta {

    public static final long MINUTOS_TOLERANCIA_LLEGADA = 60;

    private static final EnumSet<TipoIncidencia> TIPOS_ALERTA = EnumSet.of(TipoIncidencia.ACCIDENTE,
            TipoIncidencia.RETRASO, TipoIncidencia.PAUSA);
    private static final EnumSet<EstadoViaje> SIN_CERRAR = EnumSet.of(EstadoViaje.PROGRAMADO, EstadoViaje.EN_CURSO,
            EstadoViaje.EN_PAUSA, EstadoViaje.RETRASADO, EstadoViaje.INCIDENTE);

    private final IncidenciaRepositorio incidenciaRepositorio;
    private final ViajeRepositorio viajeRepositorio;
    private final UsuarioActual usuarioActual;
    private final Clock reloj;

    public ServicioAlerta(IncidenciaRepositorio incidenciaRepositorio, ViajeRepositorio viajeRepositorio,
            UsuarioActual usuarioActual, Clock reloj) {
        this.incidenciaRepositorio = incidenciaRepositorio;
        this.viajeRepositorio = viajeRepositorio;
        this.usuarioActual = usuarioActual;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<AlertaViaje> pendientes() {
        List<AlertaViaje> alertas = new ArrayList<>();
        for (Incidencia incidencia : incidenciaRepositorio.findByAtendidaFalseAndTipoInOrderByFechaRegistroDesc(
                TIPOS_ALERTA)) {
            alertas.add(desdeIncidencia(incidencia));
        }
        for (Viaje viaje : sinLlegada()) {
            long minutos = Duration.between(viaje.getFechaLlegada(), LocalDateTime.now(reloj)).toMinutes();
            alertas.add(new AlertaViaje(viaje, null, "Bus sin confirmar llegada",
                    "Debió llegar hace " + formatearMinutos(minutos) + " y el conductor no confirmó la llegada.",
                    viaje.getFechaLlegada(), "critica"));
        }
        alertas.sort(Comparator.comparing(AlertaViaje::fecha).reversed());
        return alertas;
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        return incidenciaRepositorio.countByAtendidaFalseAndTipoIn(TIPOS_ALERTA) + sinLlegada().size();
    }

    @Transactional(readOnly = true)
    public List<Incidencia> atendidasRecientes() {
        return incidenciaRepositorio.findTop20ByAtendidaTrueAndTipoInOrderByFechaAtencionDesc(TIPOS_ALERTA);
    }

    @Transactional(readOnly = true)
    public List<Incidencia> delViaje(Long viajeId) {
        return incidenciaRepositorio.findByViajeIdOrderByFechaRegistroDesc(viajeId);
    }

    @Transactional
    public void atender(Long id) {
        Incidencia incidencia = incidenciaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la alerta solicitada"));
        if (incidencia.isAtendida()) {
            throw new ReglaNegocioException("La alerta ya fue atendida");
        }
        incidencia.setAtendida(true);
        incidencia.setAtendidaPor(usuarioActual.obtener().orElse(null));
        incidencia.setFechaAtencion(LocalDateTime.now(reloj));
        incidenciaRepositorio.save(incidencia);
    }

    private List<Viaje> sinLlegada() {
        LocalDateTime limite = LocalDateTime.now(reloj).minusMinutes(MINUTOS_TOLERANCIA_LLEGADA);
        return viajeRepositorio.findByEstadoInAndFechaLlegadaBeforeOrderByFechaLlegadaAsc(SIN_CERRAR, limite);
    }

    private AlertaViaje desdeIncidencia(Incidencia incidencia) {
        String detalle = incidencia.getDescripcion() == null ? "" : incidencia.getDescripcion();
        if (incidencia.getMinutosRetraso() != null) {
            detalle = "Retraso estimado de " + formatearMinutos(incidencia.getMinutosRetraso()) + ". " + detalle;
        }
        String nivel = incidencia.getTipo() == TipoIncidencia.ACCIDENTE ? "critica" : "advertencia";
        return new AlertaViaje(incidencia.getViaje(), incidencia, incidencia.getTipo().getEtiqueta(), detalle.strip(),
                incidencia.getFechaRegistro(), nivel);
    }

    private String formatearMinutos(long minutos) {
        if (minutos < 60) {
            return minutos + " min";
        }
        long horas = minutos / 60;
        long resto = minutos % 60;
        return resto == 0 ? horas + " h" : horas + " h " + resto + " min";
    }
}
