package pe.transportes.gestion.repositorio;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.transportes.gestion.modelo.Asiento;
import pe.transportes.gestion.modelo.EstadoAsiento;

public interface AsientoRepositorio extends JpaRepository<Asiento, Long> {

    List<Asiento> findByViajeIdOrderByNumeroAsc(Long viajeId);

    long countByViajeIdAndEstado(Long viajeId, EstadoAsiento estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Asiento a where a.id = :id")
    Optional<Asiento> buscarParaActualizar(@Param("id") Long id);

    @Modifying
    @Query("delete from Asiento a where a.viaje.id = :viajeId")
    void eliminarPorViaje(@Param("viajeId") Long viajeId);

    @Query("select a.viaje.id, count(a) from Asiento a where a.estado = :estado and a.viaje.id in :ids "
            + "group by a.viaje.id")
    List<Object[]> contarPorViajes(@Param("ids") Collection<Long> ids, @Param("estado") EstadoAsiento estado);
}
