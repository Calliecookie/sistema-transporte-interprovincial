package pe.transportes.gestion.controlador;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.transportes.gestion.servicio.ServicioAlerta;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/alertas")
public class ControladorAlerta {

    private final ServicioAlerta servicio;

    public ControladorAlerta(ServicioAlerta servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("alertas", servicio.pendientes());
        modelo.addAttribute("atendidas", servicio.atendidasRecientes());
        modelo.addAttribute("tolerancia", ServicioAlerta.MINUTOS_TOLERANCIA_LLEGADA);
        return "alertas/lista";
    }

    @GetMapping("/conteo")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> conteo() {
        return ResponseEntity.ok(Map.of("pendientes", servicio.contarPendientes()));
    }

    @PostMapping("/{id}/atender")
    public String atender(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.atender(id);
            atributos.addFlashAttribute("mensajeExito", "La alerta se marcó como atendida.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/alertas";
    }
}
