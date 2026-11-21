package pe.transportes.gestion.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Comparator;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEntidades {

    private final Validator validador;

    public ValidadorEntidades(Validator validador) {
        this.validador = validador;
    }

    public void validar(Object objeto) {
        Set<ConstraintViolation<Object>> violaciones = validador.validate(objeto);
        if (violaciones.isEmpty()) {
            return;
        }
        ConstraintViolation<Object> primera = violaciones.stream()
                .min(Comparator.comparing((ConstraintViolation<Object> v) -> v.getPropertyPath().toString())
                        .thenComparing(ConstraintViolation::getMessage))
                .orElseThrow();
        throw new ReglaNegocioException(primera.getPropertyPath().toString(), primera.getMessage());
    }
}
