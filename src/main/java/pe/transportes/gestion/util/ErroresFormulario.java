package pe.transportes.gestion.util;

import org.springframework.beans.InvalidPropertyException;
import org.springframework.validation.BindingResult;

public final class ErroresFormulario {

    private ErroresFormulario() {
    }

    public static void registrar(BindingResult resultado, ReglaNegocioException excepcion) {
        String campo = excepcion.getCampo();
        if (campo != null) {
            try {
                resultado.rejectValue(campo, "regla.negocio", excepcion.getMessage());
                return;
            } catch (InvalidPropertyException e) {
                resultado.reject("regla.negocio", excepcion.getMessage());
                return;
            }
        }
        resultado.reject("regla.negocio", excepcion.getMessage());
    }
}
