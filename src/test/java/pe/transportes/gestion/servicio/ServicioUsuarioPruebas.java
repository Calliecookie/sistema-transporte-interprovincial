package pe.transportes.gestion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.transportes.gestion.modelo.FormularioUsuario;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.ConductorRepositorio;
import pe.transportes.gestion.repositorio.IncidenciaRepositorio;
import pe.transportes.gestion.repositorio.PasajeRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;
import pe.transportes.gestion.seguridad.UsuarioActual;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

class ServicioUsuarioPruebas {

    private UsuarioRepositorio repositorio;
    private ReservaRepositorio reservaRepositorio;
    private PasajeRepositorio pasajeRepositorio;
    private UsuarioActual usuarioActual;
    private PasswordEncoder codificador;
    private ServicioUsuario servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepositorio.class);
        reservaRepositorio = mock(ReservaRepositorio.class);
        pasajeRepositorio = mock(PasajeRepositorio.class);
        usuarioActual = mock(UsuarioActual.class);
        codificador = new BCryptPasswordEncoder();
        servicio = new ServicioUsuario(repositorio, reservaRepositorio, pasajeRepositorio, codificador,
                usuarioActual, mock(ValidadorEntidades.class), mock(ConductorRepositorio.class),
                mock(IncidenciaRepositorio.class));
        when(repositorio.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private FormularioUsuario formulario(String contrasena, String confirmacion) {
        FormularioUsuario formulario = new FormularioUsuario();
        formulario.setNombreUsuario("maria.ventas");
        formulario.setNombreCompleto("María Ventas");
        formulario.setRol(Rol.VENDEDOR);
        formulario.setContrasena(contrasena);
        formulario.setConfirmacion(confirmacion);
        formulario.setActivo(true);
        return formulario;
    }

    private Usuario usuario(Long id, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreUsuario("admin");
        usuario.setNombreCompleto("Administrador");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setContrasena("hash");
        return usuario;
    }

    @Test
    void creaElUsuarioConLaContrasenaCodificada() {
        Usuario creado = servicio.crear(formulario("Clave1234", "Clave1234"));

        assertThat(creado.getContrasena()).isNotEqualTo("Clave1234");
        assertThat(codificador.matches("Clave1234", creado.getContrasena())).isTrue();
        assertThat(creado.getRol()).isEqualTo(Rol.VENDEDOR);
    }

    @Test
    void exigeContrasenaAlCrear() {
        assertThatThrownBy(() -> servicio.crear(formulario(null, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("contraseña es obligatoria");
    }

    @Test
    void rechazaUnaConfirmacionDistinta() {
        assertThatThrownBy(() -> servicio.crear(formulario("Clave1234", "Otra1234")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("confirmación");
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void rechazaUnNombreDeUsuarioRepetido() {
        when(repositorio.existsByNombreUsuario("maria.ventas")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(formulario("Clave1234", "Clave1234")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void noPermiteQueUnAdministradorSeQuiteSuPropioRol() {
        Usuario propio = usuario(1L, Rol.ADMIN);
        when(repositorio.findById(1L)).thenReturn(Optional.of(propio));
        when(usuarioActual.obtener()).thenReturn(Optional.of(propio));
        FormularioUsuario cambio = formulario(null, null);
        cambio.setRol(Rol.VENDEDOR);

        assertThatThrownBy(() -> servicio.actualizar(1L, cambio))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("propia cuenta");
    }

    @Test
    void noPermiteEliminarLaPropiaCuenta() {
        Usuario propio = usuario(1L, Rol.ADMIN);
        when(repositorio.findById(1L)).thenReturn(Optional.of(propio));
        when(usuarioActual.obtener()).thenReturn(Optional.of(propio));

        assertThatThrownBy(() -> servicio.eliminar(1L)).isInstanceOf(ReglaNegocioException.class);
        verify(repositorio, never()).delete(any(Usuario.class));
    }

    @Test
    void noEliminaUsuariosConVentasRegistradas() {
        Usuario vendedor = usuario(2L, Rol.VENDEDOR);
        when(repositorio.findById(2L)).thenReturn(Optional.of(vendedor));
        when(usuarioActual.obtener()).thenReturn(Optional.of(usuario(1L, Rol.ADMIN)));
        when(reservaRepositorio.existsByUsuarioId(2L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.eliminar(2L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("desactivarlo");
    }

    @Test
    void alEditarConservaLaContrasenaSiNoSeEscribeUnaNueva() {
        Usuario existente = usuario(2L, Rol.VENDEDOR);
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));
        when(usuarioActual.obtener()).thenReturn(Optional.of(usuario(1L, Rol.ADMIN)));

        Usuario actualizado = servicio.actualizar(2L, formulario(null, null));

        assertThat(actualizado.getContrasena()).isEqualTo("hash");
    }
}
