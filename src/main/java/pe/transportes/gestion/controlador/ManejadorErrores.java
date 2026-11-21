package pe.transportes.gestion.controlador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;

@ControllerAdvice
public class ManejadorErrores {

    private static final Logger registro = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String recursoNoEncontrado(RecursoNoEncontradoException excepcion, Model modelo) {
        modelo.addAttribute("mensaje", excepcion.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String reglaNegocio(ReglaNegocioException excepcion, Model modelo) {
        return vistaGeneral(modelo, 400, "No se pudo completar la operación", excepcion.getMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String solicitudInvalida(Exception excepcion, Model modelo) {
        return vistaGeneral(modelo, 400, "Solicitud no válida",
                "Los datos enviados no son correctos. Regrese a la página anterior e inténtelo de nuevo.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String integridadDeDatos(DataIntegrityViolationException excepcion, Model modelo) {
        registro.warn("Violación de integridad de datos: {}", excepcion.getMostSpecificCause().getMessage());
        return vistaGeneral(modelo, 409, "Datos en conflicto",
                "La operación no se pudo completar porque el registro está relacionado con otros datos "
                + "o ya existe un valor igual.");
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String concurrencia(ConcurrencyFailureException excepcion, Model modelo) {
        registro.warn("Conflicto de concurrencia: {}", excepcion.getMessage());
        return vistaGeneral(modelo, 409, "Registro en uso",
                "Otro usuario está operando sobre el mismo registro. Espere unos segundos e inténtelo de nuevo.");
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String accesoADatos(DataAccessException excepcion, Model modelo) {
        registro.error("Error al acceder a la base de datos", excepcion);
        return "error/500";
    }

    private String vistaGeneral(Model modelo, int codigo, String titulo, String mensaje) {
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("titulo", titulo);
        modelo.addAttribute("mensaje", mensaje);
        return "error";
    }
}
