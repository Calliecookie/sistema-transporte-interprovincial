package pe.transportes.gestion.controlador;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AtributosGlobales {

    private final Clock reloj;

    public AtributosGlobales(Clock reloj) {
        this.reloj = reloj;
    }

    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("hoy")
    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }
}
