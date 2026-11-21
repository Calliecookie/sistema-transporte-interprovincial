package pe.transportes.gestion.controlador;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.transportes.gestion.modelo.FormularioUsuario;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.servicio.ServicioUsuario;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/usuarios")
public class ControladorUsuario {

    private static final String FORMULARIO = "usuarios/formulario";

    private final ServicioUsuario servicio;

    public ControladorUsuario(ServicioUsuario servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("usuarios", servicio.listar());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("formulario", new FormularioUsuario());
        modelo.addAttribute("roles", Rol.values());
        modelo.addAttribute("usuarioId", null);
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("formulario") FormularioUsuario formulario, BindingResult resultado,
            Model modelo, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(formulario);
                atributos.addFlashAttribute("mensajeExito", "El usuario se registró correctamente.");
                return "redirect:/usuarios";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        modelo.addAttribute("roles", Rol.values());
        modelo.addAttribute("usuarioId", null);
        return FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("formulario", servicio.formulario(id));
        modelo.addAttribute("roles", Rol.values());
        modelo.addAttribute("usuarioId", id);
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("formulario") FormularioUsuario formulario,
            BindingResult resultado, Model modelo, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, formulario);
                atributos.addFlashAttribute("mensajeExito", "Los datos del usuario se actualizaron correctamente.");
                return "redirect:/usuarios";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        modelo.addAttribute("roles", Rol.values());
        modelo.addAttribute("usuarioId", id);
        return FORMULARIO;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.eliminar(id);
            atributos.addFlashAttribute("mensajeExito", "El usuario se eliminó correctamente.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/usuarios";
    }
}
