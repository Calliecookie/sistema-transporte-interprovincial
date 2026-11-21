package pe.transportes.gestion.repositorio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.transportes.gestion.modelo.Bus;

public interface BusRepositorio extends JpaRepository<Bus, Long>, JpaSpecificationExecutor<Bus> {

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    List<Bus> findByActivoTrueOrderByPlacaAsc();

    long countByActivoTrue();
}
