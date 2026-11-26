package pe.transportes.gestion.repositorio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.transportes.gestion.modelo.TokenRecuperacion;

public interface TokenRecuperacionRepositorio extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByHuella(String huella);

    List<TokenRecuperacion> findByUsuarioIdAndUsadoFalse(Long usuarioId);

    long countByUsuarioIdAndFechaCreacionAfter(Long usuarioId, LocalDateTime desde);
}
