package pe.transportes.gestion.controlador;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.transportes.gestion.modelo.EstadoPasaje;
import pe.transportes.gestion.modelo.Pasaje;
import pe.transportes.gestion.servicio.ServicioPasaje;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/pasajes")
public class ControladorPasaje {

    private final ServicioPasaje servicio;

    public ControladorPasaje(ServicioPasaje servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, @RequestParam(required = false) String dni,
            @RequestParam(required = false) String codigo, @RequestParam(required = false) EstadoPasaje estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo) {
        Pageable paginacion = Paginacion.de(pagina, Sort.by(Sort.Direction.DESC, "fechaVenta"));
        Page<Pasaje> resultado;
        try {
            resultado = servicio.buscar(dni, codigo, estado, desde, hasta, paginacion);
        } catch (ReglaNegocioException excepcion) {
            modelo.addAttribute("mensajeError", excepcion.getMessage());
            resultado = Page.empty(paginacion);
        }
        modelo.addAttribute("pagina", resultado);
        modelo.addAttribute("estados", EstadoPasaje.values());
        modelo.addAttribute("dni", dni);
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("estado", estado);
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        modelo.addAttribute("parametros",
                ParametrosUrl.construir("dni", dni, "codigo", codigo, "estado", estado, "desde", desde, "hasta", hasta));
        return "pasajes/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("pasaje", servicio.obtener(id));
        return "pasajes/detalle";
    }
}
