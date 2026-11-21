package pe.transportes.gestion.repositorio;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Viaje;

public final class EspecificacionViaje {

    private EspecificacionViaje() {
    }

    public static Specification<Viaje> filtrar(LocalDate desde, LocalDate hasta, Long rutaId, EstadoViaje estado) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (desde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(raiz.<LocalDateTime>get("fechaSalida"),
                        desde.atStartOfDay()));
            }
            if (hasta != null) {
                condiciones.add(cb.lessThan(raiz.<LocalDateTime>get("fechaSalida"),
                        hasta.plusDays(1).atStartOfDay()));
            }
            if (rutaId != null) {
                condiciones.add(cb.equal(raiz.get("ruta").get("id"), rutaId));
            }
            if (estado != null) {
                condiciones.add(cb.equal(raiz.get("estado"), estado));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    public static Specification<Viaje> disponibles(LocalDateTime ahora, LocalDate fecha, Long rutaId) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.equal(raiz.get("estado"), EstadoViaje.PROGRAMADO));
            condiciones.add(cb.greaterThan(raiz.<LocalDateTime>get("fechaSalida"), ahora));
            if (fecha != null) {
                condiciones.add(cb.greaterThanOrEqualTo(raiz.<LocalDateTime>get("fechaSalida"),
                        fecha.atStartOfDay()));
                condiciones.add(cb.lessThan(raiz.<LocalDateTime>get("fechaSalida"),
                        fecha.plusDays(1).atStartOfDay()));
            }
            if (rutaId != null) {
                condiciones.add(cb.equal(raiz.get("ruta").get("id"), rutaId));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }
}
