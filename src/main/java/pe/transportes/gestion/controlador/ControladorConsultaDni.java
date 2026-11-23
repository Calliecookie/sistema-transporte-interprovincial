package pe.transportes.gestion.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.transportes.gestion.integracion.ResultadoConsultaDni;
import pe.transportes.gestion.integracion.ServicioConsultaDni;

@RestController
@RequestMapping("/consultas/dni")
public class ControladorConsultaDni {

    private final ServicioConsultaDni servicio;

    public ControladorConsultaDni(ServicioConsultaDni servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/pasajero/{dni}")
    public ResponseEntity<ResultadoConsultaDni> pasajero(@PathVariable String dni) {
        if (!dni.matches("\\d{8}")) {
            return ResponseEntity.badRequest().body(ResultadoConsultaDni.sinResultado("El DNI debe tener 8 dígitos"));
        }
        return ResponseEntity.ok(servicio.consultarPasajero(dni));
    }

    @GetMapping("/conductor/{dni}")
    public ResponseEntity<ResultadoConsultaDni> conductor(@PathVariable String dni) {
        if (!dni.matches("\\d{8}")) {
            return ResponseEntity.badRequest().body(ResultadoConsultaDni.sinResultado("El DNI debe tener 8 dígitos"));
        }
        return ResponseEntity.ok(servicio.consultarConductor(dni));
    }
}
