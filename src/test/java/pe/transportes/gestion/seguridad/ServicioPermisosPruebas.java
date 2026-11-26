package pe.transportes.gestion.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.modelo.PermisoRol;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.repositorio.PermisoRolRepositorio;

class ServicioPermisosPruebas {

    private PermisoRolRepositorio repositorio;
    private ServicioPermisos servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(PermisoRolRepositorio.class);
        servicio = new ServicioPermisos(repositorio);
    }

    private UsernamePasswordAuthenticationToken sesion(String rol) {
        return new UsernamePasswordAuthenticationToken("u", "x", List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    @Test
    void elAdministradorSiempreTieneAcceso() {
        assertThat(servicio.permite(sesion("ADMIN"), Modulo.REPORTES)).isTrue();
        assertThat(servicio.modulosDe(sesion("ADMIN"))).containsExactlyInAnyOrder(Modulo.values());
    }

    @Test
    void elVendedorSoloAccedeALosModulosAsignados() {
        when(repositorio.existsByRolAndModulo(Rol.VENDEDOR, Modulo.REPORTES)).thenReturn(true);
        when(repositorio.existsByRolAndModulo(Rol.VENDEDOR, Modulo.ALERTAS)).thenReturn(false);

        assertThat(servicio.permite(sesion("VENDEDOR"), Modulo.REPORTES)).isTrue();
        assertThat(servicio.permite(sesion("VENDEDOR"), Modulo.ALERTAS)).isFalse();
    }

    @Test
    void unRolNoPuedeUsarModulosDeOtroRolAunqueEstenAsignados() {
        when(repositorio.existsByRolAndModulo(Rol.VENDEDOR, Modulo.MIS_VIAJES)).thenReturn(true);
        when(repositorio.findByRol(Rol.VENDEDOR)).thenReturn(List.of(new PermisoRol(Rol.VENDEDOR, Modulo.MIS_VIAJES),
                new PermisoRol(Rol.VENDEDOR, Modulo.PASAJEROS)));

        assertThat(servicio.permite(sesion("VENDEDOR"), Modulo.MIS_VIAJES)).isFalse();
        assertThat(servicio.modulosDe(sesion("VENDEDOR"))).containsExactly(Modulo.PASAJEROS);
    }
}
