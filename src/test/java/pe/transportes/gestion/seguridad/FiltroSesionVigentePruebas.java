package pe.transportes.gestion.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

class FiltroSesionVigentePruebas {

    private UsuarioRepositorio repositorio;
    private FiltroSesionVigente filtro;
    private FilterChain cadena;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepositorio.class);
        cadena = mock(FilterChain.class);
        filtro = new FiltroSesionVigente(repositorio);
        usuario = new Usuario();
        usuario.setId(4L);
        usuario.setNombreUsuario("vendedor");
        usuario.setNombreCompleto("Vendedor de ventanilla");
        usuario.setContrasena("hash");
        usuario.setRol(Rol.VENDEDOR);
        usuario.setActivo(true);
        UsuarioDetalle detalle = new UsuarioDetalle(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(detalle, null, detalle.getAuthorities()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse ejecutar(String ruta) throws Exception {
        MockHttpServletRequest solicitud = new MockHttpServletRequest("GET", ruta);
        solicitud.getSession(true);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        filtro.doFilter(solicitud, respuesta, cadena);
        return respuesta;
    }

    @Test
    void dejaPasarAlUsuarioActivoConElMismoRol() throws Exception {
        when(repositorio.findById(4L)).thenReturn(Optional.of(usuario));

        MockHttpServletResponse respuesta = ejecutar("/pasajeros");

        verify(cadena).doFilter(any(), any());
        assertThat(respuesta.getRedirectedUrl()).isNull();
    }

    @Test
    void cierraLaSesionSiLaCuentaFueDesactivada() throws Exception {
        usuario.setActivo(false);
        when(repositorio.findById(4L)).thenReturn(Optional.of(usuario));

        MockHttpServletResponse respuesta = ejecutar("/pasajeros");

        verify(cadena, never()).doFilter(any(), any());
        assertThat(respuesta.getRedirectedUrl()).isEqualTo("/login?vencida");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void cierraLaSesionSiCambioElRolDelUsuario() throws Exception {
        usuario.setRol(Rol.ADMIN);
        when(repositorio.findById(4L)).thenReturn(Optional.of(usuario));

        MockHttpServletResponse respuesta = ejecutar("/pasajeros");

        verify(cadena, never()).doFilter(any(), any());
        assertThat(respuesta.getRedirectedUrl()).isEqualTo("/login?vencida");
    }

    @Test
    void cierraLaSesionSiElUsuarioYaNoExiste() throws Exception {
        when(repositorio.findById(4L)).thenReturn(Optional.empty());

        MockHttpServletResponse respuesta = ejecutar("/reservas");

        assertThat(respuesta.getRedirectedUrl()).isEqualTo("/login?vencida");
    }

    @Test
    void noConsultaLaBaseParaArchivosEstaticos() throws Exception {
        ejecutar("/css/estilos.css");

        verify(cadena).doFilter(any(), any());
        verify(repositorio, never()).findById(any());
    }
}
