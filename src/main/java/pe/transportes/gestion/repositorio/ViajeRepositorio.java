package pe.transportes.gestion.repositorio;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.transportes.gestion.modelo.EstadoViaje;
import pe.transportes.gestion.modelo.Viaje;

public interface ViajeRepositorio extends JpaRepository<Viaje, Long>, JpaSpecificationExecutor<Viaje> {

    boolean existsByRutaId(Long rutaId);

    boolean existsByBusId(Long busId);

    boolean existsByConductorId(Long conductorId);

    long countByEstado(EstadoViaje estado);

    List<Viaje> findTop6ByEstadoAndFechaSalidaAfterOrderByFechaSalidaAsc(EstadoViaje estado, LocalDateTime desde);

    @Query("select count(v) from Viaje v where v.bus.id = :busId and v.estado <> :cancelado and v.id <> :excluirId "
            + "and v.fechaSalida < :llegada and v.fechaLlegada > :salida")
    long contarCrucesDeBus(@Param("busId") Long busId, @Param("salida") LocalDateTime salida,
            @Param("llegada") LocalDateTime llegada, @Param("cancelado") EstadoViaje cancelado,
            @Param("excluirId") Long excluirId);

    @Query("select count(v) from Viaje v where v.conductor.id = :conductorId and v.estado <> :cancelado "
            + "and v.id <> :excluirId and v.fechaSalida < :llegada and v.fechaLlegada > :salida")
    long contarCrucesDeConductor(@Param("conductorId") Long conductorId, @Param("salida") LocalDateTime salida,
            @Param("llegada") LocalDateTime llegada, @Param("cancelado") EstadoViaje cancelado,
            @Param("excluirId") Long excluirId);
}
