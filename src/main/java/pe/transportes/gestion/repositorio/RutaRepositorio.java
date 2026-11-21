package pe.transportes.gestion.repositorio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.transportes.gestion.modelo.Ruta;

public interface RutaRepositorio extends JpaRepository<Ruta, Long>, JpaSpecificationExecutor<Ruta> {

    boolean existsByOrigenIdAndDestinoId(Long origenId, Long destinoId);

    boolean existsByOrigenIdAndDestinoIdAndIdNot(Long origenId, Long destinoId, Long id);

    boolean existsByOrigenIdOrDestinoId(Long origenId, Long destinoId);

    List<Ruta> findAllByOrderByOrigenNombreAscDestinoNombreAsc();

    List<Ruta> findByActivaTrueOrderByOrigenNombreAscDestinoNombreAsc();
}
