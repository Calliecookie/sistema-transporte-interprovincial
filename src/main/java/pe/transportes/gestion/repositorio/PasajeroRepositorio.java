package pe.transportes.gestion.repositorio;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.transportes.gestion.modelo.Pasajero;

public interface PasajeroRepositorio extends JpaRepository<Pasajero, Long>, JpaSpecificationExecutor<Pasajero> {

    Optional<Pasajero> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);
}
