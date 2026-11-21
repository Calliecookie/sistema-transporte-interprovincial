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
import pe.transportes.gestion.modelo.Destino;
import pe.transportes.gestion.servicio.ServicioDestino;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/destinos")
public class ControladorDestino {

    private static final String FORMULARIO = "destinos/formulario";

    private final ServicioDestino servicio;

    public ControladorDestino(ServicioDestino servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, @RequestParam(required = false) String texto,
            Model modelo) {
        modelo.addAttribute("pagina", servicio.buscar(texto, Paginacion.de(pagina, Sort.by("nombre"))));
        modelo.addAttribute("texto", texto);
        modelo.addAttribute("parametros", ParametrosUrl.construir("texto", texto));
        return "destinos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("destino", new Destino());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("destino") Destino destino, BindingResult resultado,
            RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(destino);
                atributos.addFlashAttribute("mensajeExito", "El destino se registró correctamente.");
                return "redirect:/destinos";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        return FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("destino", servicio.obtener(id));
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("destino") Destino destino,
            BindingResult resultado, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, destino);
                atributos.addFlashAttribute("mensajeExito", "El destino se actualizó correctamente.");
                return "redirect:/destinos";
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
            atributos.addFlashAttribute("mensajeExito", "El destino se eliminó correctamente.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/destinos";
    }
}
