package pe.transportes.gestion.seguridad;

import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

@Component
public class UsuarioActual {

    private final UsuarioRepositorio repositorio;

    public UsuarioActual(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Optional<Usuario> obtener() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return repositorio.findByNombreUsuario(autenticacion.getName());
    }
}
