package pe.transportes.gestion.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.transportes.gestion.servicio.ServicioViajeConductor;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/mis-viajes")
public class ControladorViajeConductor {

    private final ServicioViajeConductor servicio;

    public ControladorViajeConductor(ServicioViajeConductor servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model modelo) {
        try {
            modelo.addAttribute("conductor", servicio.conductorActual());
            modelo.addAttribute("pendientes", servicio.viajesPendientes());
            modelo.addAttribute("recientes", servicio.viajesRecientes());
        } catch (ReglaNegocioException excepcion) {
            modelo.addAttribute("mensajeError", excepcion.getMessage());
        }
        return "mis_viajes/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("viaje", servicio.obtener(id));
        modelo.addAttribute("pasajeros", servicio.pasajeros(id));
        modelo.addAttribute("incidencias", servicio.incidencias(id));
        return "mis_viajes/detalle";
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, @RequestParam(required = false) String comentario,
            RedirectAttributes atributos) {
        return ejecutar(id, atributos, "Se registró la salida del viaje.", () -> servicio.iniciar(id, comentario));
    }

    @PostMapping("/{id}/pausar")
    public String pausar(@PathVariable Long id, @RequestParam(required = false) String comentario,
            RedirectAttributes atributos) {
        return ejecutar(id, atributos, "Se registró la pausa y se avisó a la oficina.",
                () -> servicio.pausar(id, comentario));
    }

    @PostMapping("/{id}/reanudar")
    public String reanudar(@PathVariable Long id, @RequestParam(required = false) String comentario,
            RedirectAttributes atributos) {
        return ejecutar(id, atributos, "El viaje se reanudó.", () -> servicio.reanudar(id, comentario));
    }

    @PostMapping("/{id}/retraso")
    public String retraso(@PathVariable Long id, @RequestParam(required = false) Integer minutos,
            @RequestParam(required = false) String comentario, RedirectAttributes atributos) {
        return ejecutar(id, atributos, "Se registró el retraso y se avisó a la oficina.",
                () -> servicio.reportarRetraso(id, minutos, comentario));
    }

    @PostMapping("/{id}/accidente")
    public String accidente(@PathVariable Long id, @RequestParam(required = false) String comentario,
            RedirectAttributes atributos) {
        return ejecutar(id, atributos, "Se envió la alerta de accidente al administrador y a los vendedores.",
                () -> servicio.reportarAccidente(id, comentario));
    }

    @PostMapping("/{id}/observacion")
    public String observacion(@PathVariable Long id, @RequestParam(required = false) String comentario,
            RedirectAttributes atributos) {
        return ejecutar(id, atributos, "La observación se registró correctamente.",
                () -> servicio.registrarObservacion(id, comentario));
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, @RequestParam(required = false) String comentario,
            RedirectAttributes atributos) {
        return ejecutar(id, atributos, "Se confirmó la llegada a destino.", () -> servicio.finalizar(id, comentario));
    }

    private String ejecutar(Long id, RedirectAttributes atributos, String exito, Runnable accion) {
        try {
            accion.run();
            atributos.addFlashAttribute("mensajeExito", exito);
        } catch (ReglaNegocioException excepcion) {
            atributos.addFlashAttribute("mensajeError", excepcion.getMessage());
        }
        return "redirect:/mis-viajes/" + id;
    }
}
