package pe.transportes.gestion.controlador;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.servicio.ServicioPermiso;

@Controller
@RequestMapping("/permisos")
public class ControladorPermiso {

    private static final List<Rol> ROLES_CONFIGURABLES = List.of(Rol.VENDEDOR, Rol.CONDUCTOR);

    private final ServicioPermiso servicio;

    public ControladorPermiso(ServicioPermiso servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String mostrar(Model modelo) {
        modelo.addAttribute("roles", ROLES_CONFIGURABLES);
        modelo.addAttribute("modulos", Modulo.values());
        modelo.addAttribute("vendedor", servicio.asignados(Rol.VENDEDOR));
        modelo.addAttribute("conductor", servicio.asignados(Rol.CONDUCTOR));
        return "permisos/matriz";
    }

    @PostMapping
    public String guardar(@RequestParam(name = "vendedor", required = false) List<Modulo> vendedor,
            @RequestParam(name = "conductor", required = false) List<Modulo> conductor,
            RedirectAttributes atributos) {
        servicio.guardar(Rol.VENDEDOR, vendedor == null ? List.of() : vendedor);
        servicio.guardar(Rol.CONDUCTOR, conductor == null ? List.of() : conductor);
        atributos.addFlashAttribute("mensajeExito", "Los permisos se guardaron correctamente.");
        return "redirect:/permisos";
    }
}
