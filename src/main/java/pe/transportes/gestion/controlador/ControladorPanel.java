package pe.transportes.gestion.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.transportes.gestion.servicio.ServicioPanel;
import pe.transportes.gestion.servicio.ServicioViaje;

@Controller
public class ControladorPanel {

    private final ServicioPanel servicioPanel;
    private final ServicioViaje servicioViaje;

    public ControladorPanel(ServicioPanel servicioPanel, ServicioViaje servicioViaje) {
        this.servicioPanel = servicioPanel;
        this.servicioViaje = servicioViaje;
    }

    @GetMapping("/")
    public String panel(Model modelo) {
        modelo.addAttribute("resumen", servicioPanel.resumen());
        modelo.addAttribute("proximosViajes", servicioViaje.proximosProgramados());
        return "panel";
    }
}
