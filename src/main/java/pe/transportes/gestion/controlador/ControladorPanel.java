package pe.transportes.gestion.controlador;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.transportes.gestion.servicio.ServicioAlerta;
import pe.transportes.gestion.servicio.ServicioPanel;
import pe.transportes.gestion.servicio.ServicioViaje;

@Controller
public class ControladorPanel {

    private final ServicioPanel servicioPanel;
    private final ServicioViaje servicioViaje;
    private final ServicioAlerta servicioAlerta;

    public ControladorPanel(ServicioPanel servicioPanel, ServicioViaje servicioViaje, ServicioAlerta servicioAlerta) {
        this.servicioPanel = servicioPanel;
        this.servicioViaje = servicioViaje;
        this.servicioAlerta = servicioAlerta;
    }

    @GetMapping("/")
    public String panel(Authentication autenticacion, Model modelo) {
        if (autenticacion.getAuthorities().stream().anyMatch(rol -> "ROLE_CONDUCTOR".equals(rol.getAuthority()))) {
            return "redirect:/mis-viajes";
        }
        modelo.addAttribute("alertas", servicioAlerta.pendientes());
        modelo.addAttribute("resumen", servicioPanel.resumen());
        modelo.addAttribute("proximosViajes", servicioViaje.proximosProgramados());
        return "panel";
    }
}
