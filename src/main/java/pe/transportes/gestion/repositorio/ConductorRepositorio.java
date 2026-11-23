package pe.transportes.gestion.repositorio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.transportes.gestion.modelo.Conductor;

public interface ConductorRepositorio extends JpaRepository<Conductor, Long>, JpaSpecificationExecutor<Conductor> {

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByLicencia(String licencia);

    boolean existsByLicenciaAndIdNot(String licencia, Long id);

    List<Conductor> findByActivoTrueOrderByApellidosAscNombresAsc();

    long countByActivoTrue();

    Optional<Conductor> findFirstByActivoTrueOrderByIdAsc();

    Optional<Conductor> findByDni(String dni);
}
