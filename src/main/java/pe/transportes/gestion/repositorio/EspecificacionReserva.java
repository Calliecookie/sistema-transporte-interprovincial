package pe.transportes.gestion.repositorio;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.Reserva;
import pe.transportes.gestion.util.Patrones;

public final class EspecificacionReserva {

    private EspecificacionReserva() {
    }

    public static Specification<Reserva> filtrar(String dni, EstadoReserva estado, Long rutaId,
            LocalDate reservaDesde, LocalDate reservaHasta, LocalDate viajeDesde, LocalDate viajeHasta) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (Patrones.tieneTexto(dni)) {
                condiciones.add(cb.like(raiz.get("pasajero").<String>get("dni"), Patrones.contiene(dni), '\\'));
            }
            if (estado != null) {
                condiciones.add(cb.equal(raiz.get("estado"), estado));
            }
            if (rutaId != null) {
                condiciones.add(cb.equal(raiz.get("viaje").get("ruta").get("id"), rutaId));
            }
            if (reservaDesde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(raiz.<LocalDateTime>get("fechaReserva"),
                        reservaDesde.atStartOfDay()));
            }
            if (reservaHasta != null) {
                condiciones.add(cb.lessThan(raiz.<LocalDateTime>get("fechaReserva"),
                        reservaHasta.plusDays(1).atStartOfDay()));
            }
            if (viajeDesde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(raiz.get("viaje").<LocalDateTime>get("fechaSalida"),
                        viajeDesde.atStartOfDay()));
            }
            if (viajeHasta != null) {
                condiciones.add(cb.lessThan(raiz.get("viaje").<LocalDateTime>get("fechaSalida"),
                        viajeHasta.plusDays(1).atStartOfDay()));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }
}
