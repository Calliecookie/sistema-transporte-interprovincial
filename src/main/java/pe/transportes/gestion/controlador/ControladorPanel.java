package pe.transportes.gestion.controlador;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.seguridad.ServicioPermisos;
import pe.transportes.gestion.servicio.ServicioAlerta;
import pe.transportes.gestion.servicio.ServicioPanel;
import pe.transportes.gestion.servicio.ServicioViaje;

@Controller
public class ControladorPanel {

    private final ServicioPanel servicioPanel;
    private final ServicioViaje servicioViaje;
    private final ServicioAlerta servicioAlerta;
    private final ServicioPermisos permisos;

    public ControladorPanel(ServicioPanel servicioPanel, ServicioViaje servicioViaje, ServicioAlerta servicioAlerta,
            ServicioPermisos permisos) {
        this.permisos = permisos;
        this.servicioPanel = servicioPanel;
        this.servicioViaje = servicioViaje;
        this.servicioAlerta = servicioAlerta;
    }

    @GetMapping("/")
    public String panel(Authentication autenticacion, Model modelo) {
        if (autenticacion.getAuthorities().stream().anyMatch(rol -> "ROLE_CONDUCTOR".equals(rol.getAuthority()))) {
            return "redirect:/mis-viajes";
        }
        boolean verAlertas = permisos.permite(autenticacion, Modulo.ALERTAS);
        modelo.addAttribute("alertas", verAlertas ? servicioAlerta.pendientes() : List.of());
        modelo.addAttribute("resumen", servicioPanel.resumen());
        modelo.addAttribute("proximosViajes", servicioViaje.proximosProgramados());
        return "panel";
    }
}
