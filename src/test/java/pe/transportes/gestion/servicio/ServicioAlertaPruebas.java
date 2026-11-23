package pe.transportes.gestion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Incidencia;
import pe.transportes.gestion.modelo.TipoIncidencia;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.repositorio.IncidenciaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.seguridad.UsuarioActual;
import pe.transportes.gestion.util.ReglaNegocioException;

class ServicioAlertaPruebas {

    private IncidenciaRepositorio incidenciaRepositorio;
    private ViajeRepositorio viajeRepositorio;
    private UsuarioActual usuarioActual;
    private ServicioAlerta servicio;

    @BeforeEach
    void preparar() {
        incidenciaRepositorio = mock(IncidenciaRepositorio.class);
        viajeRepositorio = mock(ViajeRepositorio.class);
        usuarioActual = mock(UsuarioActual.class);
        Clock reloj = Clock.fixed(Instant.parse("2026-11-20T15:00:00Z"), ZoneId.of("America/Lima"));
        servicio = new ServicioAlerta(incidenciaRepositorio, viajeRepositorio, usuarioActual, reloj);
    }

    private Viaje viaje(Long id, LocalDateTime llegada) {
        Viaje viaje = new Viaje();
        viaje.setId(id);
        viaje.setConductor(new Conductor());
        viaje.setEstado(EstadoViaje.EN_CURSO);
        viaje.setFechaLlegada(llegada);
        return viaje;
    }

    @Test
    void incluyeLosBusesQueNoConfirmaronSuLlegada() {
        Viaje atrasado = viaje(5L, LocalDateTime.of(2026, 11, 20, 7, 30));
        when(viajeRepositorio.findByEstadoInAndFechaLlegadaBeforeOrderByFechaLlegadaAsc(anyCollection(),
                eq(LocalDateTime.of(2026, 11, 20, 9, 0)))).thenReturn(List.of(atrasado));

        List<AlertaViaje> alertas = servicio.pendientes();

        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).titulo()).isEqualTo("Bus sin confirmar llegada");
        assertThat(alertas.get(0).detalle()).contains("2 h 30 min");
        assertThat(alertas.get(0).atendible()).isFalse();
    }

    @Test
    void cuentaIncidenciasYBusesSinLlegada() {
        when(incidenciaRepositorio.countByAtendidaFalseAndTipoIn(anyCollection())).thenReturn(2L);
        when(viajeRepositorio.findByEstadoInAndFechaLlegadaBeforeOrderByFechaLlegadaAsc(anyCollection(), any()))
                .thenReturn(List.of(viaje(1L, LocalDateTime.of(2026, 11, 20, 6, 0))));

        assertThat(servicio.contarPendientes()).isEqualTo(3L);
    }

    @Test
    void atenderRegistraQuienYCuando() {
        Incidencia incidencia = new Incidencia();
        incidencia.setId(4L);
        incidencia.setTipo(TipoIncidencia.ACCIDENTE);
        Usuario admin = new Usuario();
        when(incidenciaRepositorio.findById(4L)).thenReturn(Optional.of(incidencia));
        when(usuarioActual.obtener()).thenReturn(Optional.of(admin));

        servicio.atender(4L);

        assertThat(incidencia.isAtendida()).isTrue();
        assertThat(incidencia.getAtendidaPor()).isSameAs(admin);
        assertThat(incidencia.getFechaAtencion()).isEqualTo(LocalDateTime.of(2026, 11, 20, 10, 0));
        verify(incidenciaRepositorio).save(incidencia);
    }

    @Test
    void noAtiendeDosVecesLaMismaAlerta() {
        Incidencia incidencia = new Incidencia();
        incidencia.setAtendida(true);
        when(incidenciaRepositorio.findById(4L)).thenReturn(Optional.of(incidencia));

        assertThatThrownBy(() -> servicio.atender(4L)).isInstanceOf(ReglaNegocioException.class);
    }
}
