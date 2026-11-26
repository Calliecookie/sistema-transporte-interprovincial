package pe.transportes.gestion.controlador;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.seguridad.ServicioPermisos;
import pe.transportes.gestion.servicio.ServicioAlerta;

@ControllerAdvice
public class AtributosGlobales {

    private final Clock reloj;
    private final ServicioAlerta servicioAlerta;
    private final ServicioPermisos permisos;

    public AtributosGlobales(Clock reloj, ServicioAlerta servicioAlerta, ServicioPermisos permisos) {
        this.reloj = reloj;
        this.servicioAlerta = servicioAlerta;
        this.permisos = permisos;
    }

    @ModelAttribute("modulosPermitidos")
    public Set<String> modulosPermitidos() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        Set<String> nombres = new HashSet<>();
        for (Modulo modulo : permisos.modulosDe(autenticacion)) {
            nombres.add(modulo.name());
        }
        return nombres;
    }

    @ModelAttribute("alertasPendientes")
    public Long alertasPendientes() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            return null;
        }
        boolean puedeVer = permisos.permite(autenticacion, Modulo.ALERTAS);
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
