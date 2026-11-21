package pe.transportes.gestion.controlador;

import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Ruta;
import pe.transportes.gestion.modelo.Viaje;
import pe.transportes.gestion.servicio.ServicioBus;
import pe.transportes.gestion.servicio.ServicioConductor;
import pe.transportes.gestion.servicio.ServicioRuta;
import pe.transportes.gestion.servicio.ServicioViaje;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/viajes")
public class ControladorViaje {

    private static final String FORMULARIO = "viajes/formulario";

    private final ServicioViaje servicio;
    private final ConstructorMapa constructorMapa;
    private final ServicioRuta servicioRuta;
    private final ServicioBus servicioBus;
    private final ServicioConductor servicioConductor;

    public ControladorViaje(ServicioViaje servicio, ConstructorMapa constructorMapa, ServicioRuta servicioRuta,
            ServicioBus servicioBus, ServicioConductor servicioConductor) {
        this.servicio = servicio;
        this.constructorMapa = constructorMapa;
        this.servicioRuta = servicioRuta;
        this.servicioBus = servicioBus;
        this.servicioConductor = servicioConductor;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long rutaId, @RequestParam(required = false) EstadoViaje estado,
            Model modelo) {
        Pageable paginacion = Paginacion.de(pagina, Sort.by(Sort.Direction.DESC, "fechaSalida"));
        Page<Viaje> resultado;
        try {
            resultado = servicio.buscar(desde, hasta, rutaId, estado, paginacion);
        } catch (ReglaNegocioException excepcion) {
            modelo.addAttribute("mensajeError", excepcion.getMessage());
            resultado = Page.empty(paginacion);
        }
        modelo.addAttribute("pagina", resultado);
        modelo.addAttribute("rutas", servicioRuta.listar());
        modelo.addAttribute("estados", EstadoViaje.values());
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        modelo.addAttribute("rutaId", rutaId);
        modelo.addAttribute("estado", estado);
        modelo.addAttribute("parametros",
                ParametrosUrl.construir("desde", desde, "hasta", hasta, "rutaId", rutaId, "estado", estado));
        return "viajes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        Viaje viaje = new Viaje();
        viaje.setRuta(new Ruta());
        viaje.setBus(new Bus());
        viaje.setConductor(new Conductor());
        modelo.addAttribute("viaje", viaje);
        cargarOpciones(modelo);
        return FORMULARIO;
    }

    @PostMapping
    public String programar(@Valid @ModelAttribute("viaje") Viaje viaje, BindingResult resultado, Model modelo,
            RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                Viaje guardado = servicio.programar(viaje);
                atributos.addFlashAttribute("mensajeExito",
                        "El viaje se programó correctamente y se generaron sus asientos.");
                return "redirect:/viajes/" + guardado.getId();
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        cargarOpciones(modelo);
        return FORMULARIO;
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model modelo) {
        constructorMapa.cargar(modelo, id);
        return "viajes/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("viaje", servicio.obtener(id));
        cargarOpciones(modelo);
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("viaje") Viaje viaje,
            BindingResult resultado, Model modelo, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, viaje);
                atributos.addFlashAttribute("mensajeExito", "El viaje se actualizó correctamente.");
                return "redirect:/viajes/" + id;
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        cargarOpciones(modelo);
        return FORMULARIO;
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.iniciar(id);
            atributos.addFlashAttribute("mensajeExito", "El viaje pasó a estado en curso.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/viajes/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.finalizar(id);
            atributos.addFlashAttribute("mensajeExito", "El viaje se marcó como finalizado.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/viajes/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.cancelar(id);
            atributos.addFlashAttribute("mensajeExito",
                    "El viaje se canceló y las reservas activas fueron anuladas.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/viajes/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.eliminar(id);
            atributos.addFlashAttribute("mensajeExito", "El viaje se eliminó correctamente.");
            return "redirect:/viajes";
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
            return "redirect:/viajes/" + id;
        }
    }

    private void cargarOpciones(Model modelo) {
        modelo.addAttribute("rutas", servicioRuta.listarActivas());
        modelo.addAttribute("buses", servicioBus.listarActivos());
        modelo.addAttribute("conductores", servicioConductor.listarActivos());
    }
}
