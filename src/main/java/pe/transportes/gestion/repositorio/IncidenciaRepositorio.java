package pe.transportes.gestion.repositorio;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.transportes.gestion.modelo.Incidencia;
import pe.transportes.gestion.modelo.TipoIncidencia;

public interface IncidenciaRepositorio extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByViajeIdOrderByFechaRegistroDesc(Long viajeId);

    List<Incidencia> findByAtendidaFalseAndTipoInOrderByFechaRegistroDesc(Collection<TipoIncidencia> tipos);

    long countByAtendidaFalseAndTipoIn(Collection<TipoIncidencia> tipos);

    List<Incidencia> findTop20ByAtendidaTrueAndTipoInOrderByFechaAtencionDesc(Collection<TipoIncidencia> tipos);

    boolean existsByUsuarioIdOrAtendidaPorId(Long usuarioId, Long atendidaPorId);
}
