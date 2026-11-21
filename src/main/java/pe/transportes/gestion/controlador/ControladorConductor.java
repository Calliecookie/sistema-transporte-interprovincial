package pe.transportes.gestion.controlador;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.servicio.ServicioConductor;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/conductores")
public class ControladorConductor {

    private static final String FORMULARIO = "conductores/formulario";

    private final ServicioConductor servicio;

    public ControladorConductor(ServicioConductor servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo, Model modelo) {
        modelo.addAttribute("pagina",
                servicio.buscar(texto, activo, Paginacion.de(pagina, Sort.by("apellidos", "nombres"))));
        modelo.addAttribute("texto", texto);
        modelo.addAttribute("activo", activo);
        modelo.addAttribute("parametros", ParametrosUrl.construir("texto", texto, "activo", activo));
        return "conductores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("conductor", new Conductor());
        modelo.addAttribute("categorias", Conductor.CATEGORIAS);
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("conductor") Conductor conductor, BindingResult resultado,
            Model modelo, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(conductor);
                atributos.addFlashAttribute("mensajeExito", "El conductor se registró correctamente.");
                return "redirect:/conductores";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        modelo.addAttribute("categorias", Conductor.CATEGORIAS);
        return FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("conductor", servicio.obtener(id));
        modelo.addAttribute("categorias", Conductor.CATEGORIAS);
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("conductor") Conductor conductor,
            BindingResult resultado, Model modelo, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, conductor);
                atributos.addFlashAttribute("mensajeExito", "Los datos del conductor se actualizaron correctamente.");
                return "redirect:/conductores";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        modelo.addAttribute("categorias", Conductor.CATEGORIAS);
        return FORMULARIO;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.eliminar(id);
            atributos.addFlashAttribute("mensajeExito", "El conductor se eliminó correctamente.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/conductores";
    }
}
