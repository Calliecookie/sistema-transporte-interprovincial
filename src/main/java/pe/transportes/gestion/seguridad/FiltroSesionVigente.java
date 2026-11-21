package pe.transportes.gestion.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

public class FiltroSesionVigente extends OncePerRequestFilter {

    private final UsuarioRepositorio repositorio;

    public FiltroSesionVigente(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest solicitud) {
        String ruta = solicitud.getRequestURI().substring(solicitud.getContextPath().length());
        return ruta.startsWith("/css/") || ruta.startsWith("/js/") || ruta.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioDetalle detalle
                && !sigueVigente(detalle)) {
            SecurityContextHolder.clearContext();
            HttpSession sesion = solicitud.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
            respuesta.sendRedirect(solicitud.getContextPath() + "/login?vencida");
            return;
        }
        cadena.doFilter(solicitud, respuesta);
    }

    private boolean sigueVigente(UsuarioDetalle detalle) {
        Optional<Usuario> actual = repositorio.findById(detalle.getId());
        return actual.isPresent() && actual.get().isActivo() && actual.get().getRol() == detalle.getRol();
    }
}
