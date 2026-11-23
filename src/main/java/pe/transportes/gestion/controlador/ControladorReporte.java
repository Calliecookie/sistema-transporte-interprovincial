package pe.transportes.gestion.controlador;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.servicio.FilaReportePasajero;
import pe.transportes.gestion.servicio.FilaReporteViaje;
import pe.transportes.gestion.servicio.ResumenReporteViajes;
import pe.transportes.gestion.servicio.ServicioReporte;
import pe.transportes.gestion.servicio.ServicioRuta;
import pe.transportes.gestion.util.Paginacion;
import pe.transportes.gestion.util.ParametrosUrl;
import pe.transportes.gestion.util.ReglaNegocioException;

@Controller
@RequestMapping("/reportes")
public class ControladorReporte {

    private static final MediaType TIPO_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final ServicioReporte servicio;
    private final ServicioRuta servicioRuta;
    private final Clock reloj;

    public ControladorReporte(ServicioReporte servicio, ServicioRuta servicioRuta, Clock reloj) {
        this.servicio = servicio;
        this.servicioRuta = servicioRuta;
        this.reloj = reloj;
    }

    @GetMapping
    public String indice() {
        return "reportes/indice";
    }

    @GetMapping("/viajes")
    public String viajes(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long rutaId, @RequestParam(required = false) EstadoViaje estado,
            Model modelo) {
        Pageable paginacion = Paginacion.de(pagina, Sort.by(Sort.Direction.DESC, "fechaSalida"));
        Page<FilaReporteViaje> resultado;
        ResumenReporteViajes resumen;
        try {
            resultado = servicio.viajes(desde, hasta, rutaId, estado, paginacion);
            resumen = servicio.resumenViajes(desde, hasta, rutaId, estado);
        } catch (ReglaNegocioException excepcion) {
            modelo.addAttribute("mensajeError", excepcion.getMessage());
            resultado = Page.empty(paginacion);
            resumen = new ResumenReporteViajes(0, 0, 0, BigDecimal.ZERO);
        }
        modelo.addAttribute("pagina", resultado);
        modelo.addAttribute("resumen", resumen);
        modelo.addAttribute("rutas", servicioRuta.listar());
        modelo.addAttribute("estados", EstadoViaje.values());
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        modelo.addAttribute("rutaId", rutaId);
        modelo.addAttribute("estado", estado);
        modelo.addAttribute("parametros",
                ParametrosUrl.construir("desde", desde, "hasta", hasta, "rutaId", rutaId, "estado", estado));
        return "reportes/viajes";
    }

    @GetMapping("/viajes/csv")
    public ResponseEntity<byte[]> viajesCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long rutaId, @RequestParam(required = false) EstadoViaje estado) {
        return respuestaCsv("reporte_viajes", servicio.csvViajes(desde, hasta, rutaId, estado));
    }

    @GetMapping("/pasajeros")
    public String pasajeros(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(required = false) String dni, @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) Long rutaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo) {
        Pageable paginacion = Paginacion.de(pagina, ServicioReporte.ordenPasajeros());
        Page<FilaReportePasajero> resultado;
        try {
            resultado = servicio.pasajeros(dni, estado, rutaId, desde, hasta, paginacion);
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
        return "reportes/pasajeros";
    }

    @GetMapping("/pasajeros/csv")
    public ResponseEntity<byte[]> pasajerosCsv(@RequestParam(required = false) String dni,
            @RequestParam(required = false) EstadoReserva estado, @RequestParam(required = false) Long rutaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return respuestaCsv("reporte_pasajeros", servicio.csvPasajeros(dni, estado, rutaId, desde, hasta));
    }

    @GetMapping("/viajes/{id}/pasajeros")
    public String pasajerosDelViaje(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("ocupacion", servicio.ocupacionDelViaje(id));
        modelo.addAttribute("filas", servicio.pasajerosDelViaje(id));
        return "reportes/pasajeros_viaje";
    }

    @GetMapping("/viajes/{id}/pasajeros/csv")
    public ResponseEntity<byte[]> pasajerosDelViajeCsv(@PathVariable Long id) {
        return respuestaCsv("pasajeros_viaje_" + id, servicio.csvPasajerosDelViaje(id));
    }

    private ResponseEntity<byte[]> respuestaCsv(String prefijo, byte[] contenido) {
        String nombre = prefijo + "_" + LocalDate.now(reloj) + ".csv";
        return ResponseEntity.ok()
                .contentType(TIPO_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .body(contenido);
    }
}
