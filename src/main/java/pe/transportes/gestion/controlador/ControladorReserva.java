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
import pe.transportes.gestion.modelo.AccionReserva;
import pe.transportes.gestion.modelo.Asiento;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.FormularioReserva;
import pe.transportes.gestion.modelo.MetodoPago;
import pe.transportes.gestion.modelo.Reserva;
import pe.transportes.gestion.servicio.ServicioReserva;
import pe.transportes.gestion.servicio.ServicioRuta;
import pe.transportes.gestion.servicio.ServicioViaje;
import pe.transportes.gestion.util.ErroresFormulario;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/reservas")
public class ControladorReserva {

    private static final String FORMULARIO = "reservas/formulario";

    private final ServicioReserva servicio;
    private final ServicioViaje servicioViaje;
    private final ServicioRuta servicioRuta;
    private final ConstructorMapa constructorMapa;

    public ControladorReserva(ServicioReserva servicio, ServicioViaje servicioViaje, ServicioRuta servicioRuta,
            ConstructorMapa constructorMapa) {
        this.servicio = servicio;
        this.servicioViaje = servicioViaje;
        this.servicioRuta = servicioRuta;
        this.constructorMapa = constructorMapa;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, @RequestParam(required = false) String dni,
            @RequestParam(required = false) EstadoReserva estado, @RequestParam(required = false) Long rutaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo) {
        Pageable paginacion = Paginacion.de(pagina, Sort.by(Sort.Direction.DESC, "fechaReserva"));
        Page<Reserva> resultado;
        try {
            resultado = servicio.buscar(dni, estado, rutaId, desde, hasta, paginacion);
        } catch (ReglaNegocioException excepcion) {
            modelo.addAttribute("mensajeError", excepcion.getMessage());
            resultado = Page.empty(paginacion);
        }
        modelo.addAttribute("pagina", resultado);
        modelo.addAttribute("rutas", servicioRuta.listar());
        modelo.addAttribute("estados", EstadoReserva.values());
        modelo.addAttribute("dni", dni);
        modelo.addAttribute("estado", estado);
        modelo.addAttribute("rutaId", rutaId);
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        modelo.addAttribute("parametros",
                ParametrosUrl.construir("dni", dni, "estado", estado, "rutaId", rutaId, "desde", desde, "hasta", hasta));
        return "reservas/lista";
    }

    @GetMapping("/viajes")
    public String viajes(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long rutaId, Model modelo) {
        Pageable paginacion = Paginacion.de(pagina, Sort.by(Sort.Direction.ASC, "fechaSalida"));
        modelo.addAttribute("pagina", servicioViaje.buscarDisponibles(fecha, rutaId, paginacion));
        modelo.addAttribute("rutas", servicioRuta.listarActivas());
        modelo.addAttribute("fecha", fecha);
        modelo.addAttribute("rutaId", rutaId);
        modelo.addAttribute("parametros", ParametrosUrl.construir("fecha", fecha, "rutaId", rutaId));
        return "reservas/viajes";
    }

    @GetMapping("/viajes/{id}")
    public String mapa(@PathVariable Long id, Model modelo) {
        constructorMapa.cargar(modelo, id);
        return "reservas/mapa";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam Long viajeId, @RequestParam Long asientoId, Model modelo,
            RedirectAttributes atributos) {
        try {
            Asiento asiento = servicio.obtenerAsientoLibre(viajeId, asientoId);
            FormularioReserva formulario = new FormularioReserva();
            formulario.setViajeId(viajeId);
            formulario.setAsientoId(asientoId);
            modelo.addAttribute("formulario", formulario);
            cargarContexto(modelo, asiento);
            return FORMULARIO;
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
            return "redirect:/reservas/viajes/" + viajeId;
        }
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("formulario") FormularioReserva formulario,
            BindingResult resultado, Model modelo, RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                Reserva reserva = servicio.registrar(formulario);
                String mensaje = reserva.getEstado() == EstadoReserva.VENDIDA
                        ? "El pasaje se vendió correctamente."
                        : "La reserva se registró correctamente.";
                atributos.addFlashAttribute("mensajeExito", mensaje);
                return "redirect:/reservas/" + reserva.getId();
            } catch (ReglaNegocioException excepcion) {
                ErroresFormulario.registrar(resultado, excepcion);
            }
        }
        cargarContexto(modelo, buscarAsientoDelFormulario(formulario));
        return FORMULARIO;
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model modelo) {
        Reserva reserva = servicio.obtener(id);
        modelo.addAttribute("reserva", reserva);
        modelo.addAttribute("pasaje", servicio.pasajeDeReserva(id));
        modelo.addAttribute("ventaAbierta", servicio.ventaAbierta(reserva.getViaje()));
        modelo.addAttribute("metodosPago", MetodoPago.values());
        return "reservas/detalle";
    }

    @PostMapping("/{id}/vender")
    public String vender(@PathVariable Long id, @RequestParam(required = false) MetodoPago metodoPago,
            RedirectAttributes atributos) {
        try {
            servicio.confirmarVenta(id, metodoPago);
            atributos.addFlashAttribute("mensajeExito", "El pasaje se vendió correctamente.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/reservas/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            servicio.anular(id);
            atributos.addFlashAttribute("mensajeExito", "La reserva se anuló y el asiento quedó libre.");
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/reservas/" + id;
    }

    private Asiento buscarAsientoDelFormulario(FormularioReserva formulario) {
        if (formulario.getViajeId() == null || formulario.getAsientoId() == null) {
            return null;
        }
        return servicio.buscarAsiento(formulario.getViajeId(), formulario.getAsientoId());
    }

    private void cargarContexto(Model modelo, Asiento asiento) {
        modelo.addAttribute("asiento", asiento);
        modelo.addAttribute("viaje", asiento == null ? null : servicioViaje.obtener(asiento.getViaje().getId()));
        modelo.addAttribute("acciones", AccionReserva.values());
        modelo.addAttribute("metodosPago", MetodoPago.values());
    }
}
