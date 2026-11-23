package pe.transportes.gestion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Incidencia;
import pe.transportes.gestion.modelo.Rol;
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

class ServicioViajeConductorPruebas {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private ViajeRepositorio viajeRepositorio;
    private IncidenciaRepositorio incidenciaRepositorio;
    private UsuarioActual usuarioActual;
    private ServicioViajeConductor servicio;
    private Conductor conductor;
    private Viaje viaje;

    @BeforeEach
    void preparar() {
        viajeRepositorio = mock(ViajeRepositorio.class);
        incidenciaRepositorio = mock(IncidenciaRepositorio.class);
        usuarioActual = mock(UsuarioActual.class);
        Clock reloj = Clock.fixed(Instant.parse("2026-11-20T15:00:00Z"), LIMA);
        servicio = new ServicioViajeConductor(viajeRepositorio, mock(AsientoRepositorio.class),
                mock(ReservaRepositorio.class), incidenciaRepositorio, usuarioActual, reloj);

        conductor = new Conductor();
        conductor.setId(7L);
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setRol(Rol.CONDUCTOR);
        usuario.setConductor(conductor);
        when(usuarioActual.obtener()).thenReturn(Optional.of(usuario));

        viaje = new Viaje();
        viaje.setId(10L);
        viaje.setConductor(conductor);
        viaje.setFechaSalida(LocalDateTime.of(2026, 11, 20, 8, 0));
        viaje.setFechaLlegada(LocalDateTime.of(2026, 11, 20, 16, 0));
        viaje.setEstado(EstadoViaje.EN_CURSO);
        when(viajeRepositorio.findById(10L)).thenReturn(Optional.of(viaje));
    }

    @Test
    void elAccidenteDetieneElViajeYGeneraUnaAlertaPendiente() {
        servicio.reportarAccidente(10L, "Choque leve en el km 120");

        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.INCIDENTE);
        ArgumentCaptor<Incidencia> captor = ArgumentCaptor.forClass(Incidencia.class);
        verify(incidenciaRepositorio).save(captor.capture());
        Incidencia incidencia = captor.getValue();
        assertThat(incidencia.getTipo()).isEqualTo(TipoIncidencia.ACCIDENTE);
        assertThat(incidencia.isAtendida()).isFalse();
        assertThat(incidencia.getDescripcion()).isEqualTo("Choque leve en el km 120");
        assertThat(incidencia.getFechaRegistro()).isEqualTo(LocalDateTime.of(2026, 11, 20, 10, 0));
    }

    @Test
    void elAccidenteExigeUnaDescripcion() {
        assertThatThrownBy(() -> servicio.reportarAccidente(10L, "  "))
                .isInstanceOf(ReglaNegocioException.class);
        verify(incidenciaRepositorio, never()).save(any(Incidencia.class));
    }

    @Test
    void elRetrasoRegistraLosMinutos() {
        servicio.reportarRetraso(10L, 45, "Derrumbe en la vía");

        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.RETRASADO);
        ArgumentCaptor<Incidencia> captor = ArgumentCaptor.forClass(Incidencia.class);
        verify(incidenciaRepositorio).save(captor.capture());
        assertThat(captor.getValue().getMinutosRetraso()).isEqualTo(45);
    }

    @Test
    void rechazaMinutosDeRetrasoInvalidos() {
        assertThatThrownBy(() -> servicio.reportarRetraso(10L, 0, "Tráfico"))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void laPausaYLaReanudacionCambianElEstado() {
        servicio.pausar(10L, "Parada técnica");
        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.EN_PAUSA);

        servicio.reanudar(10L, null);
        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.EN_CURSO);
    }

    @Test
    void noPermiteConfirmarLlegadaConUnIncidenteAbierto() {
        viaje.setEstado(EstadoViaje.INCIDENTE);

        assertThatThrownBy(() -> servicio.finalizar(10L, null)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void laLlegadaFinalizaElViajeSinGenerarAlerta() {
        servicio.finalizar(10L, "Sin novedad");

        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.FINALIZADO);
        ArgumentCaptor<Incidencia> captor = ArgumentCaptor.forClass(Incidencia.class);
        verify(incidenciaRepositorio).save(captor.capture());
        assertThat(captor.getValue().isAtendida()).isTrue();
    }

    @Test
    void noPuedeOperarViajesDeOtroConductor() {
        Conductor otro = new Conductor();
        otro.setId(99L);
        viaje.setConductor(otro);

        assertThatThrownBy(() -> servicio.reportarAccidente(10L, "Prueba"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void soloSeIniciaUnViajeProgramado() {
        assertThatThrownBy(() -> servicio.iniciar(10L, null)).isInstanceOf(ReglaNegocioException.class);

        viaje.setEstado(EstadoViaje.PROGRAMADO);
        servicio.iniciar(10L, null);
        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.EN_CURSO);
    }
}
