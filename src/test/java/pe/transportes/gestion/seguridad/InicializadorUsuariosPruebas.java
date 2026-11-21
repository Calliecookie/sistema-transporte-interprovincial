package pe.transportes.gestion.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

class InicializadorUsuariosPruebas {

    private UsuarioRepositorio repositorio;
    private PasswordEncoder codificador;
    private InicializadorUsuarios inicializador;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepositorio.class);
        codificador = new BCryptPasswordEncoder();
        inicializador = new InicializadorUsuarios(repositorio, codificador);
    }

    @Test
    void creaAdministradorYVendedorCuandoLaTablaEstaVacia() {
        when(repositorio.count()).thenReturn(0L);

        inicializador.run(new DefaultApplicationArguments());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio, times(2)).save(captor.capture());
        List<Usuario> creados = captor.getAllValues();
        assertThat(creados).extracting(Usuario::getNombreUsuario).containsExactly("admin", "vendedor");
        assertThat(creados).extracting(Usuario::getRol).containsExactly(Rol.ADMIN, Rol.VENDEDOR);
        assertThat(creados).allMatch(Usuario::isActivo);
    }

    @Test
    void guardaLasContrasenasCodificadasConBCrypt() {
        when(repositorio.count()).thenReturn(0L);

        inicializador.run(new DefaultApplicationArguments());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio, times(2)).save(captor.capture());
        Usuario admin = captor.getAllValues().get(0);
        assertThat(admin.getContrasena()).startsWith("$2").isNotEqualTo("Admin123*");
        assertThat(codificador.matches("Admin123*", admin.getContrasena())).isTrue();
        Usuario vendedor = captor.getAllValues().get(1);
        assertThat(codificador.matches("Vendedor123*", vendedor.getContrasena())).isTrue();
    }

    @Test
    void noCreaNadaSiYaExistenUsuarios() {
        when(repositorio.count()).thenReturn(3L);

        inicializador.run(new DefaultApplicationArguments());

        verify(repositorio, never()).save(any(Usuario.class));
    }
}
