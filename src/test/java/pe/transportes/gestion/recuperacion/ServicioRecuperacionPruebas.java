package pe.transportes.gestion.recuperacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.transportes.gestion.modelo.TokenRecuperacion;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.TokenRecuperacionRepositorio;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;
import pe.transportes.gestion.util.ReglaNegocioException;

class ServicioRecuperacionPruebas {

    private UsuarioRepositorio usuarios;
    private TokenRecuperacionRepositorio tokens;
    private EnvioCorreo correo;
    private BCryptPasswordEncoder codificador;
    private ServicioRecuperacion servicio;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepositorio.class);
        tokens = mock(TokenRecuperacionRepositorio.class);
        correo = mock(EnvioCorreo.class);
        codificador = new BCryptPasswordEncoder();
        Clock reloj = Clock.fixed(Instant.parse("2026-11-20T15:00:00Z"), ZoneId.of("America/Lima"));
        servicio = new ServicioRecuperacion(usuarios, tokens, codificador, correo,
                new PropiedadesAplicacion("http://localhost:8081/", null, null), reloj);
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreUsuario("admin");
        usuario.setNombreCompleto("Administrador");
        usuario.setCorreo("admin@correo.pe");
        usuario.setActivo(true);
        usuario.setContrasena("anterior");
        when(usuarios.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        when(usuarios.findByNombreUsuario("admin@correo.pe")).thenReturn(Optional.empty());
        when(usuarios.findByCorreoIgnoreCase("admin@correo.pe")).thenReturn(Optional.of(usuario));
        when(tokens.save(any(TokenRecuperacion.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void enviaUnEnlaceYGuardaSoloLaHuellaDelToken() {
        servicio.solicitar("admin@correo.pe");

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(correo).enviar(any(), anyString(), texto.capture());
        String token = texto.getValue().replaceAll("(?s).*token=(\\S+).*", "$1");
        ArgumentCaptor<TokenRecuperacion> guardado = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokens).save(guardado.capture());
        assertThat(guardado.getValue().getHuella()).isEqualTo(ServicioRecuperacion.huella(token))
                .isNotEqualTo(token);
        assertThat(guardado.getValue().getFechaExpiracion()).isEqualTo(LocalDateTime.of(2026, 11, 20, 10, 30));
    }

    @Test
    void noRevelaSiLaCuentaExiste() {
        when(usuarios.findByNombreUsuario("nadie")).thenReturn(Optional.empty());
        when(usuarios.findByCorreoIgnoreCase("nadie")).thenReturn(Optional.empty());

        servicio.solicitar("nadie");

        verify(correo, never()).enviar(any(), any(), any());
    }

    @Test
    void noEnviaNadaSiElUsuarioNoTieneCorreoOEstaInactivo() {
        usuario.setCorreo(null);
        servicio.solicitar("admin");
        usuario.setCorreo("admin@correo.pe");
        usuario.setActivo(false);
        servicio.solicitar("admin");

        verify(correo, never()).enviar(any(), any(), any());
    }

    @Test
    void limitaLasSolicitudesPorHora() {
        when(tokens.countByUsuarioIdAndFechaCreacionAfter(any(), any())).thenReturn(3L);

        servicio.solicitar("admin");

        verify(correo, never()).enviar(any(), any(), any());
    }

    private TokenRecuperacion token(String valor, boolean usado, LocalDateTime expira) {
        TokenRecuperacion registro = new TokenRecuperacion();
        registro.setUsuario(usuario);
        registro.setHuella(ServicioRecuperacion.huella(valor));
        registro.setUsado(usado);
        registro.setFechaExpiracion(expira);
        when(tokens.findByHuella(registro.getHuella())).thenReturn(Optional.of(registro));
        return registro;
    }

    @Test
    void restableceLaContrasenaYInvalidaElEnlace() {
        TokenRecuperacion registro = token("abc", false, LocalDateTime.of(2026, 11, 20, 10, 20));

        servicio.restablecer("abc", "NuevaClave123", "NuevaClave123");

        assertThat(codificador.matches("NuevaClave123", usuario.getContrasena())).isTrue();
        assertThat(registro.isUsado()).isTrue();
    }

    @Test
    void rechazaEnlacesUsadosOVencidos() {
        token("usado", true, LocalDateTime.of(2026, 11, 20, 10, 20));
        token("vencido", false, LocalDateTime.of(2026, 11, 20, 9, 0));

        assertThatThrownBy(() -> servicio.restablecer("usado", "NuevaClave123", "NuevaClave123"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> servicio.restablecer("vencido", "NuevaClave123", "NuevaClave123"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(usuario.getContrasena()).isEqualTo("anterior");
    }

    @Test
    void validaLaContrasenaNueva() {
        token("abc", false, LocalDateTime.of(2026, 11, 20, 10, 20));

        assertThatThrownBy(() -> servicio.restablecer("abc", "corta", "corta"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> servicio.restablecer("abc", "NuevaClave123", "Otra123456"))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
