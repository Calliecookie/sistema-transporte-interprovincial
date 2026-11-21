package pe.transportes.gestion.seguridad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

@Component
public class InicializadorUsuarios implements ApplicationRunner {

    private static final Logger registro = LoggerFactory.getLogger(InicializadorUsuarios.class);

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder codificador;

    public InicializadorUsuarios(UsuarioRepositorio repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (repositorio.count() > 0) {
            return;
        }
        repositorio.save(crear("admin", "Administrador del sistema", "Admin123*", Rol.ADMIN));
        repositorio.save(crear("vendedor", "Vendedor de ventanilla", "Vendedor123*", Rol.VENDEDOR));
        registro.info("Se crearon los usuarios iniciales admin y vendedor");
    }

    private Usuario crear(String nombreUsuario, String nombreCompleto, String contrasena, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setContrasena(codificador.encode(contrasena));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }
}
