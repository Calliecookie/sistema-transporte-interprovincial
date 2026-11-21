package pe.transportes.gestion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import pe.transportes.gestion.repositorio.PasajeRepositorio;
import pe.transportes.gestion.repositorio.PasajeroRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.seguridad.UsuarioActual;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

class ServicioReservaPruebas {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    private ReservaRepositorio reservaRepositorio;
    private AsientoRepositorio asientoRepositorio;
    private PasajeroRepositorio pasajeroRepositorio;
    private ViajeRepositorio viajeRepositorio;
    private PasajeRepositorio pasajeRepositorio;
    private ServicioReserva servicio;
    private Clock reloj;
    private Viaje viaje;
    private Pasajero pasajero;

    @BeforeEach
    void preparar() {
        reservaRepositorio = mock(ReservaRepositorio.class);
        asientoRepositorio = mock(AsientoRepositorio.class);
        pasajeroRepositorio = mock(PasajeroRepositorio.class);
        viajeRepositorio = mock(ViajeRepositorio.class);
        pasajeRepositorio = mock(PasajeRepositorio.class);
        reloj = Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), ZONA);
        servicio = new ServicioReserva(reservaRepositorio, asientoRepositorio, pasajeroRepositorio,
                viajeRepositorio, pasajeRepositorio, mock(ValidadorEntidades.class), mock(UsuarioActual.class), reloj);

        viaje = new Viaje();
        viaje.setId(1L);
        viaje.setEstado(EstadoViaje.PROGRAMADO);
        viaje.setFechaSalida(LocalDateTime.now(reloj).plusDays(2));
        viaje.setPrecio(new BigDecimal("110.00"));
        pasajero = new Pasajero();
        pasajero.setId(5L);
        pasajero.setDni("45218790");

        when(viajeRepositorio.findById(1L)).thenReturn(Optional.of(viaje));
        when(pasajeroRepositorio.findByDni("45218790")).thenReturn(Optional.of(pasajero));
        when(reservaRepositorio.save(any(Reserva.class))).thenAnswer(invocacion -> {
            Reserva reserva = invocacion.getArgument(0);
            if (reserva.getId() == null) {
                reserva.setId(30L);
            }
            return reserva;
        });
        when(pasajeRepositorio.save(any(Pasaje.class))).thenAnswer(invocacion -> {
            Pasaje pasaje = invocacion.getArgument(0);
            if (pasaje.getId() == null) {
                pasaje.setId(8L);
            }
            return pasaje;
        });
        when(asientoRepositorio.save(any(Asiento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private Asiento asiento(Long id, int numero, EstadoAsiento estado) {
        Asiento asiento = new Asiento();
        asiento.setId(id);
        asiento.setViaje(viaje);
        asiento.setNumero(numero);
        asiento.setEstado(estado);
        when(asientoRepositorio.buscarParaActualizar(id)).thenReturn(Optional.of(asiento));
        return asiento;
    }

    private FormularioReserva formulario(Long asientoId, AccionReserva accion, MetodoPago metodoPago) {
        FormularioReserva formulario = new FormularioReserva();
        formulario.setViajeId(1L);
        formulario.setAsientoId(asientoId);
        formulario.setDni("45218790");
        formulario.setAccion(accion);
        formulario.setMetodoPago(metodoPago);
        return formulario;
    }

    @Test
    void noVendeUnAsientoQueYaEstaVendido() {
        asiento(10L, 5, EstadoAsiento.VENDIDO);

        assertThatThrownBy(() -> servicio.registrar(formulario(10L, AccionReserva.VENDER, MetodoPago.EFECTIVO)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya está ocupado");
        verify(reservaRepositorio, never()).save(any(Reserva.class));
        verify(pasajeRepositorio, never()).save(any(Pasaje.class));
    }

    @Test
    void noReservaUnAsientoQueYaEstaReservado() {
        asiento(10L, 5, EstadoAsiento.RESERVADO);

        assertThatThrownBy(() -> servicio.registrar(formulario(10L, AccionReserva.RESERVAR, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya está ocupado");
    }

    @Test
    void reservarDejaElAsientoReservadoYNoEmitePasaje() {
        Asiento asiento = asiento(10L, 5, EstadoAsiento.LIBRE);

        Reserva reserva = servicio.registrar(formulario(10L, AccionReserva.RESERVAR, null));

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.RESERVADA);
        assertThat(asiento.getEstado()).isEqualTo(EstadoAsiento.RESERVADO);
        verify(pasajeRepositorio, never()).save(any(Pasaje.class));
    }

    @Test
    void venderDejaElAsientoVendidoYEmiteElPasajeConSuCodigo() {
        Asiento asiento = asiento(10L, 5, EstadoAsiento.LIBRE);

        Reserva reserva = servicio.registrar(formulario(10L, AccionReserva.VENDER, MetodoPago.TARJETA));

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.VENDIDA);
        assertThat(asiento.getEstado()).isEqualTo(EstadoAsiento.VENDIDO);
        ArgumentCaptor<Pasaje> captor = ArgumentCaptor.forClass(Pasaje.class);
        verify(pasajeRepositorio, atLeastOnce()).save(captor.capture());
        Pasaje emitido = captor.getValue();
        assertThat(emitido.getCodigo()).isEqualTo("PJ-00000008");
        assertThat(emitido.getEstado()).isEqualTo(EstadoPasaje.EMITIDO);
        assertThat(emitido.getMetodoPago()).isEqualTo(MetodoPago.TARJETA);
        assertThat(emitido.getPrecio()).isEqualByComparingTo("110.00");
    }

    @Test
    void exigeElMetodoDePagoCuandoSeVende() {
        asiento(10L, 5, EstadoAsiento.LIBRE);

        assertThatThrownBy(() -> servicio.registrar(formulario(10L, AccionReserva.VENDER, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("método de pago");
    }

    @Test
    void noPermiteQueUnPasajeroTengaDosAsientosEnElMismoViaje() {
        asiento(10L, 5, EstadoAsiento.LIBRE);
        when(reservaRepositorio.existsByViajeIdAndPasajeroIdAndEstadoNot(1L, 5L, EstadoReserva.ANULADA))
                .thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(formulario(10L, AccionReserva.RESERVAR, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya tiene un asiento");
    }

    @Test
    void noVendeSiElViajeYaSalio() {
        viaje.setFechaSalida(LocalDateTime.now(reloj).minusHours(1));
        asiento(10L, 5, EstadoAsiento.LIBRE);

        assertThatThrownBy(() -> servicio.registrar(formulario(10L, AccionReserva.RESERVAR, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya no admite");
    }

    @Test
    void anularUnaReservaVendidaLiberaElAsientoYAnulaElPasaje() {
        Asiento asiento = asiento(10L, 5, EstadoAsiento.VENDIDO);
        Reserva reserva = new Reserva();
        reserva.setId(30L);
        reserva.setViaje(viaje);
        reserva.setAsiento(asiento);
        reserva.setEstado(EstadoReserva.VENDIDA);
        Pasaje pasaje = new Pasaje();
        pasaje.setEstado(EstadoPasaje.EMITIDO);
        when(reservaRepositorio.findById(30L)).thenReturn(Optional.of(reserva));
        when(pasajeRepositorio.findByReservaId(30L)).thenReturn(Optional.of(pasaje));

        servicio.anular(30L);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.ANULADA);
        assertThat(asiento.getEstado()).isEqualTo(EstadoAsiento.LIBRE);
        assertThat(pasaje.getEstado()).isEqualTo(EstadoPasaje.ANULADO);
    }

    @Test
    void noAnulaUnaReservaQueYaEstaAnulada() {
        Reserva reserva = new Reserva();
        reserva.setId(30L);
        reserva.setViaje(viaje);
        reserva.setEstado(EstadoReserva.ANULADA);
        when(reservaRepositorio.findById(30L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> servicio.anular(30L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya fue anulada");
    }

    @Test
    void noAnulaReservasDeUnViajeQueYaEstaEnCurso() {
        viaje.setEstado(EstadoViaje.EN_CURSO);
        Reserva reserva = new Reserva();
        reserva.setId(30L);
        reserva.setViaje(viaje);
        reserva.setEstado(EstadoReserva.VENDIDA);
        when(reservaRepositorio.findById(30L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> servicio.anular(30L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("viajes programados");
    }
}
