package pe.transportes.gestion.repositorio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.transportes.gestion.modelo.EstadoReserva;
import pe.transportes.gestion.modelo.Reserva;

public interface ReservaRepositorio extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    List<Reserva> findByViajeIdAndEstadoNot(Long viajeId, EstadoReserva estado);

    boolean existsByViajeId(Long viajeId);

    boolean existsByPasajeroId(Long pasajeroId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByViajeIdAndPasajeroIdAndEstadoNot(Long viajeId, Long pasajeroId, EstadoReserva estado);

    long countByEstado(EstadoReserva estado);
}
