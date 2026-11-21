package pe.transportes.gestion.repositorio;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.transportes.gestion.modelo.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuarioAndIdNot(String nombreUsuario, Long id);
}
