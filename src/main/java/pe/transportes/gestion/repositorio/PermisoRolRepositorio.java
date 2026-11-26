package pe.transportes.gestion.repositorio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.modelo.PermisoRol;
import pe.transportes.gestion.modelo.Rol;

public interface PermisoRolRepositorio extends JpaRepository<PermisoRol, Long> {

    List<PermisoRol> findByRol(Rol rol);

    boolean existsByRolAndModulo(Rol rol, Modulo modulo);

    void deleteByRol(Rol rol);
}
