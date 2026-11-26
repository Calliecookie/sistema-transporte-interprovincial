package pe.transportes.gestion.seguridad;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.modelo.PermisoRol;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.repositorio.PermisoRolRepositorio;

@Component
public class ServicioPermisos {

    private final PermisoRolRepositorio repositorio;

    public ServicioPermisos(PermisoRolRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public boolean permite(Authentication autenticacion, Modulo modulo) {
        Rol rol = rolDe(autenticacion);
        return rol != null && permite(rol, modulo);
    }

    @Transactional(readOnly = true)
    public boolean permite(Rol rol, Modulo modulo) {
        if (rol == Rol.ADMIN) {
            return true;
        }
        return modulo.getRolAplicable() == rol && repositorio.existsByRolAndModulo(rol, modulo);
    }

    @Transactional(readOnly = true)
    public Set<Modulo> modulosDe(Authentication autenticacion) {
        Rol rol = rolDe(autenticacion);
        if (rol == null) {
            return EnumSet.noneOf(Modulo.class);
        }
        if (rol == Rol.ADMIN) {
            return EnumSet.allOf(Modulo.class);
        }
        Set<Modulo> modulos = EnumSet.noneOf(Modulo.class);
        for (PermisoRol permiso : repositorio.findByRol(rol)) {
            if (permiso.getModulo().getRolAplicable() == rol) {
                modulos.add(permiso.getModulo());
            }
        }
        return modulos;
    }

    private Rol rolDe(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            return null;
        }
        Collection<? extends GrantedAuthority> autoridades = autenticacion.getAuthorities();
        for (Rol rol : Rol.values()) {
            for (GrantedAuthority autoridad : autoridades) {
                if (("ROLE_" + rol.name()).equals(autoridad.getAuthority())) {
                    return rol;
                }
            }
        }
        return null;
    }
}
