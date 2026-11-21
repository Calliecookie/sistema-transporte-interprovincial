package pe.transportes.gestion.repositorio;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import pe.transportes.gestion.modelo.EstadoPasaje;
import pe.transportes.gestion.modelo.Pasaje;
import pe.transportes.gestion.util.Patrones;

public final class EspecificacionPasaje {

    private EspecificacionPasaje() {
    }

    public static Specification<Pasaje> filtrar(String dni, String codigo, EstadoPasaje estado, LocalDate desde,
            LocalDate hasta) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (Patrones.tieneTexto(dni)) {
                condiciones.add(cb.like(raiz.get("reserva").get("pasajero").<String>get("dni"),
                        Patrones.contiene(dni), '\\'));
            }
            if (Patrones.tieneTexto(codigo)) {
                condiciones.add(cb.like(cb.lower(raiz.<String>get("codigo")), Patrones.contiene(codigo), '\\'));
            }
            if (estado != null) {
                condiciones.add(cb.equal(raiz.get("estado"), estado));
            }
            if (desde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(raiz.<LocalDateTime>get("fechaVenta"),
                        desde.atStartOfDay()));
            }
            if (hasta != null) {
                condiciones.add(cb.lessThan(raiz.<LocalDateTime>get("fechaVenta"),
                        hasta.plusDays(1).atStartOfDay()));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }
}
