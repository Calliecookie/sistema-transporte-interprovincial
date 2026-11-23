package pe.transportes.gestion.controlador;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.transportes.gestion.servicio.ServicioAlerta;

@ControllerAdvice
public class AtributosGlobales {

    private final Clock reloj;
    private final ServicioAlerta servicioAlerta;

    public AtributosGlobales(Clock reloj, ServicioAlerta servicioAlerta) {
        this.reloj = reloj;
        this.servicioAlerta = servicioAlerta;
    }

    @ModelAttribute("alertasPendientes")
    public Long alertasPendientes() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            return null;
        }
        boolean puedeVer = autenticacion.getAuthorities().stream().anyMatch(
                rol -> "ROLE_ADMIN".equals(rol.getAuthority()) || "ROLE_VENDEDOR".equals(rol.getAuthority()));
        return puedeVer ? servicioAlerta.contarPendientes() : null;
    }

    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("hoy")
    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }
}
