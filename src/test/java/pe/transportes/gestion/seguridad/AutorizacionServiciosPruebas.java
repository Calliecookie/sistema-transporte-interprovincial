package pe.transportes.gestion.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pe.transportes.gestion.modelo.Bus;
import pe.transportes.gestion.modelo.Pasajero;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.repositorio.AsientoRepositorio;
import pe.transportes.gestion.repositorio.BusRepositorio;
import pe.transportes.gestion.repositorio.PasajeroRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.servicio.ServicioBus;
import pe.transportes.gestion.servicio.ServicioPasajero;
import pe.transportes.gestion.servicio.ServicioUsuario;
import pe.transportes.gestion.servicio.ServicioViaje;
import pe.transportes.gestion.util.ValidadorEntidades;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AutorizacionServiciosPruebas.Configuracion.class)
class AutorizacionServiciosPruebas {

    @Autowired
    private ServicioBus servicioBus;

    @Autowired
    private ServicioPasajero servicioPasajero;

    @Autowired
    private ServicioViaje servicioViaje;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {

        @Bean
        BusRepositorio busRepositorio() {
            BusRepositorio repositorio = mock(BusRepositorio.class);
            when(repositorio.findById(1L)).thenReturn(Optional.of(new Bus()));
            return repositorio;
        }

        @Bean
        PasajeroRepositorio pasajeroRepositorio() {
            PasajeroRepositorio repositorio = mock(PasajeroRepositorio.class);
            when(repositorio.findById(1L)).thenReturn(Optional.of(new Pasajero()));
            return repositorio;
        }

        @Bean
        ServicioBus servicioBus(BusRepositorio repositorio) {
            return new ServicioBus(repositorio, mock(ViajeRepositorio.class), mock(ValidadorEntidades.class));
        }

        @Bean
        ServicioPasajero servicioPasajero(PasajeroRepositorio repositorio) {
            return new ServicioPasajero(repositorio, mock(ReservaRepositorio.class), mock(ValidadorEntidades.class));
        }

        @Bean
        ServicioViaje servicioViaje() {
            ViajeRepositorio repositorio = mock(ViajeRepositorio.class);
            when(repositorio.findById(1L)).thenReturn(Optional.of(new Viaje()));
            return new ServicioViaje(repositorio, null, null, null, mock(AsientoRepositorio.class),
                    null, null, mock(ValidadorEntidades.class), Clock.systemUTC());
        }

        @Bean
        ServicioUsuario servicioUsuario() {
            return new ServicioUsuario(null, null, null, null, null, mock(ValidadorEntidades.class));
        }
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void elVendedorNoPuedeUsarLosServiciosDeAdministracion() {
        assertThatThrownBy(() -> servicioBus.obtener(1L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicioBus.crear(new Bus())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicioViaje.programar(new Viaje())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicioViaje.cancelar(1L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicioUsuario.listar()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void elVendedorPuedeGestionarPasajerosPeroNoEliminarlos() {
        assertThat(servicioPasajero.obtener(1L)).isNotNull();
        assertThatThrownBy(() -> servicioPasajero.eliminar(1L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void elVendedorPuedeConsultarLosAsientosDeUnViaje() {
        assertThatCode(() -> servicioViaje.obtener(1L)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void elAdministradorPuedeUsarLosServiciosDeAdministracion() {
        assertThat(servicioBus.obtener(1L)).isNotNull();
        assertThat(servicioPasajero.obtener(1L)).isNotNull();
        assertThatCode(() -> servicioPasajero.eliminar(1L)).doesNotThrowAnyException();
    }

    @Test
    void sinAutenticarseSeRechazaCualquierOperacion() {
        assertThatThrownBy(() -> servicioPasajero.obtener(1L)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> servicioBus.obtener(1L)).isInstanceOf(Exception.class);
    }
}
