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
import pe.transportes.gestion.modelo.Bus;
import pe.transportes.gestion.servicio.ServicioBus;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/buses")
public class ControladorBus {

    private static final String FORMULARIO = "buses/formulario";

    private final ServicioBus servicio;

    public ControladorBus(ServicioBus servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo, Model modelo) {
        modelo.addAttribute("pagina", servicio.buscar(texto, activo, Paginacion.de(pagina, Sort.by("placa"))));
        modelo.addAttribute("texto", texto);
        modelo.addAttribute("activo", activo);
        modelo.addAttribute("parametros", ParametrosUrl.construir("texto", texto, "activo", activo));
        return "buses/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("bus", new Bus());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("bus") Bus bus, BindingResult resultado,
            RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(bus);
                atributos.addFlashAttribute("mensajeExito", "El bus se registró correctamente.");
                return "redirect:/buses";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        return FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("bus", servicio.obtener(id));
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("bus") Bus bus,
            BindingResult resultado, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, bus);
                atributos.addFlashAttribute("mensajeExito", "Los datos del bus se actualizaron correctamente.");
                return "redirect:/buses";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        return FORMULARIO;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.eliminar(id);
            atributos.addFlashAttribute("mensajeExito", "El bus se eliminó correctamente.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/buses";
    }
}
