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
import pe.transportes.gestion.modelo.Ruta;
import pe.transportes.gestion.servicio.ServicioDestino;
import pe.transportes.gestion.servicio.ServicioRuta;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/rutas")
public class ControladorRuta {

    private static final String FORMULARIO = "rutas/formulario";

    private final ServicioRuta servicio;
    private final ServicioDestino servicioDestino;

    public ControladorRuta(ServicioRuta servicio, ServicioDestino servicioDestino) {
        this.servicio = servicio;
        this.servicioDestino = servicioDestino;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(required = false) Long origenId, @RequestParam(required = false) Long destinoId,
            @RequestParam(required = false) Boolean activa, Model modelo) {
        modelo.addAttribute("pagina", servicio.buscar(origenId, destinoId, activa,
                Paginacion.de(pagina, Sort.by("origen.nombre", "destino.nombre"))));
        modelo.addAttribute("destinos", servicioDestino.listar());
        modelo.addAttribute("origenId", origenId);
        modelo.addAttribute("destinoId", destinoId);
        modelo.addAttribute("activa", activa);
        modelo.addAttribute("parametros",
                ParametrosUrl.construir("origenId", origenId, "destinoId", destinoId, "activa", activa));
        return "rutas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        Ruta ruta = new Ruta();
        ruta.setOrigen(new Destino());
        ruta.setDestino(new Destino());
        modelo.addAttribute("ruta", ruta);
        modelo.addAttribute("destinos", servicioDestino.listar());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("ruta") Ruta ruta, BindingResult resultado, Model modelo,
            RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(ruta);
                atributos.addFlashAttribute("mensajeExito", "La ruta se registró correctamente.");
                return "redirect:/rutas";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        modelo.addAttribute("destinos", servicioDestino.listar());
        return FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("ruta", servicio.obtener(id));
        modelo.addAttribute("destinos", servicioDestino.listar());
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("ruta") Ruta ruta,
            BindingResult resultado, Model modelo, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, ruta);
                atributos.addFlashAttribute("mensajeExito", "La ruta se actualizó correctamente.");
                return "redirect:/rutas";
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        modelo.addAttribute("destinos", servicioDestino.listar());
        return FORMULARIO;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.eliminar(id);
            atributos.addFlashAttribute("mensajeExito", "La ruta se eliminó correctamente.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/rutas";
    }
}
