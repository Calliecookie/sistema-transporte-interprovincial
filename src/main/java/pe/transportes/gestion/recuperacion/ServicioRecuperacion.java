package pe.transportes.gestion.recuperacion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.TokenRecuperacion;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.TokenRecuperacionRepositorio;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;
import pe.transportes.gestion.util.ReglaNegocioException;

@Service
public class ServicioRecuperacion {

    private static final int MAXIMO_SOLICITUDES_POR_HORA = 3;

    private final UsuarioRepositorio usuarioRepositorio;
    private final TokenRecuperacionRepositorio tokenRepositorio;
    private final PasswordEncoder codificador;
    private final EnvioCorreo envioCorreo;
    private final PropiedadesAplicacion propiedades;
    private final Clock reloj;
    private final SecureRandom azar = new SecureRandom();

    public ServicioRecuperacion(UsuarioRepositorio usuarioRepositorio, TokenRecuperacionRepositorio tokenRepositorio,
            PasswordEncoder codificador, EnvioCorreo envioCorreo, PropiedadesAplicacion propiedades, Clock reloj) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tokenRepositorio = tokenRepositorio;
        this.codificador = codificador;
        this.envioCorreo = envioCorreo;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    @Transactional
    public void solicitar(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return;
        }
        String dato = identificador.strip();
        Optional<Usuario> encontrado = usuarioRepositorio.findByNombreUsuario(dato)
                .or(() -> usuarioRepositorio.findByCorreoIgnoreCase(dato));
        if (encontrado.isEmpty()) {
            return;
        }
        Usuario usuario = encontrado.get();
        if (!usuario.isActivo() || usuario.getCorreo() == null) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (tokenRepositorio.countByUsuarioIdAndFechaCreacionAfter(usuario.getId(),
                ahora.minusHours(1)) >= MAXIMO_SOLICITUDES_POR_HORA) {
            return;
        }
        for (TokenRecuperacion previo : tokenRepositorio.findByUsuarioIdAndUsadoFalse(usuario.getId())) {
            previo.setUsado(true);
            tokenRepositorio.save(previo);
        }
        byte[] bytes = new byte[32];
        azar.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TokenRecuperacion registro = new TokenRecuperacion();
        registro.setUsuario(usuario);
        registro.setHuella(huella(token));
        registro.setFechaCreacion(ahora);
        registro.setFechaExpiracion(ahora.plusMinutes(propiedades.minutosVigenciaEnlace()));
        tokenRepositorio.save(registro);
        String enlace = propiedades.urlPublica() + "/restablecer?token=" + token;
        envioCorreo.enviar(usuario.getCorreo(), "Recuperación de contraseña - Transportes Andinos",
                "Hola " + usuario.getNombreCompleto() + ",\n\nRecibimos una solicitud para restablecer su contraseña. "
                        + "Ingrese al siguiente enlace, vigente por " + propiedades.minutosVigenciaEnlace()
                        + " minutos:\n\n" + enlace
                        + "\n\nSi usted no lo solicitó, ignore este mensaje y su contraseña seguirá igual.");
    }

    @Transactional(readOnly = true)
    public boolean enlaceVigente(String token) {
        return buscarVigente(token).isPresent();
    }

    @Transactional
    public void restablecer(String token, String contrasena, String confirmacion) {
        if (contrasena == null || contrasena.length() < 8 || contrasena.length() > 72) {
            throw new ReglaNegocioException("La contraseña debe tener entre 8 y 72 caracteres");
        }
        if (!contrasena.equals(confirmacion)) {
            throw new ReglaNegocioException("La confirmación no coincide con la contraseña");
        }
        TokenRecuperacion registro = buscarVigente(token)
                .orElseThrow(() -> new ReglaNegocioException("El enlace no es válido o ya venció. Solicite uno nuevo"));
        Usuario usuario = registro.getUsuario();
        usuario.setContrasena(codificador.encode(contrasena));
        usuarioRepositorio.save(usuario);
        registro.setUsado(true);
        tokenRepositorio.save(registro);
    }

    private Optional<TokenRecuperacion> buscarVigente(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return tokenRepositorio.findByHuella(huella(token.strip()))
                .filter(registro -> !registro.isUsado())
                .filter(registro -> registro.getFechaExpiracion().isAfter(LocalDateTime.now(reloj)))
                .filter(registro -> registro.getUsuario().isActivo());
    }

    static String huella(String token) {
        try {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(resumen.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException(excepcion);
        }
    }
}
