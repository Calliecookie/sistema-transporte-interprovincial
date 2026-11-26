package pe.transportes.gestion.servicio;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.modelo.PermisoRol;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.repositorio.PermisoRolRepositorio;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class ServicioPermiso {

    private final PermisoRolRepositorio repositorio;

    public ServicioPermiso(PermisoRolRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public Set<Modulo> asignados(Rol rol) {
        Set<Modulo> modulos = EnumSet.noneOf(Modulo.class);
        for (PermisoRol permiso : repositorio.findByRol(rol)) {
            modulos.add(permiso.getModulo());
        }
        return modulos;
    }

    @Transactional
    public void guardar(Rol rol, List<Modulo> seleccionados) {
        repositorio.deleteByRol(rol);
        repositorio.flush();
        for (Modulo modulo : EnumSet.copyOf(seleccionados.isEmpty() ? EnumSet.noneOf(Modulo.class) : seleccionados)) {
            if (modulo.getRolAplicable() == rol) {
                repositorio.save(new PermisoRol(rol, modulo));
            }
        }
    }
}
