package pe.transportes.gestion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.RutaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

class ServicioViajePruebas {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    private ViajeRepositorio viajeRepositorio;
    private RutaRepositorio rutaRepositorio;
    private BusRepositorio busRepositorio;
    private ConductorRepositorio conductorRepositorio;
    private AsientoRepositorio asientoRepositorio;
    private ReservaRepositorio reservaRepositorio;
    private ServicioReserva servicioReserva;
    private ServicioViaje servicio;
    private Clock reloj;
    private Ruta ruta;
    private Bus bus;
    private Conductor conductor;

    @BeforeEach
    void preparar() {
        viajeRepositorio = mock(ViajeRepositorio.class);
        rutaRepositorio = mock(RutaRepositorio.class);
        busRepositorio = mock(BusRepositorio.class);
        conductorRepositorio = mock(ConductorRepositorio.class);
        asientoRepositorio = mock(AsientoRepositorio.class);
        reservaRepositorio = mock(ReservaRepositorio.class);
        servicioReserva = mock(ServicioReserva.class);
        reloj = Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), ZONA);
        servicio = new ServicioViaje(viajeRepositorio, rutaRepositorio, busRepositorio, conductorRepositorio,
                asientoRepositorio, reservaRepositorio, servicioReserva, mock(ValidadorEntidades.class), reloj);

        ruta = new Ruta();
        ruta.setId(1L);
        ruta.setActiva(true);
        ruta.setDuracionHoras(new BigDecimal("8.5"));
        bus = new Bus();
        bus.setId(2L);
        bus.setActivo(true);
        bus.setCapacidad(44);
        conductor = new Conductor();
        conductor.setId(3L);
        conductor.setActivo(true);

        when(rutaRepositorio.findById(1L)).thenReturn(Optional.of(ruta));
        when(busRepositorio.findById(2L)).thenReturn(Optional.of(bus));
        when(conductorRepositorio.findById(3L)).thenReturn(Optional.of(conductor));
        when(viajeRepositorio.save(any(Viaje.class))).thenAnswer(invocacion -> {
            Viaje viaje = invocacion.getArgument(0);
            if (viaje.getId() == null) {
                viaje.setId(50L);
            }
            return viaje;
        });
    }

    private Viaje datos(LocalDateTime salida) {
        Viaje viaje = new Viaje();
        Ruta rutaElegida = new Ruta();
        rutaElegida.setId(1L);
        Bus busElegido = new Bus();
        busElegido.setId(2L);
        Conductor conductorElegido = new Conductor();
        conductorElegido.setId(3L);
        viaje.setRuta(rutaElegida);
        viaje.setBus(busElegido);
        viaje.setConductor(conductorElegido);
        viaje.setFechaSalida(salida);
        viaje.setPrecio(new BigDecimal("70.00"));
        return viaje;
    }

    private LocalDateTime enDosDias() {
        return LocalDateTime.now(reloj).plusDays(2);
    }

    @Test
    void generaTantosAsientosComoCapacidadDelBus() {
        servicio.programar(datos(enDosDias()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Asiento>> captor = ArgumentCaptor.forClass(List.class);
        verify(asientoRepositorio).saveAll(captor.capture());
        List<Asiento> asientos = captor.getValue();
        assertThat(asientos).hasSize(44);
        assertThat(asientos.get(0).getNumero()).isEqualTo(1);
        assertThat(asientos.get(43).getNumero()).isEqualTo(44);
        assertThat(asientos).allMatch(asiento -> asiento.getEstado() == EstadoAsiento.LIBRE);
    }

    @Test
    void calculaLaLlegadaConLaDuracionDeLaRuta() {
        LocalDateTime salida = enDosDias();

        Viaje viaje = servicio.programar(datos(salida));

        assertThat(viaje.getFechaLlegada()).isEqualTo(salida.plusMinutes(510));
        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.PROGRAMADO);
    }

    @Test
    void noProgramaSiElBusYaTieneOtroViajeEnEseHorario() {
        when(viajeRepositorio.contarCrucesDeBus(eq(2L), any(), any(), eq(EstadoViaje.CANCELADO), anyLong()))
                .thenReturn(1L);

        assertThatThrownBy(() -> servicio.programar(datos(enDosDias())))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("bus ya tiene un viaje");
        verify(viajeRepositorio, never()).save(any(Viaje.class));
        verify(asientoRepositorio, never()).saveAll(any());
    }

    @Test
    void noProgramaSiElConductorYaTieneOtroViajeEnEseHorario() {
        when(viajeRepositorio.contarCrucesDeConductor(eq(3L), any(), any(), eq(EstadoViaje.CANCELADO), anyLong()))
                .thenReturn(1L);

        assertThatThrownBy(() -> servicio.programar(datos(enDosDias())))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("conductor ya tiene un viaje");
        verify(viajeRepositorio, never()).save(any(Viaje.class));
    }

    @Test
    void noProgramaConFechaDeSalidaEnElPasado() {
        assertThatThrownBy(() -> servicio.programar(datos(LocalDateTime.now(reloj).minusHours(2))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("posterior a la fecha");
    }

    @Test
    void noProgramaConUnBusInactivo() {
        bus.setActivo(false);

        assertThatThrownBy(() -> servicio.programar(datos(enDosDias())))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void noCambiaElBusDeUnViajeQueYaTieneReservas() {
        Bus otroBus = new Bus();
        otroBus.setId(9L);
        otroBus.setCapacidad(40);
        Viaje existente = new Viaje();
        existente.setId(50L);
        existente.setEstado(EstadoViaje.PROGRAMADO);
        existente.setRuta(ruta);
        existente.setBus(otroBus);
        existente.setConductor(conductor);
        existente.setFechaSalida(enDosDias());
        when(viajeRepositorio.findById(50L)).thenReturn(Optional.of(existente));
        when(reservaRepositorio.existsByViajeId(50L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.actualizar(50L, datos(enDosDias())))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya tiene reservas");
        verify(asientoRepositorio, never()).eliminarPorViaje(anyLong());
    }

    @Test
    void alCancelarUnViajeSeAnulanSusReservas() {
        Viaje existente = new Viaje();
        existente.setId(50L);
        existente.setEstado(EstadoViaje.PROGRAMADO);
        when(viajeRepositorio.findById(50L)).thenReturn(Optional.of(existente));

        Viaje cancelado = servicio.cancelar(50L);

        assertThat(cancelado.getEstado()).isEqualTo(EstadoViaje.CANCELADO);
        verify(servicioReserva).anularActivasDelViaje(50L);
    }

    @Test
    void noCancelaUnViajeQueYaFinalizo() {
        Viaje existente = new Viaje();
        existente.setId(50L);
        existente.setEstado(EstadoViaje.FINALIZADO);
        when(viajeRepositorio.findById(50L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> servicio.cancelar(50L)).isInstanceOf(ReglaNegocioException.class);
        verify(servicioReserva, never()).anularActivasDelViaje(anyLong());
    }
}
