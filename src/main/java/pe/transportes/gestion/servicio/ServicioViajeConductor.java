package pe.transportes.gestion.servicio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.modelo.EstadoAsiento;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Incidencia;
import pe.transportes.gestion.modelo.Reserva;
import pe.transportes.gestion.modelo.TipoIncidencia;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.repositorio.AsientoRepositorio;
import pe.transportes.gestion.repositorio.IncidenciaRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.seguridad.UsuarioActual;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;

@Service
@PreAuthorize("hasRole('CONDUCTOR')")
public class ServicioViajeConductor {

    private static final EnumSet<EstadoViaje> PENDIENTES = EnumSet.of(EstadoViaje.PROGRAMADO, EstadoViaje.EN_CURSO,
            EstadoViaje.EN_PAUSA, EstadoViaje.RETRASADO, EstadoViaje.INCIDENTE);
    private static final EnumSet<EstadoViaje> CERRADOS = EnumSet.of(EstadoViaje.FINALIZADO, EstadoViaje.CANCELADO);
    private static final int LONGITUD_MAXIMA = 500;

    private final ViajeRepositorio viajeRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final IncidenciaRepositorio incidenciaRepositorio;
    private final UsuarioActual usuarioActual;
    private final Clock reloj;

    public ServicioViajeConductor(ViajeRepositorio viajeRepositorio, AsientoRepositorio asientoRepositorio,
            ReservaRepositorio reservaRepositorio, IncidenciaRepositorio incidenciaRepositorio,
            UsuarioActual usuarioActual, Clock reloj) {
        this.viajeRepositorio = viajeRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.incidenciaRepositorio = incidenciaRepositorio;
        this.usuarioActual = usuarioActual;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public Conductor conductorActual() {
        return usuarioConductor().getConductor();
    }

    @Transactional(readOnly = true)
    public List<Viaje> viajesPendientes() {
        List<Viaje> viajes = viajeRepositorio.findByConductorIdAndEstadoInOrderByFechaSalidaAsc(
                conductorActual().getId(), PENDIENTES);
        viajes.forEach(this::completarOcupacion);
        return viajes;
    }

    @Transactional(readOnly = true)
    public List<Viaje> viajesRecientes() {
        return viajeRepositorio.findTop10ByConductorIdAndEstadoInOrderByFechaSalidaDesc(conductorActual().getId(),
                CERRADOS);
    }

    @Transactional(readOnly = true)
    public Viaje obtener(Long id) {
        Viaje viaje = viajePropio(id);
        completarOcupacion(viaje);
        return viaje;
    }

    @Transactional(readOnly = true)
    public List<Reserva> pasajeros(Long viajeId) {
        viajePropio(viajeId);
        return reservaRepositorio.findByViajeIdAndEstadoNot(viajeId, EstadoReserva.ANULADA).stream()
                .sorted(Comparator.comparing(reserva -> reserva.getAsiento().getNumero()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Incidencia> incidencias(Long viajeId) {
        viajePropio(viajeId);
        return incidenciaRepositorio.findByViajeIdOrderByFechaRegistroDesc(viajeId);
    }

    @Transactional
    public void iniciar(Long id, String comentario) {
        Viaje viaje = viajePropio(id);
        if (viaje.getEstado() != EstadoViaje.PROGRAMADO) {
            throw new ReglaNegocioException("Solo se puede iniciar un viaje que está programado");
        }
        cambiarEstado(viaje, EstadoViaje.EN_CURSO, TipoIncidencia.INICIO, comentario, null);
    }

    @Transactional
    public void pausar(Long id, String comentario) {
        Viaje viaje = viajeEnRuta(id);
        if (viaje.getEstado() == EstadoViaje.EN_PAUSA) {
            throw new ReglaNegocioException("El viaje ya se encuentra en pausa");
        }
        cambiarEstado(viaje, EstadoViaje.EN_PAUSA, TipoIncidencia.PAUSA, exigirComentario(comentario,
                "Indique el motivo de la pausa"), null);
    }

    @Transactional
    public void reanudar(Long id, String comentario) {
        Viaje viaje = viajeEnRuta(id);
        if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
            throw new ReglaNegocioException("El viaje ya se encuentra en curso");
        }
        cambiarEstado(viaje, EstadoViaje.EN_CURSO, TipoIncidencia.REANUDACION, comentario, null);
    }

    @Transactional
    public void reportarRetraso(Long id, Integer minutos, String comentario) {
        Viaje viaje = viajeEnRuta(id);
        if (minutos == null || minutos < 1 || minutos > 1440) {
            throw new ReglaNegocioException("Indique los minutos de retraso (entre 1 y 1440)");
        }
        cambiarEstado(viaje, EstadoViaje.RETRASADO, TipoIncidencia.RETRASO, exigirComentario(comentario,
                "Indique el motivo del retraso"), minutos);
    }

    @Transactional
    public void reportarAccidente(Long id, String comentario) {
        Viaje viaje = viajeEnRuta(id);
        cambiarEstado(viaje, EstadoViaje.INCIDENTE, TipoIncidencia.ACCIDENTE, exigirComentario(comentario,
                "Describa brevemente el accidente y la ubicación"), null);
    }

    @Transactional
    public void registrarObservacion(Long id, String comentario) {
        Viaje viaje = viajePropio(id);
        if (CERRADOS.contains(viaje.getEstado())) {
            throw new ReglaNegocioException("No se pueden registrar observaciones en un viaje cerrado");
        }
        registrar(viaje, TipoIncidencia.OBSERVACION, exigirComentario(comentario, "Escriba la observación"), null);
    }

    @Transactional
    public void finalizar(Long id, String comentario) {
        Viaje viaje = viajeEnRuta(id);
        if (viaje.getEstado() == EstadoViaje.INCIDENTE) {
            throw new ReglaNegocioException(
                    "El viaje está detenido por un incidente. Reanúdelo antes de confirmar la llegada");
        }
        cambiarEstado(viaje, EstadoViaje.FINALIZADO, TipoIncidencia.LLEGADA, comentario, null);
    }

    private void cambiarEstado(Viaje viaje, EstadoViaje estado, TipoIncidencia tipo, String comentario,
            Integer minutos) {
        viaje.setEstado(estado);
        viajeRepositorio.save(viaje);
        registrar(viaje, tipo, comentario, minutos);
    }

    private void registrar(Viaje viaje, TipoIncidencia tipo, String comentario, Integer minutos) {
        Incidencia incidencia = new Incidencia();
        incidencia.setViaje(viaje);
        incidencia.setTipo(tipo);
        incidencia.setDescripcion(limpiar(comentario));
        incidencia.setMinutosRetraso(minutos);
        incidencia.setFechaRegistro(LocalDateTime.now(reloj));
        incidencia.setUsuario(usuarioConductor());
        incidencia.setAtendida(!tipo.isAlerta());
        incidenciaRepositorio.save(incidencia);
    }

    private String exigirComentario(String comentario, String mensaje) {
        if (comentario == null || comentario.isBlank()) {
            throw new ReglaNegocioException(mensaje);
        }
        return comentario;
    }

    private String limpiar(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        String texto = comentario.strip();
        if (texto.length() > LONGITUD_MAXIMA) {
            throw new ReglaNegocioException("El comentario no puede superar los " + LONGITUD_MAXIMA + " caracteres");
        }
        return texto;
    }

    private Viaje viajeEnRuta(Long id) {
        Viaje viaje = viajePropio(id);
        if (!viaje.getEstado().isEnRuta()) {
            throw new ReglaNegocioException("Esta acción solo está disponible cuando el viaje está en ruta");
        }
        return viaje;
    }

    private Viaje viajePropio(Long id) {
        Conductor conductor = conductorActual();
        Viaje viaje = viajeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el viaje solicitado"));
        if (!viaje.getConductor().getId().equals(conductor.getId())) {
            throw new RecursoNoEncontradoException("No se encontró el viaje solicitado");
        }
        return viaje;
    }

    private Usuario usuarioConductor() {
        Usuario usuario = usuarioActual.obtener()
                .orElseThrow(() -> new ReglaNegocioException("No hay una sesión activa"));
        if (usuario.getConductor() == null) {
            throw new ReglaNegocioException(
                    "Su cuenta no está vinculada a ningún conductor. Comuníquese con el administrador");
        }
        return usuario;
    }

    private void completarOcupacion(Viaje viaje) {
        viaje.setAsientosLibres((int) asientoRepositorio.countByViajeIdAndEstado(viaje.getId(), EstadoAsiento.LIBRE));
    }
}
