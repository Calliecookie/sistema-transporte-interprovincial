package pe.transportes.gestion.repositorio;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.transportes.gestion.modelo.EstadoPasaje;
import pe.transportes.gestion.modelo.Pasaje;

public interface PasajeRepositorio extends JpaRepository<Pasaje, Long>, JpaSpecificationExecutor<Pasaje> {

    Optional<Pasaje> findByReservaId(Long reservaId);

    List<Pasaje> findByReservaIdIn(Collection<Long> reservaIds);

    long countByEstado(EstadoPasaje estado);

    boolean existsByUsuarioId(Long usuarioId);

    @Query("select sum(p.precio) from Pasaje p where p.estado = :estado")
    BigDecimal sumarPrecios(@Param("estado") EstadoPasaje estado);

    @Query("select r.viaje.id, sum(p.precio) from Pasaje p join p.reserva r where p.estado = :estado "
            + "and r.viaje.id in :ids group by r.viaje.id")
    List<Object[]> sumarPreciosPorViaje(@Param("ids") Collection<Long> ids, @Param("estado") EstadoPasaje estado);
}
