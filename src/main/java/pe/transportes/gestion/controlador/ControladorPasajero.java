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
import pe.transportes.gestion.modelo.Pasajero;
import pe.transportes.gestion.servicio.ServicioPasajero;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/pasajeros")
public class ControladorPasajero {

    private static final String FORMULARIO = "pasajeros/formulario";

    private final ServicioPasajero servicio;

    public ControladorPasajero(ServicioPasajero servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, @RequestParam(required = false) String dni,
            @RequestParam(required = false) String texto, Model modelo) {
        modelo.addAttribute("pagina",
                servicio.buscar(dni, texto, Paginacion.de(pagina, Sort.by("apellidos", "nombres"))));
        modelo.addAttribute("dni", dni);
        modelo.addAttribute("texto", texto);
        modelo.addAttribute("parametros", ParametrosUrl.construir("dni", dni, "texto", texto));
        return "pasajeros/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("pasajero", new Pasajero());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("pasajero") Pasajero pasajero, BindingResult resultado,
            RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(pasajero);
                atributos.addFlashAttribute("mensajeExito", "El pasajero se registró correctamente.");
                return "redirect:/pasajeros";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        return FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("pasajero", servicio.obtener(id));
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("pasajero") Pasajero pasajero,
            BindingResult resultado, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, pasajero);
                atributos.addFlashAttribute("mensajeExito", "Los datos del pasajero se actualizaron correctamente.");
                return "redirect:/pasajeros";
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
            atributos.addFlashAttribute("mensajeExito", "El pasajero se eliminó correctamente.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/pasajeros";
    }
}
