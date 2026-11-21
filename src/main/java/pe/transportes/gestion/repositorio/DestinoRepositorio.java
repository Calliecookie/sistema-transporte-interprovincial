package pe.transportes.gestion.repositorio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.transportes.gestion.modelo.Destino;

public interface DestinoRepositorio extends JpaRepository<Destino, Long>, JpaSpecificationExecutor<Destino> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Destino> findAllByOrderByNombreAsc();
}
