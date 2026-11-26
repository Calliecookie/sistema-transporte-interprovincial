package pe.transportes.gestion.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.transportes.gestion.recuperacion.ServicioRecuperacion;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
public class ControladorRecuperacion {

    private final ServicioRecuperacion servicio;

    public ControladorRecuperacion(ServicioRecuperacion servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/recuperar")
    public String formulario() {
        return "recuperacion/solicitar";
    }

    @PostMapping("/recuperar")
    public String solicitar(@RequestParam(required = false) String identificador, Model modelo) {
        servicio.solicitar(identificador);
        modelo.addAttribute("enviado", true);
        return "recuperacion/solicitar";
    }

    @GetMapping("/restablecer")
    public String restablecer(@RequestParam(required = false) String token, Model modelo) {
        modelo.addAttribute("token", token);
        modelo.addAttribute("vigente", servicio.enlaceVigente(token));
        return "recuperacion/restablecer";
    }

    @PostMapping("/restablecer")
    public String guardar(@RequestParam String token, @RequestParam(required = false) String contrasena,
            @RequestParam(required = false) String confirmacion, Model modelo) {
        try {
            servicio.restablecer(token, contrasena, confirmacion);
            return "redirect:/login?restablecida";
        } catch (ReglaNegocioException excepcion) {
            modelo.addAttribute("token", token);
            modelo.addAttribute("vigente", servicio.enlaceVigente(token));
            modelo.addAttribute("mensajeError", excepcion.getMessage());
            return "recuperacion/restablecer";
        }
    }
}
