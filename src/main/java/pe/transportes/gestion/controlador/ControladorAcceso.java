package pe.transportes.gestion.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ControladorAcceso {

    @GetMapping("/login")
    public String login(Authentication autenticacion) {
        if (autenticacion != null && autenticacion.isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @RequestMapping("/acceso-denegado")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accesoDenegado() {
        return "error/403";
    }
}
